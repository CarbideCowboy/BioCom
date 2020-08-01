package com.hoker.biocom.pages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hoker.biocom.R;
import com.hoker.biocom.fragments.ReadJpeg;
import com.hoker.biocom.fragments.ReadText;
import com.hoker.biocom.fragments.ReadToolbar;
import com.hoker.biocom.interfaces.IEditButton;

import java.util.Objects;

public class DisplayNdefPayload extends AppCompatActivity implements IEditButton
{
    NdefMessage _ndefMessage;
    recordDataType _recordDataType;
    Fragment _fragment;

    public enum recordDataType
    {
        plainText,
        Uri,
        Jpeg,
        Unknown
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_ndef_payload);
        setStatusBarColor();
        setTitleBar();

        _ndefMessage = (NdefMessage)Objects.requireNonNull(getIntent().getExtras()).get("NdefMessage");
        _recordDataType = getDataType();
        prepareFragment();
    }

    private void setTitleBar()
    {
        ReadToolbar readToolbar = new ReadToolbar();
        readToolbar.setInterface(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.display_toolbar_frame, readToolbar);
        fragmentTransaction.commit();
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
        intent.putExtra("DataType", _recordDataType);
        intent.putExtra("Payload", _ndefMessage.getRecords()[0].getPayload());
        startActivity(intent);
        finish();
    }

    private void prepareFragment()
    {
        Bundle bundle;
        if(_recordDataType == recordDataType.plainText)
        {
            _fragment = new ReadText();
        }
        else if(_recordDataType == recordDataType.Jpeg)
        {
            _fragment = new ReadJpeg();
        }

        bundle = new Bundle();
        bundle.putByteArray("Payload", _ndefMessage.getRecords()[0].getPayload());
        _fragment.setArguments(bundle);
        updateFragment();
    }

    private void setStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void updateFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.display_payload_frame, _fragment);
        fragmentTransaction.commit();
    }

    private recordDataType getDataType()
    {
        NdefRecord record = _ndefMessage.getRecords()[0];
        short tnf = record.getTnf();
        String mimeType;

        switch(tnf)
        {
            case NdefRecord.TNF_ABSOLUTE_URI:
                //absolute uri
                break;
            case NdefRecord.TNF_EMPTY:
                //empty tnf
                break;
            case NdefRecord.TNF_EXTERNAL_TYPE:
                //external type
                break;
            case NdefRecord.TNF_MIME_MEDIA:
                mimeType = record.toMimeType();
                if(mimeType.equals("text/plain"))
                {
                    return recordDataType.plainText;
                }
                else if(mimeType.equals("biocom/jpeg"))
                {
                    return recordDataType.Jpeg;
                }
            case NdefRecord.TNF_UNKNOWN:
                return recordDataType.Unknown;
            case NdefRecord.TNF_WELL_KNOWN:
                if(record.toUri()!=null)
                {
                    return recordDataType.Uri;
                }
                break;
        }
        return recordDataType.Unknown;
    }
}