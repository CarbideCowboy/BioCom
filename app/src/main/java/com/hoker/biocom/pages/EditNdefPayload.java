package com.hoker.biocom.pages;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.hoker.biocom.R;
import com.hoker.biocom.fragments.EditText;
import com.hoker.biocom.interfaces.IEditFragment;

import java.util.Objects;

public class EditNdefPayload extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    String _stringPayload;
    Toolbar mToolbar;
    Spinner mToolbarSpinner;
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
        setBroadcastReceiver();
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

    private void setBroadcastReceiver()
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                assert action != null;
                if(action.equals("finish_edit_activity"))
                {
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish_edit_activity"));
    }

    public void writeButton_Clicked(View view)
    {
        _stringPayload = _fragment.getPayload();
        if(!_stringPayload.isEmpty())
        {
            Intent intent = new Intent(this, TagScanner.class);
            intent.putExtra("ScanType", TagScanner.scanType.writeNdef);
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
        mToolbar = findViewById(R.id.toolbar_edit);
        mToolbarSpinner = findViewById(R.id.toolbar_edit_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.mime_type_options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mToolbarSpinner.setAdapter(adapter);
        mToolbarSpinner.setOnItemSelectedListener(this);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String selectedOption = (String) parent.getItemAtPosition(position);
        fragmentSwitcher(selectedOption);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    public void fragmentSwitcher(String selectedOption)
    {

    }
}