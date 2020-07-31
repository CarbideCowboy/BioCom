package com.hoker.biocom.fragments;

import android.content.Context;
import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;
import com.hoker.biocom.interfaces.ITracksPayload;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EditText extends Fragment implements IEditFragment
{
    String _stringPayload;
    android.widget.EditText mEditText;
    ScrollView mScrollView;
    LinearLayout mLinearLayout;
    TextView mPayloadSizeText;
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
        return inflater.inflate(R.layout.fragment_edit_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        mEditText = Objects.requireNonNull(getView()).findViewById(R.id.ndef_edit_text);
        mScrollView = Objects.requireNonNull(getView()).findViewById(R.id.edit_text_scroll);
        mLinearLayout = Objects.requireNonNull(getView()).findViewById(R.id.edit_text_linear);
        mPayloadSizeText = Objects.requireNonNull(getView()).findViewById(R.id.edit_payload_size);

        mLinearLayout.setOnClickListener(mLinearLayout_Clicked);

        setupTextView();
        setupTextChangedEvent();
    }

    private final View.OnClickListener mLinearLayout_Clicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            focusEntry();
        }
    };

    public void setPayloadTrackingInterface(ITracksPayload iTracksPayload)
    {
        this.payloadInterface = iTracksPayload;
    }

    public void setupTextChangedEvent()
    {
        mEditText.addTextChangedListener(new TextWatcher()
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
        if(mEditText == null)
        {
            assert getArguments() != null;
            byte[] payload = getArguments().getByteArray("Payload");
            if(payload != null)
            {
                return NdefRecord.createMime("text/plain", payload);
            }
            else
            {
                return NdefRecord.createMime("text/plain", "".getBytes());
            }
        }
        return NdefRecord.createMime("text/plain", mEditText.getText().toString().getBytes());
    }

    public void focusEntry()
    {
        mEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        int position = mEditText.length();
        Editable editable = mEditText.getText();
        Selection.setSelection(editable, position);
    }

    private void setupTextView()
    {
        assert getArguments() != null;
        byte[] payload = getArguments().getByteArray("Payload");
        if(payload != null)
        {
            _stringPayload = new String(payload, StandardCharsets.UTF_8);
        }
        else
        {
            _stringPayload = "";
        }
        mEditText.setText(_stringPayload);
    }
}