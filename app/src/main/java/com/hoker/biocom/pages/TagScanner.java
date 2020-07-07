package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hoker.biocom.R;
import com.hoker.biocom.fragments.ReadText;
import com.hoker.biocom.fragments.TagInfo;
import com.hoker.biocom.fragments.WriteToolbar;
import com.hoker.biocom.utilities.TagHandler;

import java.util.Objects;

public class TagScanner extends AppCompatActivity implements WriteToolbar.IEditButton
{
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    NfcAdapter adapter;
    Toolbar mToolbar;
    TextView mTextView1;
    TextView mTextView2;
    int _scanType;
    String _stringPayload;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_prompt);

        mTextView1 = findViewById(R.id.scan_text1);
        mTextView2 = findViewById(R.id.scan_text2);

        getScanType();
        setStatusBarColor();
        setTitleBar();
        nfcPrimer();
    }

    private void getScanType()
    {
        try
        {
            _scanType = (int)Objects.requireNonNull(getIntent().getExtras()).get("ScanType");
        }
        catch(NullPointerException e)
        {
            _scanType = -1;
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

    @Override
    public void buttonClicked()
    {
        Intent intent = new Intent(this, EditNdefPayload.class);
        intent.putExtra("StringNDEF", _stringPayload);
        startActivity(intent);
        finish();
    }

    public void nfcPrimer()
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

    public void setStatusBarColor()
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

    public void writeToTag(Intent intent)
    {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(TagHandler.writeNdefText(_stringPayload, tag))
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

    public void eraseTag(Intent intent)
    {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(TagHandler.eraseNfcTag(tag))
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
                        assert clipboard != null;
                        clipboard.setPrimaryClip(clipData);

                        //create new intent to open OpenKeychain
                        PackageManager manager = getBaseContext().getPackageManager();
                        Intent decryptionIntent = manager.getLaunchIntentForPackage("org.sufficientlysecure.keychain");

                        if(decryptionIntent != null)
                        {
                            decryptionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        else
                        {
                            decryptionIntent = new Intent(Intent.ACTION_VIEW);
                            decryptionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            decryptionIntent.setData(Uri.parse("https://f-droid.org/en/packages/org.sufficientlysecure.keychain/"));
                        }
                        startActivity(decryptionIntent);
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

    public void popBack()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void readTextRecord(Intent intent)
    {
        if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            Bundle bundle = new Bundle();
            _stringPayload = TagHandler.parseStringNdefPayload(intent);
            bundle.putString("StringNDEF", _stringPayload);

            ReadText readText = new ReadText();
            readText.setArguments(bundle);

            WriteToolbar writeToolbar = new WriteToolbar();
            writeToolbar.setInterface(this);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.scan_prompt_frame, readText);
            fragmentTransaction.replace(R.id.toolbar_frame, writeToolbar);
            fragmentTransaction.commit();
        }
    }

    public void handleActionDiscovered(Intent intent)
    {
        //if the foreground dispatch system intercept an NFC intent
        if(_scanType == -1)
        {
            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                readTextRecord(intent);
                String ndefStringMessage = TagHandler.parseStringNdefPayload(intent);
                if (ndefStringMessage.length() > 27)
                {
                    if (ndefStringMessage.substring(0, 27).equals("-----BEGIN PGP MESSAGE-----"))
                    {
                        attemptDecryption();
                    }
                }
            }
        }
        //NDEF Read Operation
        if(_scanType == 0)
        {
            mTextView1.setText(R.string.scan_text_ndef_read);
            mTextView2.setText("");

            readTextRecord(intent);
        }

        //NDEF Decrypt Operation
        else if(_scanType == 1)
        {
            mTextView1.setText(R.string.scan_text_encrypted);
            mTextView2.setText("");

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                readTextRecord(intent);
                if(_stringPayload.length() > 27)
                {
                    if(_stringPayload.substring(0,27).equals("-----BEGIN PGP MESSAGE-----"))
                    {
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

        //Tag Erase Operation
        else if(_scanType == 2)
        {
            mTextView1.setText(R.string.scan_text_writable);
            mTextView2.setText(R.string.scan_text_write_warning);

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TECH_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                eraseTag(intent);
            }
        }

        //NDEF Write Operation
        else if(_scanType == 3)
        {
            mTextView1.setText(R.string.scan_text_writable);
            mTextView2.setText(R.string.scan_text_write_warning);

            _stringPayload = getIntent().getStringExtra("StringNDEF");

            if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TECH_DISCOVERED)||Objects.equals(intent.getAction(), NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                writeToTag(intent);
            }
        }

        //Tag Info Operation
        else if(_scanType == 4)
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