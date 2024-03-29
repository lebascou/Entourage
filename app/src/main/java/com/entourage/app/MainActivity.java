package com.entourage.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.entourage.app.home.FragmentHome;
import com.entourage.app.places.FragmentPlaces;
import com.entourage.app.profile.FragmentProfile;
import com.entourage.app.profile.Profile;
import com.entourage.app.settings.FragmentSettings;

import java.util.HashMap;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Profile mProfile;
    private MainActivityLoadingTask mLoadingTask = null;
    private ProgressBar mProgressBar;

    /**
     * Fragment views
     */
    private HashMap<String, Fragment> mFragmentViews;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.mainProgressBar);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mLoadingTask = new MainActivityLoadingTask();
        mLoadingTask.execute();
    }

    public void setDrawerPicture(String url)
    {
        mNavigationDrawerFragment.setProfilePicture(url);
    }

    public void showProgress(boolean show)
    {
        if (show)
        {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String item_title)
    {
        Fragment curFragment = mFragmentViews.get(item_title);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, curFragment)
                .commit();
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    public class MainActivityLoadingTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params) {
            // Load user profile
            Bundle extras = getIntent().getExtras();
            mProfile = (Profile) extras.get(Profile.EXTRA_USER_PROFILE);

            // Setup the views
            mFragmentViews = new HashMap<String, Fragment>();
            FragmentProfile fragmentProfile = new FragmentProfile();
            fragmentProfile.setArguments(extras);
            mFragmentViews.put(getString(R.string.title_home), new FragmentHome());
            mFragmentViews.put(getString(R.string.title_profile), fragmentProfile);
            mFragmentViews.put(getString(R.string.title_places), new FragmentPlaces());
            mFragmentViews.put(getString(R.string.title_clothes), new FragmentClothes());
            mFragmentViews.put(getString(R.string.title_settings), new FragmentSettings());
            mFragmentViews.put(getString(R.string.title_wanted), new FragmentWanted());

            mTitle = getTitle();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadingTask = null;
            showProgress(false);

            mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
            mNavigationDrawerFragment.setProfilePicture(mProfile.getProfilePicture(0));
            onNavigationDrawerItemSelected(getString(R.string.title_places));
        }

        @Override
        protected void onCancelled() {
            mLoadingTask = null;
            showProgress(false);
        }
    }
}
