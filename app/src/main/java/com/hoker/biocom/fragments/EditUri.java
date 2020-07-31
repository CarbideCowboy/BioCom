package com.hoker.biocom.fragments;

import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;

import java.util.Objects;

public class EditUri extends Fragment implements IEditFragment
{
    android.widget.EditText mUriEditText;

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
    }

    @Override
    public NdefRecord getRecord()
    {
        String uri = mUriEditText.getText().toString();
        return NdefRecord.createUri(uri);
    }
}