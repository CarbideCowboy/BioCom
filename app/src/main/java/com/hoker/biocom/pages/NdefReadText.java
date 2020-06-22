package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hoker.biocom.R;

import java.net.URI;
import java.net.URISyntaxException;

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
            if(URLUtil.isValidUrl(_stringPayload))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
