package com.hoker.biocom.pages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hoker.biocom.R;

public class TagInfo extends AppCompatActivity
{
    Tag _tag;
    Intent _intent;
    Toolbar mToolbar;
    TextView mUIDTextView;
    TextView mTechTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tag_info);

        _intent = getIntent();
        _tag = _intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        mUIDTextView = findViewById(R.id.info_uid);
        mTechTextView = findViewById(R.id.info_tag_tech);

        setTitleBar();
        setStatusBarColor();
        getTagInfo();
        getTagCapacity();
    }

    private void getTagInfo()
    {
        mUIDTextView.setText(bytesToHexString(_tag.getId()));
        mTechTextView.setText(getNFCType(_tag.getTechList()));
    }

    private void getTagCapacity()
    {
    }

    private String getNFCType(String[] techList)
    {
        String buffer = "";
        for(int i=0; i<techList.length; i++)
        {
            if(techList[i].equals(MifareClassic.class.getName()))
            {
                MifareClassic mifareClassicTag = MifareClassic.get(_tag);
                switch(mifareClassicTag.getType())
                {
                    case MifareClassic.TYPE_CLASSIC:
                        //Type classic
                        buffer = buffer + "Mifare Classic" + "\n";
                        _techType = MifareClassic;
                        break;
                    case MifareClassic.TYPE_PLUS:
                        //Type plus
                        buffer = buffer + "Mifare Classic Plus" + "\n";
                        break;
                    case MifareClassic.TYPE_PRO:
                        //Type pro
                        buffer = buffer + "Mifare Classic Pro" + "\n";
                        break;
                }
            }
            else if(techList[i].equals(MifareUltralight.class.getName()))
            {
                MifareUltralight mifareUltralightTag = MifareUltralight.get(_tag);
                switch(mifareUltralightTag.getType())
                {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        //Type ultralight
                        buffer = buffer + "Mifare Ultralight" + "\n";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        //Type ultralight c
                        buffer = buffer + "Mifare Ultralight C" + "\n";
                        break;
                }
            }
            else if(techList[i].equals(Ndef.class.getName()))
            {
                buffer = buffer + "NDEF Formattable" + "\n";
            }
            else if(techList[i].equals(IsoDep.class.getName()))
            {
                buffer = buffer + "IsoDep" + "\n";
            }
        }
        if(buffer.length() > 0)
        {
            buffer = buffer.substring(0, buffer.length() - 1);
            return buffer;
        }
        return buffer;
    }

    private String bytesToHexString(byte[] src)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if(src == null || src.length <= 0)
        {
            return null;
        }
        char[] buffer = new char[2];
        for(int i=0; i<src.length; i++)
        {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private void setTitleBar()
    {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}