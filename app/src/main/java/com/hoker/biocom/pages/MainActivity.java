package com.hoker.biocom.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.hoker.biocom.R;
import com.hoker.biocom.utilities.NdefUtilities;
import com.hoker.biocom.utilities.OnSwipeTouchListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    NfcAdapter adapter;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private ConstraintLayout mMainConstraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainConstraintLayout = findViewById(R.id.main_constraint_layout);

        nfcPrimer();
        setStatusBarColor();
        setUpNavigationDrawer();
        setUpSwipeHandler();
        setVersionNumber();
        setUpAnimation();
    }

    private void setUpAnimation()
    {
        ImageView mAnimatedImage = findViewById(R.id.animation_imageview);
        final AnimatedVectorDrawableCompat animation = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_transmit_pulse);
        mAnimatedImage.setImageDrawable(animation);
        final Handler handler = new Handler(Looper.getMainLooper());
        assert animation != null;
        animation.registerAnimationCallback(new Animatable2Compat.AnimationCallback()
        {
            @Override
            public void onAnimationEnd(Drawable drawable)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        animation.start();
                    }
                });
                super.onAnimationStart(drawable);
            }
        });
        animation.start();
    }

    private void setVersionNumber()
    {
        TextView mVersionTextView = findViewById(R.id.main_version_text);
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

    @SuppressWarnings("deprecation")
    private void setUpNavigationDrawer()
    {
        mToolbar = findViewById(R.id.toolbar);
        mDrawer = findViewById(R.id.main_drawer_layout);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
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

        NavigationView mNavigationView = findViewById(R.id.main_navigation_view);

        setupDrawerContent(mNavigationView);
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
            ndef.addDataType("image/jpeg");
        }
        catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("fail", e);
        }

        //create a filter array for the foreground dispatch system
        intentFiltersArray = new IntentFilter[] {ndef};

        //checks to see if the launch intent is NDEF_DISCOVERED
        handleActionDiscovered(this.getIntent());
    }

    private void handleActionDiscovered(Intent intent)
    {
        if(Objects.equals(intent.getAction(), NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            Intent filterCaptureIntent = new Intent(this, TagScanner.class);
            filterCaptureIntent.putExtra("ScanType", TagScanner.scanType.readNdef);
            filterCaptureIntent.putExtra("NdefMessage", NdefUtilities.getNdefMessage(intent));
            startActivity(filterCaptureIntent);
        }
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
        handleActionDiscovered(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig)
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
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
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
            case R.id.nav_ndef_decrypt:
                Intent ndefDecryptIntent = new Intent(this, TagScanner.class);
                ndefDecryptIntent.putExtra("ScanType", TagScanner.scanType.decryptNdef);
                startActivity(ndefDecryptIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_ndef_write:
                Intent ndefWriteIntent = new Intent(this, EditNdefPayload.class);
                startActivity(ndefWriteIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_erase_tag:
                Intent tagEraseIntent = new Intent(this, TagScanner.class);
                tagEraseIntent.putExtra("ScanType", TagScanner.scanType.eraseTag);
                startActivity(tagEraseIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_tag_info:
                Intent tagInfoIntent = new Intent(this, TagScanner.class);
                tagInfoIntent.putExtra("ScanType", TagScanner.scanType.tagInfo);
                startActivity(tagInfoIntent);
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
