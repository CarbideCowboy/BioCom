package com.hoker.biocom.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.openintents.openpgp.IOpenPgpService2;

public class OpenPgpApi {

    public static final String TAG = "OpenPgp API";

    public static final String SERVICE_INTENT_2 = "org.openintents.openpgp.IOpenPgpService2";

    /**
     * see CHANGELOG.md
     */
    public static final int API_VERSION = 11;

    @Deprecated
    public static final String ACTION_SIGN = "org.openintents.openpgp.action.SIGN";

    public static final String ACTION_ENCRYPT = "org.openintents.openpgp.action.ENCRYPT";

    /* Intent extras */
    public static final String EXTRA_API_VERSION = "api_version";

    // ACTION_DETACHED_SIGN, ENCRYPT, SIGN_AND_ENCRYPT, DECRYPT_VERIFY
    // request ASCII Armor for output
    // OpenPGP Radix-64, 33 percent overhead compared to binary, see http://tools.ietf.org/html/rfc4880#page-53)
    public static final String EXTRA_REQUEST_ASCII_ARMOR = "ascii_armor";

    // ENCRYPT, SIGN_AND_ENCRYPT, QUERY_AUTOCRYPT_STATUS
    public static final String EXTRA_USER_IDS = "user_ids";

    /* Service Intent returns */
    public static final String RESULT_CODE = "result_code";

    // get actual error object from RESULT_ERROR
    public static final int RESULT_CODE_ERROR = 0;
    // success!
    public static final int RESULT_CODE_SUCCESS = 1;
    // get PendingIntent from RESULT_INTENT, start PendingIntent with startIntentSenderForResult,
    // and execute service method again in onActivityResult
    public static final int RESULT_CODE_USER_INTERACTION_REQUIRED = 2;

    public static final String RESULT_ERROR = "error";
    public static final String RESULT_INTENT = "intent";

    public static final String RESULT_SIGNATURE = "signature";

    final IOpenPgpService2 mService;
    final Context mContext;
    final AtomicInteger mPipeIdGen = new AtomicInteger();

    public OpenPgpApi(Context context, IOpenPgpService2 service) {
        this.mContext = context;
        this.mService = service;
    }

    public Intent executeApi(Intent data, InputStream is, OutputStream os) {
        ParcelFileDescriptor input = null;
        try {
            if (is != null) {
                input = ParcelFileDescriptorUtil.pipeFrom(is);
            }

            return executeApi(data, input, os);
        } catch (Exception e) {
            Log.e(OpenPgpApi.TAG, "Exception in executeApi call", e);
            Intent result = new Intent();
            result.putExtra(RESULT_CODE, RESULT_CODE_ERROR);
            result.putExtra(RESULT_ERROR,
                    new OpenPgpError(OpenPgpError.CLIENT_SIDE_ERROR, e.getMessage()));
            return result;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(OpenPgpApi.TAG, "IOException when closing ParcelFileDescriptor!", e);
                }
            }
        }
    }

    /**
     * InputStream and OutputStreams are always closed after operating on them!
     */
    public Intent executeApi(Intent data, ParcelFileDescriptor input, OutputStream os) {
        ParcelFileDescriptor output = null;
        try {
            // always send version from client
            data.putExtra(EXTRA_API_VERSION, OpenPgpApi.API_VERSION);

            Intent result;

            Thread pumpThread = null;
            int outputPipeId = 0;

            if (os != null) {
                outputPipeId = mPipeIdGen.incrementAndGet();
                output = mService.createOutputPipe(outputPipeId);
                pumpThread = ParcelFileDescriptorUtil.pipeTo(os, output);
            }

            // blocks until result is ready
            result = mService.execute(data, input, outputPipeId);

            // set class loader to current context to allow unparcelling
            // of OpenPgpError and OpenPgpSignatureResult
            // http://stackoverflow.com/a/3806769
            result.setExtrasClassLoader(mContext.getClassLoader());

            //wait for ALL data being pumped from remote side
            if (pumpThread != null) {
                pumpThread.join();
            }

            return result;
        } catch (Exception e) {
            Log.e(OpenPgpApi.TAG, "Exception in executeApi call", e);
            Intent result = new Intent();
            result.putExtra(RESULT_CODE, RESULT_CODE_ERROR);
            result.putExtra(RESULT_ERROR,
                    new OpenPgpError(OpenPgpError.CLIENT_SIDE_ERROR, e.getMessage()));
            return result;
        } finally {
            // close() is required to halt the TransferThread
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Log.e(OpenPgpApi.TAG, "IOException when closing ParcelFileDescriptor!", e);
                }
            }
        }
    }

}

