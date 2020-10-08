package com.hoker.biocom.fragments;

import android.app.Activity;
import android.content.Context;
import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;
import com.hoker.biocom.interfaces.ITracksPayload;
import com.hoker.biocom.utilities.NdefUtilities;

import java.util.Objects;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class EditMarkdown extends Fragment implements IEditFragment
{
    LinearLayout mLinearLayout;
    android.widget.EditText mEditText;

    ITracksPayload payloadInterface;

    ImageButton mPreviewButton;
    ImageButton mBoldButton;
    ImageButton mItalicizeButton;
    ImageButton mCodeButton;
    ImageButton mLinkButton;
    ImageButton mStrikethroughButton;
    ImageButton mListButton;
    ImageButton mQuoteButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_edit_markdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        mLinearLayout = Objects.requireNonNull(getView()).findViewById(R.id.linear_edit_markdown);
        mEditText = getView().findViewById(R.id.edittext_edit_markdown);

        final Markwon markwon = Markwon.create(Objects.requireNonNull(getActivity()));
        final MarkwonEditor editor = MarkwonEditor.create(markwon);
        mEditText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

        mLinearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                focusEntry();
            }
        });

        setupMarkdownButtons();
        setupEditText();
        setupTextChangedEvent();
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
                return NdefRecord.createMime("text/markdown", payload);
            }
            else
            {
                return NdefRecord.createMime("text/markdown", "".getBytes());
            }
        }
        return NdefRecord.createMime("text/markdown", mEditText.getText().toString().getBytes());
    }

    @Override
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

    private void setupMarkdownButtons()
    {
        mPreviewButton = Objects.requireNonNull(getView()).findViewById(R.id.button_preview_markdown);
        mBoldButton = getView().findViewById(R.id.button_markdown_bold);
        mItalicizeButton = getView().findViewById(R.id.button_markdown_italicize);
        mCodeButton = getView().findViewById(R.id.button_markdown_code);
        mLinkButton = getView().findViewById(R.id.button_markdown_link);
        mStrikethroughButton = getView().findViewById(R.id.button_markdown_strikethrough);
        mListButton = getView().findViewById(R.id.button_markdown_bullet_list);
        mQuoteButton = getView().findViewById(R.id.button_markdown_quote);

        mPreviewButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Fragment previewFragment = new ReadMarkdown();
                Bundle bundle = new Bundle();
                bundle.putByteArray("Payload", mEditText.getText().toString().getBytes());
                previewFragment.setArguments(bundle);
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.edit_payload_frame, previewFragment).addToBackStack("preview");
                fragmentTransaction.commit();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            }
        });

        mBoldButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundSelection("**");
            }
        });

        mItalicizeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundSelection("_");
            }
        });

        mCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundSelection("`");
            }
        });

        mLinkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        mStrikethroughButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundSelection("~~");
            }
        });

        mListButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        mQuoteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }

    private void focusEntry()
    {
        mEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        int position = mEditText.length();
        Editable editable = mEditText.getText();
        Selection.setSelection(editable, position);
    }

    private void insertTextAroundSelection(String text)
    {
        int selectionStart = mEditText.getSelectionStart();
        int selectionEnd = mEditText.getSelectionEnd();

        mEditText.getText().insert(selectionStart, text);
        mEditText.getText().insert(selectionEnd+text.length(), text);

        Selection.setSelection(mEditText.getText(), selectionEnd+text.length());
    }

    private void setupEditText()
    {
        if(getArguments() != null)
        {
            byte[] payload = getArguments().getByteArray("Payload");
            if(payload != null)
            {
                mEditText.setText(NdefUtilities.getStringFromBytes(payload));
            }
            else
            {
                mEditText.setText(NdefUtilities.getStringFromBytes("".getBytes()));
            }
        }
        else
        {
            mEditText.setText(NdefUtilities.getStringFromBytes("".getBytes()));
        }
    }
}