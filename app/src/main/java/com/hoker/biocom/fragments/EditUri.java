package com.hoker.biocom.fragments;

import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;
import com.hoker.biocom.interfaces.ITracksPayload;

import java.util.Objects;

public class EditUri extends Fragment implements IEditFragment
{
    android.widget.EditText mUriEditText;
    ITracksPayload payloadInterface;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_edit_uri, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        mUriEditText = Objects.requireNonNull(getView()).findViewById(R.id.edit_uri_entry);
        setupTextChangedEvent();
    }

    public void setPayloadInterface(ITracksPayload iTracksPayload)
    {
        this.payloadInterface = iTracksPayload;
    }

    public void setupTextChangedEvent()
    {
        mUriEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                payloadInterface.payloadChanged();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public NdefRecord getRecord()
    {
        if(mUriEditText == null)
        {
            return NdefRecord.createUri(" ");
        }
        else
        {
            String uri = mUriEditText.getText().toString();
            if(uri.isEmpty())
            {
                return NdefRecord.createUri(" ");
            }
            return NdefRecord.createUri(uri);
        }
    }
}