package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hoker.biocom.R;
import com.hoker.biocom.fragments.TagInfo;
import com.hoker.biocom.utilities.NdefUtilities;
import com.hoker.biocom.utilities.TagUtilities;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TagScanner extends AppCompatActivity
{
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    NfcAdapter adapter;
    Toolbar mToolbar;
    TextView mTextView1;
    TextView mTextView2;
    scanType _scanType;
    NdefMessage _ndefMessage = null;
    String _decryptionPayload;
    OpenPgpServiceConnection mServiceConnection;

    public enum scanType
    {
        foreGroundDispatch,
        readNdef,
        writeNdef,
        decryptNdef,
        eraseTag,
        tagInfo
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_scanner);

        mTextView1 = findViewById(R.id.scan_text1);
        mTextView2 = findViewById(R.id.scan_text2);

        getScanType();
        setStatusBarColor();
        setTitleBar();
        nfcPrimer();

        mServiceConnection = new OpenPgpServiceConnection(this, "org.sufficientlysecure.keychain");
        mServiceConnection.bindToService();
    }

    private void getScanType()
    {
        _scanType = (scanType)Objects.requireNonNull(getIntent().getExtras()).get("ScanType");
        if(_scanType == null)
        {
            _scanType = scanType.foreGroundDispatch;
        }
    }

    private void setTitleBar()
    {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void nfcPrimer()
    {
        //setup the physical nfc interface
        adapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //set up NDEF_DISCOVERED filter for the foreground dispatch system
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try
        {
            ndef.addDataType("text/plain");
        }
        catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("fail", e);
        }

        //create a filter array for the foreground dispatch system
        intentFiltersArray = new IntentFilter[] {ndef, tech, tag};

        //checks to see if the launch intent is NDEF_DISCOVERED
        handleActionDiscovered(this.getIntent());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        adapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleActionDiscovered(intent);
    }

    private void setStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void badEncryptedPayload()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Scanned device does not contain an encrypted payload");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void writeToTag(Intent intent, NdefMessage ndefMessage)
    {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(TagUtilities.writeNdefMessage(tag, ndefMessage))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("NDEF write operation successful");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                    popBack();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("NDEF write operation was unsuccessful");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void eraseTag(Intent intent)
    {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(TagUtilities.eraseNfcTag(tag))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Erase operation successful");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                    popBack();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Erase operation was unsuccessful");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private boolean attemptDecryption(final String textPayload)
    {
        _decryptionPayload = textPayload;
        if(textPayload.length() > 27)
        {
            if (textPayload.substring(0, 27).equals("-----BEGIN PGP MESSAGE-----"))
            {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder
                        .setTitle("Encrypted payload detected")
                        .setMessage("Would you like to attempt decryption with OpenKeychain?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                try
                                {
                                    Intent data = new Intent();
                                    data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);
                                    openPgpDecryptText(data);
                                }
                                catch(ActivityNotFoundException e)
                                {
                                    Intent fdroidIntent = new Intent(Intent.ACTION_VIEW);
                                    fdroidIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    fdroidIntent.setData(Uri.parse("https://f-droid.org/en/packages/org.sufficientlysecure.keychain/"));
                                    startActivity(fdroidIntent);
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                                if(_scanType == scanType.foreGroundDispatch)
                                {
                                    readNdefPayload();
                                }
                            }
                        });

                //create alert dialog
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);

                //display dialog
                alertDialog.show();
                return false;
            }
            return true;
        }
        return true;
    }

    private void openPgpDecryptText(Intent data)
    {
        InputStream inputStream = new ByteArrayInputStream(_decryptionPayload.getBytes(StandardCharsets.UTF_8));
        OpenPgpApi api = new OpenPgpApi(TagScanner.this, mServiceConnection.getService());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Intent result = api.executeApi(data, inputStream, outputStream);
        switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            case OpenPgpApi.RESULT_CODE_SUCCESS:
                String decryptedMessage = outputStream.toString();
                _ndefMessage = new NdefMessage(NdefRecord.createTextRecord("en", decryptedMessage));
                readNdefPayload();
                break;
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                try
                {
                    assert pi != null;
                    startIntentSenderForResult(pi.getIntentSender(), 42, null, 0, 0, 0);
                }
                catch (IntentSender.SendIntentException e)
                {
                    Log.e("Tag", "SendIntentException", e);
                }
                break;
            case OpenPgpApi.RESULT_CODE_ERROR:
                OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                assert error != null;
                if(error.getErrorId() == OpenPgpError.CLIENT_SIDE_ERROR)
                {
                    Intent fdroidIntent = new Intent(Intent.ACTION_VIEW);
                    fdroidIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    fdroidIntent.setData(Uri.parse("https://f-droid.org/en/packages/org.sufficientlysecure.keychain/"));
                    startActivity(fdroidIntent);
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Error Decrypting: Possibly missing key", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // try again after user interaction
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == 42)
            {
                openPgpDecryptText(data);
            }
        }
        else
        {
            if(_scanType == scanType.foreGroundDispatch)
            {
                readNdefPayload();
            }
        }
    }

    private void readNdefPayload()
    {
        Intent displayNdefIntent = new Intent(TagScanner.this, DisplayNdefPayload.class);
        displayNdefIntent.putExtra("NdefMessage", _ndefMessage);
        startActivity(displayNdefIntent);
        finish();
    }

    private void popBack()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Intent broadcastIntent = new Intent("finish_activity");
        sendBroadcast(broadcastIntent);
        finish();
    }

    private void handleActionDiscovered(Intent intent)
    {
        if(_scanType == scanType.readNdef)
        {
            _ndefMessage = (NdefMessage)Objects.requireNonNull(getIntent().getExtras()).get("NdefMessage");
            readNdefPayload();
        }

        if(_scanType == scanType.foreGroundDispatch)
        {
            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                _ndefMessage = NdefUtilities.getNdefMessage(intent);
                if(attemptDecryption(NdefUtilities.parseStringNdefPayloadFromIntent(intent)))
                {
                    readNdefPayload();
                }
            }
        }

        else if(_scanType == scanType.decryptNdef)
        {
            mTextView1.setText(R.string.scan_text_encrypted);
            mTextView2.setText("");

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                if(attemptDecryption(NdefUtilities.parseStringNdefPayloadFromIntent(intent)))
                {
                    badEncryptedPayload();
                }
            }
        }

        else if(_scanType == scanType.eraseTag)
        {
            mTextView1.setText(R.string.scan_text_writable);
            mTextView2.setText(R.string.scan_text_write_warning);

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TECH_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                eraseTag(intent);
            }
        }

        else if(_scanType == scanType.writeNdef)
        {
            mTextView1.setText(R.string.scan_text_writable);
            mTextView2.setText(R.string.scan_text_write_warning);

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TECH_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                writeToTag(intent, _ndefMessage);
            }
            else
            {
                _ndefMessage = intent.getParcelableExtra("NdefMessage");
            }
        }

        else if(_scanType == scanType.tagInfo)
        {
            mTextView1.setText(R.string.scan_nfc);
            mTextView2.setText("");

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TECH_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                Bundle bundle = new Bundle();
                bundle.putParcelable("Intent", intent);

                Fragment tagInfo = new TagInfo();
                tagInfo.setArguments(bundle);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.scan_prompt_frame, tagInfo);
                fragmentTransaction.commit();
            }
        }
    }
}
