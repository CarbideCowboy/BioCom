package com.hoker.biocom.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.hoker.biocom.R;
import com.hoker.biocom.fragments.EditJpeg;
import com.hoker.biocom.fragments.EditMarkdown;
import com.hoker.biocom.fragments.EditText;
import com.hoker.biocom.fragments.EditUri;
import com.hoker.biocom.interfaces.ITracksPayload;
import com.hoker.biocom.interfaces.IEditFragment;

import java.util.Objects;

public class EditNdefPayload extends AppCompatActivity implements ITracksPayload
{
    NdefRecord _ndefRecord;
    byte[] _bytePayload;
    Toolbar mToolbar;
    TextView mPayloadSizeText;
    FloatingActionButton mWriteFab;
    IEditFragment _fragment;
    DisplayNdefPayload.recordDataType _dataType;
    Button mDataSelectionButton;

    //data type selection drawer
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_payload);
        mPayloadSizeText = findViewById(R.id.edit_payload_size);
        NavigationView mNavigationView = findViewById(R.id.edit_navigation_view);
        mDrawer = findViewById(R.id.edit_drawer_layout);
        mDataSelectionButton = findViewById(R.id.button_data_selection_toolbar_edit);

        mDataSelectionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mDrawer.openDrawer(GravityCompat.END);
            }
        });

        setupDrawerContent(mNavigationView);

        setStatusBarColor();
        setTitleBar();
        setStringPayload();
        setWriteButton();
        fragmentSwitcher();
        setBroadcastReceiver();
    }

    private void setWriteButton()
    {
        mWriteFab = findViewById(R.id.fab_write);
        mWriteFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                writeButton_Clicked();
            }
        });
    }

    @Override
    public void payloadChanged()
    {
        NdefMessage ndefMessage = new NdefMessage(_fragment.getRecord());
        int byteSize = ndefMessage.getByteArrayLength();
        String payloadSize = getString(R.string.payload_size) + byteSize + getString(R.string.bytes);
        mPayloadSizeText.setText(payloadSize);
    }

    private void setStringPayload()
    {
        if(getIntent().getExtras() != null)
        {
            _dataType = (DisplayNdefPayload.recordDataType)Objects.requireNonNull(getIntent().getExtras().get("DataType"));
            _bytePayload = (byte[])Objects.requireNonNull(getIntent().getExtras().get("Payload"));
        }
        else
        {
            _dataType = DisplayNdefPayload.recordDataType.plainText;
            _bytePayload = "".getBytes();
        }
    }

    private void setBroadcastReceiver()
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                assert action != null;
                if(action.equals("finish_activity"))
                {
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish_edit_activity"));
    }

    public void writeButton_Clicked()
    {
        _ndefRecord = _fragment.getRecord();
        if(_ndefRecord != null)
        {
            Intent intent = new Intent(this, TagScanner.class);
            intent.putExtra("ScanType", TagScanner.scanType.writeNdef);
            intent.putExtra("NdefMessage", new NdefMessage(_ndefRecord));
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
        mToolbar = findViewById(R.id.toolbar_edit);
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

    public void fragmentSwitcher()
    {
        if(_dataType.equals(DisplayNdefPayload.recordDataType.plainText))
        {
            _fragment = new EditText();
            Bundle bundle = new Bundle();
            bundle.putByteArray("Payload", _bytePayload);
            ((EditText)_fragment).setArguments(bundle);
            updateFragment();
            _fragment.setPayloadTrackingInterface(this);
            mDataSelectionButton.setText(R.string.plain_text);
            mDrawer.closeDrawers();
        }
        else if(_dataType.equals(DisplayNdefPayload.recordDataType.Uri))
        {
            _fragment = new EditUri();
            updateFragment();
            _fragment.setPayloadTrackingInterface(this);
            mDataSelectionButton.setText(R.string.uri_text);
            mDrawer.closeDrawers();
        }
        else if(_dataType.equals(DisplayNdefPayload.recordDataType.Jpeg))
        {
            _fragment = new EditJpeg();
            Bundle bundle = new Bundle();
            bundle.putByteArray("Payload", _bytePayload);
            ((EditJpeg)_fragment).setArguments(bundle);
            updateFragment();
            _fragment.setPayloadTrackingInterface(this);
            mDataSelectionButton.setText(R.string.jpeg);
            mDrawer.closeDrawers();
        }
        else if(_dataType.equals(DisplayNdefPayload.recordDataType.Markdown))
        {
            _fragment = new EditMarkdown();
            Bundle bundle = new Bundle();
            bundle.putByteArray("Payload", _bytePayload);
            ((EditMarkdown)_fragment).setArguments(bundle);
            updateFragment();
            _fragment.setPayloadTrackingInterface(this);
            mDataSelectionButton.setText(R.string.markdown);
            mDrawer.closeDrawers();
        }
    }

    private void updateFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.edit_payload_frame, (Fragment)_fragment);
        fragmentTransaction.commit();
        payloadChanged();
    }

    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
                    {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                }
        );
    }

    private void selectDrawerItem(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.nav_plaintext:
                _dataType = DisplayNdefPayload.recordDataType.plainText;
                fragmentSwitcher();
                break;
            case R.id.nav_uri:
                _dataType = DisplayNdefPayload.recordDataType.Uri;
                fragmentSwitcher();
                break;
            case R.id.nav_jpeg:
                _dataType = DisplayNdefPayload.recordDataType.Jpeg;
                fragmentSwitcher();
                break;
            case R.id.nav_markdown:
                _dataType = DisplayNdefPayload.recordDataType.Markdown;
                fragmentSwitcher();
                break;
        }
    }
}