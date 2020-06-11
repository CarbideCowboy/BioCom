package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoker.biocom.R;
import com.hoker.biocom.utilities.TagHandler;

import java.io.IOException;

public class NdefEditTextPayload extends AppCompatActivity
{
    String _stringPayload;
    EditText mEditText;
    Toolbar mToolbar;
    ScrollView mScrollView;
    LinearLayout mLinearLayout;
    TextView mPayloadSizeText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ndef_edit_text);

        mEditText = findViewById(R.id.ndef_edit_text);
        mScrollView = findViewById(R.id.edit_text_scroll);
        mLinearLayout = findViewById(R.id.edit_text_linear);
        mPayloadSizeText = findViewById(R.id.edit_payload_size);

        setStatusBarColor();
        setTitleBar();
        setEditTextChangeEvent();
        setupTextView();
        getPayloadBytes();
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
        try
        {
            NdefRecord[] record = { TagHandler.createTextRecord(mEditText.getText().toString()) };
            NdefMessage message = new NdefMessage(record);
            int byteSize = message.getByteArrayLength();
            String payloadSize = getString(R.string.payload_size) + byteSize + getString(R.string.bytes);
            mPayloadSizeText.setText(payloadSize);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void focusEntry(View view)
    {
        mEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        int position = mEditText.length();
        Editable editable = mEditText.getText();
        Selection.setSelection(editable, position);
    }

    private void setupTextView()
    {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null)
        {
            _stringPayload = bundle.getString("StringNDEF");
            mEditText.setText(_stringPayload);
        }
    }

    public void writeButton_Clicked(View view)
    {
        if(!mEditText.getText().toString().isEmpty())
        {
            Intent intent = new Intent(this, ScanTagPrompt.class);
            intent.putExtra("ScanType", 3);
            intent.putExtra("StringNDEF", mEditText.getText().toString());
            startActivity(intent);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("String payload cannot be empty");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
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

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void setTitleBar()
    {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}