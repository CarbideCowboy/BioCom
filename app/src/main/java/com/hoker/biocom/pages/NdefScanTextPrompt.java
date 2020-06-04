package com.hoker.biocom.pages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hoker.biocom.R;
import com.hoker.biocom.utilities.TagHandler;

import java.util.Objects;

public class NdefScanTextPrompt extends AppCompatActivity
{
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    NfcAdapter adapter;
    Toolbar mToolbar;
    boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scan_prompt);

        isEdit = getIntent().getExtras().getBoolean("IsEdit");

        setStatusBarColor();
        setTitleBar();
        nfcPrimer();
    }

    private void setTitleBar()
    {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
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

    public void setStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void handleActionNdefDiscovered(Intent intent)
    {
        if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            String ndefStringMessage = TagHandler.parseStringNdefPayload(intent);
            if(isEdit)
            {
                Intent ndefEditIntent = new Intent(this, NdefEditTextPayload.class);
                ndefEditIntent.putExtra("StringNDEF", ndefStringMessage);
                startActivity(ndefEditIntent);
            }
            else
            {
                Intent ndefReadIntent = new Intent(this, NdefReadText.class);
                ndefReadIntent.putExtra("StringNDEF", ndefStringMessage);
                startActivity(ndefReadIntent);
            }
        }
    }
}
