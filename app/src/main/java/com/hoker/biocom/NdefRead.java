package com.hoker.biocom;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class NdefRead extends AppCompatActivity
{
    private TextView mUxNdefTextbox;
    private String _stringPayload;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ndef_read);

        //set up views
        mUxNdefTextbox = findViewById(R.id.uxNdefTextbox);

        pullNdefRecord();
        checkEncryption();
    }

    public void pullNdefRecord()
    {
        Intent NdefIntent = getIntent();
        Bundle bundle = NdefIntent.getExtras();
        if(bundle != null)
        {
            _stringPayload = bundle.getString("StringNDEF");
            mUxNdefTextbox.setText(_stringPayload);
        }
    }

    public void checkEncryption()
    {
        //check for minimum length
        if(_stringPayload.length() < 27)
        {
            return;
        }
        //check for pgp header
        else if(!_stringPayload.substring(0,27).equals("-----BEGIN PGP MESSAGE-----"))
        {
            return;
        }
        else
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
    }
}
