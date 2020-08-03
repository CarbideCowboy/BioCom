package com.hoker.biocom.pages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    final String[] URI_PREFIX = new String[]
    {
        /* 0x00 */ "",
        /* 0x01 */ "http://www.",
        /* 0x02 */ "https://www.",
        /* 0x03 */ "http://",
        /* 0x04 */ "https://",
        /* 0x05 */ "tel:",
        /* 0x06 */ "mailto:",
        /* 0x07 */ "ftp://anonymous:anonymous@",
        /* 0x08 */ "ftp://ftp.",
        /* 0x09 */ "ftps://",
        /* 0x0A */ "sftp://",
        /* 0x0B */ "smb://",
        /* 0x0C */ "nfs://",
        /* 0x0D */ "ftp://",
        /* 0x0E */ "dav://",
        /* 0x0F */ "news:",
        /* 0x10 */ "telnet://",
        /* 0x11 */ "imap:",
        /* 0x12 */ "rtsp://",
        /* 0x13 */ "urn:",
        /* 0x14 */ "pop:",
        /* 0x15 */ "sip:",
        /* 0x16 */ "sips:",
        /* 0x17 */ "tftp:",
        /* 0x18 */ "btspp://",
        /* 0x19 */ "btl2cap://",
        /* 0x1A */ "btgoep://",
        /* 0x1B */ "tcpobex://",
        /* 0x1C */ "irdaobex://",
        /* 0x1D */ "file://",
        /* 0x1E */ "urn:epc:id:",
        /* 0x1F */ "urn:epc:tag:",
        /* 0x20 */ "urn:epc:pat:",
        /* 0x21 */ "urn:epc:raw:",
        /* 0x22 */ "urn:epc:",
        /* 0x23 */ "urn:nfc:"
    };

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
        else if(_recordDataType == recordDataType.Uri)
        {
            openUriIntent();
        }

        bundle = new Bundle();
        bundle.putByteArray("Payload", _ndefMessage.getRecords()[0].getPayload());
        _fragment.setArguments(bundle);
        updateFragment();
    }

    private void openUriIntent()
    {
        byte[] bytes = _ndefMessage.getRecords()[0].getPayload();
        int prefixCode = bytes[0] & 0x0FF;
        if(prefixCode >= URI_PREFIX.length)
        {
            prefixCode = 0;
        }
        String uri = new String(bytes, 1, bytes.length - 1, StandardCharsets.UTF_8);
        uri = URI_PREFIX[prefixCode] + uri;
        Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(uriIntent);
        finish();
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
                else if(mimeType.equals("image/jpeg"))
                {
                    return recordDataType.Jpeg;
                }
            case NdefRecord.TNF_UNKNOWN:
                return recordDataType.Unknown;
            case NdefRecord.TNF_WELL_KNOWN:
                byte[] type = record.getType();
                if(Arrays.equals(type, NdefRecord.RTD_URI))
                {
                    return recordDataType.Uri;
                }
                else if(Arrays.equals(type, NdefRecord.RTD_TEXT))
                {
                    return recordDataType.plainText;
                }
                break;
        }
        return recordDataType.Unknown;
    }
}