package com.hoker.biocom.pages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hoker.biocom.R;
import com.hoker.biocom.fragments.EditText;
import com.hoker.biocom.interfaces.ITracksPayload;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EncryptNdefText extends AppCompatActivity implements ITracksPayload
{
    OpenPgpServiceConnection mServiceConnection;
    EditText _fragment;
    Toolbar mToolbar;
    FloatingActionButton mWriteFab;
    String _userID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_ndef_text);

        setEditTextFragment();
        setStatusBarColor();
        setTitleBar();
        setWriteButton();
        setBroadcastReceiver();

        mServiceConnection = new OpenPgpServiceConnection(this, "org.sufficientlysecure.keychain");
        mServiceConnection.bindToService();
    }

    private void setWriteButton()
    {
        mWriteFab = findViewById(R.id.fab_write);
        mWriteFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(EncryptNdefText.this);
                builder.setTitle(R.string.enter_pgp_id);
                final android.widget.EditText input = new android.widget.EditText(EncryptNdefText.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        _userID = input.getText().toString();
                        Intent data = new Intent();
                        data.setAction(OpenPgpApi.ACTION_ENCRYPT);
                        data.putExtra(OpenPgpApi.EXTRA_USER_IDS, new String[]{_userID});
                        data.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                        encrypt(data);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void encrypt(Intent data)
    {
        InputStream inputStream = new ByteArrayInputStream(_fragment.getEntryText().getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OpenPgpApi api = new OpenPgpApi(this, mServiceConnection.getService());
        Intent result = api.executeApi(data, inputStream, outputStream);

        switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            case OpenPgpApi.RESULT_CODE_SUCCESS:
                try
                {
                    Log.d(OpenPgpApi.TAG, "output: " + outputStream.toString("UTF-8"));
                    Intent intent = new Intent(this, TagScanner.class);
                    intent.putExtra("ScanType", TagScanner.scanType.writeNdef);
                    intent.putExtra("NdefMessage", new NdefMessage(NdefRecord.createTextRecord("en", outputStream.toString())));
                    startActivity(intent);
                }
                catch (UnsupportedEncodingException e)
                {
                    Log.e("Tag", "UnsupportedEncodingException", e);
                }
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
                    Toast toast = Toast.makeText(getApplicationContext(), "Error encrypting", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // try again after user interaction
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 42) {
                encrypt(data);
            }
        }
    }

    private void setBroadcastReceiver()
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                assert action != null;
                if(action.equals("finish_activity"))
                {
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish_edit_activity"));
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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void setEditTextFragment()
    {
        _fragment = new EditText();
        _fragment.setPayloadTrackingInterface(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.edit_text_encrypt_frame, _fragment);
        fragmentTransaction.commit();
        payloadChanged();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mServiceConnection != null)
        {
            mServiceConnection.unbindFromService();
        }
    }

    @Override
    public void payloadChanged()
    {

    }
}