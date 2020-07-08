package com.hoker.biocom.fragments;

import android.content.Context;
import android.nfc.NdefMessage;
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
import com.hoker.biocom.utilities.TagHandler;

import java.util.Objects;

public class EditText extends Fragment implements IEditFragment
{
    String _stringPayload;
    android.widget.EditText mEditText;
    ScrollView mScrollView;
    LinearLayout mLinearLayout;
    TextView mPayloadSizeText;

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

        setEditTextChangeEvent();
        setupTextView();
        getPayloadBytes();
    }

    private final View.OnClickListener mLinearLayout_Clicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            focusEntry();
        }
    };

    @Override
    public String getPayload()
    {
        return mEditText.getText().toString();
    }

    private void setEditTextChangeEvent()
    {
        mEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                getPayloadBytes();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    private void getPayloadBytes()
    {
        NdefRecord[] record = { TagHandler.createTextRecord(mEditText.getText().toString()) };
        NdefMessage message = new NdefMessage(record);
        int byteSize = message.getByteArrayLength();
        String payloadSize = getString(R.string.payload_size) + byteSize + getString(R.string.bytes);
        mPayloadSizeText.setText(payloadSize);
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
        _stringPayload = getArguments().getString("StringNDEF");
        mEditText.setText(_stringPayload);
    }
}