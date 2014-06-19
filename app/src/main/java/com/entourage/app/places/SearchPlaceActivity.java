package com.entourage.app.places;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.entourage.app.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SearchPlaceActivity extends Activity
{
    private BackgroundLoadingTask mLoadingTask = null;
    private ProgressBar mProgressBar;
    private TextView mErrorTextView;
    private ListView mPlacesListView;
    private PlacesAdapter mPlacesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);
        overridePendingTransition(R.anim.activity_enter_from_left, R.anim.activity_leave_from_left);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.action_back));
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mErrorTextView = (TextView) findViewById(R.id.error_text_view);
        mPlacesListView = (ListView) findViewById(R.id.placesList);
        mPlacesListAdapter = new PlacesAdapter(this, new ArrayList<Place>());
        mPlacesListView.setAdapter(mPlacesListAdapter);
        showProgress(false);
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
    public void finish()
    {
        if (mLoadingTask != null)
            mLoadingTask.cancel(true);
        super.finish();
        this.overridePendingTransition(R.anim.activity_enter_from_right, R.anim.activity_leave_from_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
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
            if (mLoadingTask == null)
            {
                mLoadingTask = new BackgroundLoadingTask();
                mLoadingTask.execute(query);
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private class BackgroundLoadingTask extends AsyncTask<String, Void, Boolean>
    {
        private List<Place> mPlacesResult;

        private BackgroundLoadingTask() {
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length <= 0)
                return null;
            mPlacesResult = PlacesApiUtils.search(params[0]);
            return mPlacesResult != null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadingTask = null;
            showProgress(false);
            if (success && !mPlacesResult.isEmpty()) {
                mPlacesListView.setVisibility(View.VISIBLE);
                mErrorTextView.setVisibility(View.GONE);
                mPlacesListAdapter.clear();
                mPlacesListAdapter.addAll(mPlacesResult);
                mPlacesListAdapter.notifyDataSetChanged();
            }
            else {
                mPlacesListView.setVisibility(View.GONE);
                mErrorTextView.setVisibility(View.VISIBLE);
                mPlacesListAdapter.notifyDataSetInvalidated();
            }
        }

        @Override
        protected void onCancelled() {
            mLoadingTask = null;
            showProgress(false);
        }
    }
}
