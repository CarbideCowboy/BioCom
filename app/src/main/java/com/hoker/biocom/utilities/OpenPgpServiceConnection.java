package com.hoker.biocom.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.openintents.openpgp.IOpenPgpService2;

@SuppressWarnings("unused")
public class OpenPgpServiceConnection {

    // callback interface
    public interface OnBound {
        void onBound(IOpenPgpService2 service);

        void onError(Exception e);
    }

    private final Context mApplicationContext;

    private IOpenPgpService2 mService;
    private final String mProviderPackageName;

    private OnBound mOnBoundListener;

    public OpenPgpServiceConnection(Context context, String providerPackageName) {
        this.mApplicationContext = context.getApplicationContext();
        this.mProviderPackageName = providerPackageName;
    }

    public IOpenPgpService2 getService() {
        return mService;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IOpenPgpService2.Stub.asInterface(service);
            if (mOnBoundListener != null) {
                mOnBoundListener.onBound(mService);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public void bindToService() {
        // if not already bound...
        if (mService == null) {
            try {
                Intent serviceIntent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
                // NOTE: setPackage is very important to restrict the intent to this provider only!
                serviceIntent.setPackage(mProviderPackageName);
                boolean connect = mApplicationContext.bindService(serviceIntent, mServiceConnection,
                        Context.BIND_AUTO_CREATE);
                if (!connect) {
                    throw new Exception("bindService() returned false!");
                }
            } catch (Exception e) {
                if (mOnBoundListener != null) {
                    mOnBoundListener.onError(e);
                }
            }
        } else {
            // already bound, but also inform client about it with callback
            if (mOnBoundListener != null) {
                mOnBoundListener.onBound(mService);
            }
        }
    }

    public void unbindFromService() {
        mApplicationContext.unbindService(mServiceConnection);
    }

}

