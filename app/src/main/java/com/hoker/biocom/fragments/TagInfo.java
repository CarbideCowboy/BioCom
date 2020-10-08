package com.hoker.biocom.fragments;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoker.biocom.R;
import com.hoker.biocom.utilities.FingerprintUtilities;
import com.hoker.biocom.utilities.HexUtilities;

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
        EnumMap<infoType, String> tagInfo = fingerprintTag();
        mUIDTextView.setText(tagInfo.get(infoType.UID));
        mManufacturerTextView.setText(tagInfo.get(infoType.tagManufacturer));
        mTypeTextView.setText(tagInfo.get(infoType.tagType));
        mIsWriteTextView.setText(tagInfo.get(infoType.isWriteable));
        mCanReadOnlyTextView.setText(tagInfo.get(infoType.canBeMadeReadOnly));
    }

    private EnumMap<infoType, String> fingerprintTag()
    {
        EnumMap<infoType, String> info = new EnumMap<>(infoType.class);

        //get manufacturer
        byte[] UIDBytes = _tag.getId();
        byte manufacturerByte = UIDBytes[0];
        info.put(infoType.tagManufacturer, getManufacturerFromByte(manufacturerByte));

        //get UID
        info.put(infoType.UID, HexUtilities.bytesToHex(UIDBytes));

        //get tag type
        info.put(infoType.tagType, FingerprintUtilities.fingerprintNfcTag(_tag));

        //pull isWritable and canBeMadeReadOnly
        Ndef ndef = Ndef.get(_tag);
        if(ndef != null)
        {
            if(ndef.isWritable())
            {
                info.put(infoType.isWriteable, "Yes");
            }
            else
            {
                info.put(infoType.isWriteable, "No");
            }
            if(ndef.canMakeReadOnly())
            {
                info.put(infoType.canBeMadeReadOnly, "Yes");
            }
            else
            {
                info.put(infoType.canBeMadeReadOnly, "No");
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
}