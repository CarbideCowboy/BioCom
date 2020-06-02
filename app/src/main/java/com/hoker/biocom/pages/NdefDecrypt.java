package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hoker.biocom.R;
import com.hoker.biocom.utilities.TagHandler;

import java.util.Objects;

public class NdefDecrypt extends AppCompatActivity
{
    private String _stringPayload;
    private Toolbar mToolbar;
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    NfcAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ndef_decrypt);

        setStatusBarColor();
        setTitleBar();
        nfcPrimer();
        pullNdefRecord();
    }

   public void pullNdefRecord()
    {
        Intent NdefIntent = getIntent();
        Bundle bundle = NdefIntent.getExtras();
        if(bundle != null)
        {
            _stringPayload = bundle.getString("StringNDEF");
            attemptDecryption();
        }
    }

    public void nfcPrimer()
    {
        //setup the physical nfc interface
        adapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //set up NDEF_DISCOVERED filter for the foreground dispatch system
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try
        {
            ndef.addDataType("text/plain");
        }
        catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("fail", e);
        }

        //create a filter array for the foreground dispatch system
        intentFiltersArray = new IntentFilter[] {ndef, };

        //checks to see if the launch intent is NDEF_DISCOVERED
        handleActionNdefDiscovered(this.getIntent());
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
        handleActionNdefDiscovered(intent);
    }

    public void handleActionNdefDiscovered(Intent intent)
    {
        if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            String ndefStringMessage = TagHandler.parseStringNdefPayload(intent);
            if(ndefStringMessage.length() > 27)
            {
                if(ndefStringMessage.substring(0,27).equals("-----BEGIN PGP MESSAGE-----"))
                {
                    _stringPayload = ndefStringMessage;
                    attemptDecryption();
                }
                else
                {
                    badEncryptedPayload();
                }
            }
            else
            {
                badEncryptedPayload();
            }
        }
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

    public void attemptDecryption()
    {
        //set title, message and yes/no functionality for the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle("Encrypted payload detected")
                .setMessage("Would you like to attempt decryption with OpenKeychain?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //copy string ndef payload to system clipboard
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("payload", _stringPayload);
                        clipboard.setPrimaryClip(clipData);

                        //create new intent to open OpenKeychain
                        PackageManager manager = getBaseContext().getPackageManager();
                        Intent decryptionIntent = manager.getLaunchIntentForPackage("org.sufficientlysecure.keychain");

                        if(decryptionIntent != null)
                        {
                            decryptionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(decryptionIntent);
                        }
                        else
                        {
                            decryptionIntent = new Intent(Intent.ACTION_VIEW);
                            decryptionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            decryptionIntent.setData(Uri.parse("https://f-droid.org/en/packages/org.sufficientlysecure.keychain/"));
                            startActivity(decryptionIntent);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

        //create alert dialog
        AlertDialog alertDialog = dialogBuilder.create();

        //display dialog
        alertDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    public void setStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void setTitleBar()
    {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}