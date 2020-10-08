package com.hoker.biocom.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoker.biocom.R;
import com.hoker.biocom.utilities.NdefUtilities;

import java.util.Objects;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;

public class ReadMarkdown extends Fragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_read_markdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        assert getArguments() != null;
        byte[] _payload = getArguments().getByteArray("Payload");

        TextView mReadMarkdownTextbox = Objects.requireNonNull(getView()).findViewById(R.id.textview_read_markdown);

        final Markwon markwon = Markwon.builder(Objects.requireNonNull(getActivity()))
                .usePlugin(StrikethroughPlugin.create())
                .build();

        if(_payload != null)
        {
            markwon.setMarkdown(mReadMarkdownTextbox, NdefUtilities.getStringFromBytes(_payload));
        }
    }
}