package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hoker.biocom.R;
import com.hoker.biocom.fragments.EditText;
import com.hoker.biocom.interfaces.IEditFragment;

import java.util.Objects;

public class EditNdefPayload extends AppCompatActivity
{
    String _stringPayload;
    Toolbar mToolbar;
    IEditFragment _fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_payload);

        setStringPayload();
        startEditTextFragment();
        setStatusBarColor();
        setTitleBar();
    }

    private void startEditTextFragment()
    {
        Bundle bundle = new Bundle();
        bundle.putString("StringNDEF", _stringPayload);

        _fragment = new EditText();
        ((EditText)_fragment).setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.edit_payload_frame, (Fragment)_fragment);
        fragmentTransaction.commit();
    }

    private void setStringPayload()
    {
        if(getIntent().getExtras() != null)
        {
            _stringPayload = getIntent().getExtras().getString("StringNDEF");
        }
        else
        {
            _stringPayload = "";
        }
    }

    public void writeButton_Clicked(View view)
    {
        _stringPayload = _fragment.getPayload();
        if(!_stringPayload.isEmpty())
        {
            Intent intent = new Intent(this, TagScanner.class);
            intent.putExtra("ScanType", 3);
            intent.putExtra("StringNDEF", _stringPayload);
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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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