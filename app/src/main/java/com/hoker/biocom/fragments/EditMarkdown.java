package com.hoker.biocom.fragments;

import android.content.Context;
import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;
import com.hoker.biocom.interfaces.ITracksPayload;

import java.text.BreakIterator;
import java.util.Objects;

public class EditMarkdown extends Fragment implements IEditFragment
{
    LinearLayout mLinearLayout;
    android.widget.EditText mEditText;

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

        mLinearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                focusEntry();
            }
        });

        setupMarkdownButtons();
    }

    @Override
    public NdefRecord getRecord()
    {
        return NdefRecord.createTextRecord("en","nothing");
    }

    @Override
    public void setPayloadTrackingInterface(ITracksPayload iTracksPayload)
    {

    }

    private void setupMarkdownButtons()
    {
        mBoldButton = Objects.requireNonNull(getView()).findViewById(R.id.button_markdown_bold);
        mItalicizeButton = getView().findViewById(R.id.button_markdown_italicize);
        mCodeButton = getView().findViewById(R.id.button_markdown_code);
        mLinkButton = getView().findViewById(R.id.button_markdown_link);
        mStrikethroughButton = getView().findViewById(R.id.button_markdown_strikethrough);
        mListButton = getView().findViewById(R.id.button_markdown_bullet_list);
        mQuoteButton = getView().findViewById(R.id.button_markdown_quote);

        mBoldButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundCursor("**");
            }
        });

        mItalicizeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundCursor("_");
            }
        });

        mCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                insertTextAroundCursor("`");
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
                insertTextAroundCursor("~~");
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

    private void insertTextAroundCursor(String text)
    {
        int cursorPosition = mEditText.getSelectionStart();
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(mEditText.getText().toString());
        int wordStart;
        if(iterator.isBoundary(cursorPosition))
        {
            wordStart = cursorPosition;
        }
        else
        {
            wordStart = iterator.preceding(cursorPosition);
        }
        int wordEnd = iterator.following(cursorPosition);

        mEditText.getText().insert(wordStart, text);
        mEditText.getText().insert(wordEnd+text.length(), text);
    }
}