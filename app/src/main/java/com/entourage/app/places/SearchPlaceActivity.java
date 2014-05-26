package com.entourage.app.places;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.entourage.app.R;

public class SearchPlaceActivity extends Activity
{
    private BackgroundLoadingTask mLoadingTask = null;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture);
        overridePendingTransition(R.anim.activity_enter_from_left, R.anim.activity_leave_from_left);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.action_back));
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        showProgress(false);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.activity_enter_from_right, R.anim.activity_leave_from_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.search_place, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setIconified(false);
        searchView.setQueryHint(getString(R.string.search_place_hint));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });
        searchView.setOnQueryTextListener(new OnSearchListener());
        searchView.requestFocus();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        this.overridePendingTransition(R.anim.activity_enter_from_right, R.anim.activity_leave_from_right);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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

    private class OnSearchListener implements SearchView.OnQueryTextListener
    {
        @Override
        public boolean onQueryTextSubmit(String query) {
            
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private class BackgroundLoadingTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadingTask = null;
        }

        @Override
        protected void onCancelled() {
            mLoadingTask = null;
            showProgress(false);
        }
    }
}
