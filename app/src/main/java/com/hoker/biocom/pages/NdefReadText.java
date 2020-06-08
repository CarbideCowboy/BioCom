package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hoker.biocom.R;

public class NdefReadText extends AppCompatActivity
{
    private TextView mReadTextbox;
    private String _stringPayload;
    private Toolbar mToolbar;
    private ImageButton mToolbarWriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ndef_read);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //set up views
        mReadTextbox = findViewById(R.id.read_textbox);
        mToolbarWriteButton = findViewById(R.id.toolbar_write_button);

        setStatusBarColor();
        setTitleBar();
        setUpToolbarButton();
        pullNdefRecord();
    }

    public void pullNdefRecord()
    {
        Intent NdefIntent = getIntent();
        Bundle bundle = NdefIntent.getExtras();
        if(bundle != null)
        {
            _stringPayload = bundle.getString("StringNDEF");
            mReadTextbox.setText(_stringPayload);
        }
    }

    private void setUpToolbarButton()
    {
        mToolbarWriteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), NdefEditTextPayload.class);
                intent.putExtra("StringNDEF", _stringPayload);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void setTitleBar()
    {
        mToolbar = findViewById(R.id.toolbar_write);
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
