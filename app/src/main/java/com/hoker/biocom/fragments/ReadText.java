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

import java.util.Objects;

public class ReadText extends Fragment
{
    private TextView mReadTextbox;
    private String _stringPayload;

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
        _stringPayload = getArguments().getString("StringNDEF");

        //set up views
        mReadTextbox = Objects.requireNonNull(getView()).findViewById(R.id.read_textbox);

        pullNdefRecord();
    }

    public void pullNdefRecord()
    {
        if(_stringPayload != null)
        {
            mReadTextbox.setText(_stringPayload);
            if(URLUtil.isValidUrl(_stringPayload))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setMessage("Would you like to open encoded URL externally?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent openURL = new Intent(Intent.ACTION_VIEW);
                        openURL.setData(Uri.parse(_stringPayload));
                        startActivity(openURL);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}