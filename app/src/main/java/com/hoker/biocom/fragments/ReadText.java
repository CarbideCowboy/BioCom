package com.hoker.biocom.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.hoker.biocom.R;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ReadText extends Fragment
{
    private TextView mReadTextbox;
    private byte[] _payload;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_read_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        assert getArguments() != null;
        _payload = getArguments().getByteArray("Payload");

        //set up views
        mReadTextbox = Objects.requireNonNull(getView()).findViewById(R.id.read_textbox);

        if(_payload != null)
        {
            mReadTextbox.setText(new String(_payload, StandardCharsets.UTF_8));
        }
    }
}