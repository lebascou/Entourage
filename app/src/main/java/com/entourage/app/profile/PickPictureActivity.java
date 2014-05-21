package com.entourage.app.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.entourage.app.R;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PickPictureActivity extends Activity
{
    private BackgroundLoadingTask mLoadingTask = null;
    private ProgressBar mProgressBar;
    private ListView mAlbumList;
    private GridView mPhotoList;
    private List<String[]> mAlbums = null;
    private AlbumAdapter mAlbumAdapter;
    private AlbumClickListener mAlbumClickListener;
    private PhotoClickListener mPhotoClickListener;
    private PhotoAdapter mPhotoAdapter;
    String mCurUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(R.drawable.ic_ab_back_holo_dark_am);
        getActionBar().setTitle(getString(R.string.title_albums));
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mAlbumList = (ListView) findViewById(R.id.album_list);
        mPhotoList = (GridView) findViewById(R.id.photo_list);

        mAlbumAdapter = new AlbumAdapter(this, new ArrayList<String[]>());
        mAlbumClickListener = new AlbumClickListener();
        mAlbumList.setAdapter(mAlbumAdapter);
        mAlbumList.setOnItemClickListener(mAlbumClickListener);

        mPhotoAdapter = new PhotoAdapter(this, new ArrayList<String[]>());
        mPhotoClickListener = new PhotoClickListener();
        mPhotoList.setAdapter(mPhotoAdapter);
        mPhotoList.setOnItemClickListener(mPhotoClickListener);

        showProgress(true);
        mLoadingTask = new BackgroundLoadingTask();
        mLoadingTask.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mAlbumList.getVisibility() == View.VISIBLE) {
            finish();
        }
        else {
            showAlbums();
        }
        return true;

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void showPhotos(List<String[]> photos)
    {
        mProgressBar.setVisibility(View.GONE);
        mAlbumList.setVisibility(View.GONE);

        mPhotoAdapter.clear();
        mPhotoAdapter.addAll(photos);
        mPhotoAdapter.notifyDataSetChanged();
        mPhotoList.setVisibility(View.VISIBLE);
    }

    public void showAlbums()
    {
        showProgress(false);
        mAlbumAdapter.clear();
        if (mAlbums != null) {
            mAlbumAdapter.addAll(mAlbums);
        }
        mAlbumAdapter.notifyDataSetChanged();
    }

    public void showProgress(boolean show)
    {
        if (show)
        {
            mProgressBar.setVisibility(View.VISIBLE);
            mAlbumList.setVisibility(View.GONE);
            mPhotoList.setVisibility(View.GONE);
        }
        else
        {
            mProgressBar.setVisibility(View.GONE);
            mPhotoList.setVisibility(View.GONE);
            mAlbumList.setVisibility(View.VISIBLE);
            getActionBar().setTitle(getString(R.string.title_albums));
        }
    }

    protected String getPhotoPictureUrl(String photoId)
    {
        mCurUrl = "";
        Bundle params = new Bundle();
        params.putString("fields", "picture");

        new Request(
                Session.getActiveSession(),
                "/" + photoId,
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response)
                    {
                        GraphObject graphObj = response.getGraphObject();
                        JSONObject dataObj = graphObj.getInnerJSONObject();
                        try {
                            mCurUrl = dataObj.getString("picture");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAndWait();
        return mCurUrl;
    }

    protected List<String[]> getAlbums()
    {
        final List<String[]> albums = new ArrayList<String[]>();
        Bundle params = new Bundle();

        // TODO: get photo of me with
        // /me/photos?fields=picture&limit=100&type=tagged
        //params.putString("fields", "picture");
        //params.putString("limit", "1");
        //params.putString("type", "tagged");

        params.putString("fields", "name,cover_photo,id,count");
        new Request(
                Session.getActiveSession(),
                "/me/albums",
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response)
                    {
                        GraphObject graphObj = response.getGraphObject();
                        try {
                            JSONArray dataObj = graphObj.getInnerJSONObject().getJSONArray("data");
                            for (int i = 0; i < dataObj.length(); i++)
                            {
                                JSONObject curObj = dataObj.getJSONObject(i);
                                String[] album = {
                                        curObj.getString("id"),
                                        curObj.getString("name"),
                                        curObj.getString("count"),
                                        getPhotoPictureUrl(curObj.getString("cover_photo"))
                                };
                                albums.add(album);
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
        ).executeAndWait();

        return albums;
    }

    protected List<String[]> getPhotos(String albumId)
    {
        final List<String[]> photos = new ArrayList<String[]>();
        Bundle params = new Bundle();

        params.putString("fields", "images");
        new Request(
                Session.getActiveSession(),
                "/" + albumId + "/photos",
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response)
                    {
                        GraphObject graphObj = response.getGraphObject();
                        try {
                            JSONArray dataObj = graphObj.getInnerJSONObject().getJSONArray("data");
                            for (int i = 0; i < dataObj.length(); i++)
                            {
                                JSONObject curObj = dataObj.getJSONObject(i);
                                String[] photo = new String[0];
                                try {
                                    String fitSrc = Profile.getBestFittedImage(curObj.getJSONArray("images"),
                                            Profile.DEFAULT_PICTURE_SIZE,
                                            Profile.DEFAULT_PICTURE_SIZE);
                                    if (fitSrc != null) {
                                        photo = new String[]{
                                                curObj.getString("id"),
                                                fitSrc
                                        };
                                        photos.add(photo);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
        ).executeAndWait();

        return photos;
    }

    private class AlbumClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            showProgress(true);
            String[] selectedAlbum = mAlbumAdapter.getItem(position);
            getActionBar().setTitle(selectedAlbum[1]);
            mLoadingTask = new BackgroundLoadingTask();
            mLoadingTask.execute(selectedAlbum[0]);
        }
    }

    private class PhotoClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent intent = getIntent();
            intent.putExtra("url", mPhotoAdapter.getItem(position)[1]);
            setResult(FragmentProfile.RESULT_PICTURE_SELECTED, intent);
            finish();
        }
    }

    private class BackgroundLoadingTask extends AsyncTask<String, Void, Boolean>
    {
        private List<String[]> curPhotos;

        @Override
        protected Boolean doInBackground(String... params) {
            curPhotos = null;
            if (params != null && params.length > 0) {
                curPhotos = getPhotos(params[0]);
            }
            else {
                mAlbums = getAlbums();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadingTask = null;

            if (curPhotos == null) {
                showAlbums();
            }
            else {
                showPhotos(curPhotos);
            }
        }

        @Override
        protected void onCancelled() {
            mLoadingTask = null;
            showProgress(false);
        }
    }
}
