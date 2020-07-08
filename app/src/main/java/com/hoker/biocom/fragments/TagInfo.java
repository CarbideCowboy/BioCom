package com.hoker.biocom.fragments;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoker.biocom.R;

import java.util.EnumMap;
import java.util.Objects;

public class TagInfo extends Fragment
{
    Tag _tag;
    Intent _intent;
    TextView mUIDTextView;
    TextView mTypeTextView;
    TextView mManufacturerTextView;
    TextView mIsWriteTextView;
    TextView mCanReadOnlyTextView;

    public enum infoType
    {
        UID,
        tagManufacturer,
        tagType,
        isWriteable,
        canBeMadeReadOnly
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        _intent = getArguments().getParcelable("Intent");
        assert _intent != null;
        _tag = _intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_tag_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        findViewsById();
        getTagInfo();
    }

    private void findViewsById()
    {
        mUIDTextView = Objects.requireNonNull(getView()).findViewById(R.id.info_uid);
        mTypeTextView = Objects.requireNonNull(getView()).findViewById(R.id.info_tag_type);
        mManufacturerTextView = Objects.requireNonNull(getView()).findViewById(R.id.info_manufacturer);
        mIsWriteTextView = Objects.requireNonNull(getView()).findViewById(R.id.info_tag_is_write);
        mCanReadOnlyTextView = Objects.requireNonNull(getView()).findViewById(R.id.info_tag_can_be_read_only);
    }

    private void getTagInfo()
    {
        EnumMap<infoType, String> tagInfo = fingerprintTag(_tag.getTechList());
        mUIDTextView.setText(tagInfo.get(infoType.UID));
        mManufacturerTextView.setText(tagInfo.get(infoType.tagManufacturer));
        mTypeTextView.setText(tagInfo.get(infoType.tagType));
        mIsWriteTextView.setText(tagInfo.get(infoType.isWriteable));
        mCanReadOnlyTextView.setText(tagInfo.get(infoType.canBeMadeReadOnly));
    }

    private EnumMap<infoType, String> fingerprintTag(String[] techList)
    {
        // String[] info = new String[5];
        EnumMap<infoType, String> info = new EnumMap<>(infoType.class);
        info.put(infoType.tagType, "Unknown Tag Type");
        info.put(infoType.isWriteable, "Tag is read only");

        //get manufacturer
        byte[] UIDBytes = _tag.getId();
        byte manufacturerByte = UIDBytes[0];
        info.put(infoType.tagManufacturer, getManufacturerFromByte(manufacturerByte));

        //get UID
        info.put(infoType.UID, bytesToHexString(UIDBytes));

        for (String s : techList)
        {
            if (s.equals(MifareClassic.class.getName()))
            {
                MifareClassic mifareClassicTag = MifareClassic.get(_tag);
                switch (mifareClassicTag.getType())
                {
                    case MifareClassic.TYPE_CLASSIC:
                        //Type classic
                        info.put(infoType.tagType, "Mifare Classic");
                        info.put(infoType.tagManufacturer, "NXP Semiconductors");
                        break;
                    case MifareClassic.TYPE_PLUS:
                        //Type plus
                        info.put(infoType.tagType, "Mifare Classic Plus");
                        info.put(infoType.tagManufacturer, "NXP Semiconductors");
                        break;
                    case MifareClassic.TYPE_PRO:
                        //Type pro
                        info.put(infoType.tagType, "Mifare Classic Pro");
                        info.put(infoType.tagManufacturer, "NXP Semiconductors");
                        break;
                }
            }
            else if (s.equals(MifareUltralight.class.getName()))
            {
                MifareUltralight mifareUltralightTag = MifareUltralight.get(_tag);
                switch (mifareUltralightTag.getType())
                {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        //Type ultralight
                        info.put(infoType.tagType, "Mifare Ultralight");
                        info.put(infoType.tagManufacturer, "NXP Semiconductors");
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        //Type ultralight c
                        info.put(infoType.tagType, "Mifare Ultralight C");
                        info.put(infoType.tagManufacturer, "NXP Semiconductors");
                        break;
                }
            }
            else if (s.equals(Ndef.class.getName()))
            {
                Ndef ndefTag = Ndef.get(_tag);
                if (ndefTag.isWritable())
                {
                    info.put(infoType.isWriteable, "True");
                } else
                {
                    info.put(infoType.isWriteable, "False");
                }
                if (ndefTag.canMakeReadOnly())
                {
                    info.put(infoType.canBeMadeReadOnly, "True");
                } else
                {
                    info.put(infoType.canBeMadeReadOnly, "False");
                }
            }
            else if (s.equals(IsoDep.class.getName()))
            {
                info.put(infoType.tagType, "IsoDep");
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
        for (byte b : src)
        {
            buffer[0] = Character.forDigit((b >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(b & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}