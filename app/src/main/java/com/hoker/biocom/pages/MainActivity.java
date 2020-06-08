package com.hoker.biocom.pages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.hoker.biocom.R;
import com.hoker.biocom.utilities.OnSwipeTouchListener;
import com.hoker.biocom.utilities.TagHandler;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    //navigation drawer and toolbar
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private ConstraintLayout mMainConstraintLayout;
    private TextView mVersionTextView;
    //intent filter and foreground dispatch
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    NfcAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mMainConstraintLayout = findViewById(R.id.main_constraint_layout);

        setStatusBarColor();
        setUpNavigationDrawer();
        setUpSwipeHandler();
        setVersionNumber();
        nfcPrimer();
    }

    private void setVersionNumber()
    {
        mVersionTextView = findViewById(R.id.main_version_text);
        try
        {
            PackageInfo pInfo = getBaseContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionText = "R" + pInfo.versionName;
            mVersionTextView.setText(versionText);
        }
        catch(PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void nfcPrimer()
    {
        //setup the physical nfc interface
        adapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //set up NDEF_DISCOVERED filter for the foreground dispatch system
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try
        {
            ndef.addDataType("text/plain");
        }
        catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("fail", e);
        }

        //create a filter array for the foreground dispatch system
        intentFiltersArray = new IntentFilter[] {ndef, };

        //checks to see if the launch intent is NDEF_DISCOVERED
        handleActionNdefDiscovered(this.getIntent());
    }

    public void handleActionNdefDiscovered(Intent intent)
    {
        if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            String ndefStringMessage = TagHandler.parseStringNdefPayload(intent);

            //check for minimum length for encrypted payload
            if(ndefStringMessage.length() < 27)
            {
                pushToNdefRead(ndefStringMessage);
            }
            else
            {
                //check for pgp message header
                if(ndefStringMessage.substring(0,27).equals("-----BEGIN PGP MESSAGE-----"))
                {
                    pushToDecrypt(ndefStringMessage);
                }
                else
                {
                    pushToNdefRead(ndefStringMessage);
                }
            }
        }
    }

    private void pushToNdefRead(String ndefStringMessage)
    {
        Intent ndefReadIntent = new Intent(this, NdefReadText.class);
        ndefReadIntent.putExtra("StringNDEF", ndefStringMessage);
        startActivity(ndefReadIntent);
    }

    private void pushToDecrypt(String ndefStringMessage)
    {
        Intent ndefDecryptIntent = new Intent(this, NdefDecrypt.class);
        ndefDecryptIntent.putExtra("StringNDEF", ndefStringMessage);
        startActivity(ndefDecryptIntent);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        adapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleActionNdefDiscovered(intent);
    }

    private void setUpNavigationDrawer()
    {
        mToolbar = findViewById(R.id.toolbar);
        mDrawer = findViewById(R.id.main_drawer_layout);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_toggle_open, R.string.drawer_toggle_close)
        {
            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView)
            {
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.main_navigation_view);

        setupDrawerContent(mNavigationView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener()
            {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem)
                {
                    selectDrawerItem(menuItem);
                    return true;
                }
            }
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpSwipeHandler()
    {
        mMainConstraintLayout.setOnTouchListener(new OnSwipeTouchListener(this)
        {
            @Override
            public void onSwipeRight()
            {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });
    }

    private void selectDrawerItem(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.nav_ndef_read:
                Intent ndefReadIntent = new Intent(this, NdefScanTextPrompt.class);
                ndefReadIntent.putExtra("IsEdit", false);
                startActivity(ndefReadIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_ndef_decrypt:
                Intent ndefDecryptIntent = new Intent(this, NdefDecrypt.class);
                startActivity(ndefDecryptIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_ndef_write:
                Intent ndefWriteIntent = new Intent(this, NdefEditTextPayload.class);
                startActivity(ndefWriteIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_erase_tag:
                Intent tagEraseIntent = new Intent(this, EraseTag.class);
                startActivity(tagEraseIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
        }
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
