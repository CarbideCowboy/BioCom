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
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
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
    TextView mManufacturerTextView;
    TextView mModelTextView;
    TextView mIsWriteTextView;
    TextView mCanReadOnlyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tag_info);

        _intent = getIntent();
        _tag = _intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        findViewsById();
        setTitleBar();
        setStatusBarColor();
        getTagInfo();
    }

    private void findViewsById()
    {
        mUIDTextView = findViewById(R.id.info_uid);
        mTechTextView = findViewById(R.id.info_tag_tech);
        mManufacturerTextView = findViewById(R.id.info_manufacturer);
        mModelTextView = findViewById(R.id.info_tag_model);
        mIsWriteTextView = findViewById(R.id.info_tag_is_write);
        mCanReadOnlyTextView = findViewById(R.id.info_tag_can_be_read_only);
    }

    private void getTagInfo()
    {
        String[] tagInfo = fingerprintTag(_tag.getTechList());
        mUIDTextView.setText(tagInfo[0]);
        mManufacturerTextView.setText(tagInfo[1]);
        mTechTextView.setText(tagInfo[2]);
        mModelTextView.setText(tagInfo[3]);
        mIsWriteTextView.setText(tagInfo[4]);
        mCanReadOnlyTextView.setText(tagInfo[5]);
    }

    private String[] fingerprintTag(String[] techList)
    {
        String[] info = new String[7];
        /*
         * Info index breakdown:
         * 0: UID
         * 1: Tag Manufacturer
         * 2: Tag Type
         * 3: Tag Model
         * 4: Is Writeable
         * 5: Can be Made Read Only
         */
        info[2] = "Unknown Tag Type";
        info[3] = "Unknown Tag Model";
        info[4] = "Tag is read only";

        //get manufacturer
        byte[] UIDBytes = _tag.getId();
        byte manufacturerByte = UIDBytes[0];
        info[1] = getManufacturerFromByte(manufacturerByte);

        //get UID
        info[0] = bytesToHexString(UIDBytes);

        for(int i=0; i<techList.length; i++)
        {
            if(techList[i].equals(MifareClassic.class.getName()))
            {
                MifareClassic mifareClassicTag = MifareClassic.get(_tag);
                switch(mifareClassicTag.getType())
                {
                    case MifareClassic.TYPE_CLASSIC:
                        //Type classic
                        info[2] = "Mifare Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        //Type plus
                        info[2] = "Mifare Classic Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        //Type pro
                        info[2] = "Mifare Classic Pro";
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
                        info[2] = "Mifare Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        //Type ultralight c
                        info[2] = "Mifare Ultralight C";
                        break;
                }
            }
            else if(techList[i].equals(Ndef.class.getName()))
            {
                Ndef ndefTag = Ndef.get(_tag);
                if(ndefTag.isWritable())
                {
                    info[4] = "True";
                }
                else
                {
                    info[4] = "False";
                }
                if(ndefTag.canMakeReadOnly())
                {
                    info[5] = "True";
                }
                else
                {
                    info[5] = "False";
                }
            }
            else if(techList[i].equals(IsoDep.class.getName()))
            {
                info[2] = "IsoDep";
            }
        }
        return info;
    }

    private String getManufacturerFromByte(byte manufacturerByte)
    {
        if(manufacturerByte == (byte)0x04)
        {
            return "NXP Semiconductors";
        }
        else
        {
            return "Unknown Tag Manufacturer";
        }
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