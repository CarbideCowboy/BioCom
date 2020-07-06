package com.hoker.biocom.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.hoker.biocom.R;
import com.hoker.biocom.utilities.OnSwipeTouchListener;

public class MainActivity extends AppCompatActivity
{
    //navigation drawer and toolbar
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

        setStatusBarColor();
        setUpNavigationDrawer();
        setUpSwipeHandler();
        setVersionNumber();
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

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

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
            case R.id.nav_ndef_read:
                Intent ndefReadIntent = new Intent(this, TagScanner.class);
                ndefReadIntent.putExtra("ScanType", 0);
                startActivity(ndefReadIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_ndef_decrypt:
                Intent ndefDecryptIntent = new Intent(this, TagScanner.class);
                ndefDecryptIntent.putExtra("ScanType", 1);
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
                tagEraseIntent.putExtra("ScanType", 2);
                startActivity(tagEraseIntent);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_tag_info:
                Intent tagInfoIntent = new Intent(this, TagScanner.class);
                tagInfoIntent.putExtra("ScanType", 4);
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
