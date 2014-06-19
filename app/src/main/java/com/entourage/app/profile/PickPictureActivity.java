package com.entourage.app.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.cengalabs.flatui.views.FlatTextView;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabrice on 5/10/14.
 * <p/>
 * Show facebook albums and allow the user to navigate and pick a picture from those albums
 */
public class PickPictureActivity extends Activity {
    /**
     * Async content loader
     */
    private ContentLoadingTask mLoadingTask = null;
    /**
     * Main progress bar (show when content loading)
     */
    private ProgressBar mProgressBar;
    /**
     * Album list view
     */
    private ListView mAlbumList;
    /**
     * Album photos grid view
     */
    private GridView mPhotoList;
    /**
     * Current album list
     * album[0] = id
     * album[1] = name
     * album[2] = number of photo
     * album[3] = cover picture
     */
    private List<String[]> mAlbums = null;
    /**
     * List view album adapter
     */
    private AlbumAdapter mAlbumAdapter;
    /**
     * Listener on albums click, open the album and show photos
     */
    private AlbumClickListener mAlbumClickListener;
    /**
     * Listener on photo click, select the photo and return it to the previous activity
     */
    private PhotoClickListener mPhotoClickListener;
    /**
     * Photo grid adapter
     */
    private PhotoAdapter mPhotoAdapter;
    /**
     * Error text view, show if no album found if (token revoked || token invalid || permission denied)
     */
    private FlatTextView mErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity transition test (arrive from right)
        overridePendingTransition(R.anim.activity_enter_from_left, R.anim.activity_leave_from_left);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.title_albums));
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // Get the views
        setContentView(R.layout.activity_pick_picture);
        mErrorTextView = (FlatTextView) findViewById(R.id.error_text_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mAlbumList = (ListView) findViewById(R.id.album_list);
        mPhotoList = (GridView) findViewById(R.id.photo_list);

        // Init album items
        mAlbumAdapter = new AlbumAdapter(this, new ArrayList<String[]>());
        mAlbumClickListener = new AlbumClickListener();
        mAlbumList.setAdapter(mAlbumAdapter);
        mAlbumList.setOnItemClickListener(mAlbumClickListener);

        // Init photo items
        mPhotoAdapter = new PhotoAdapter(this, new ArrayList<String[]>());
        mPhotoClickListener = new PhotoClickListener();
        mPhotoList.setAdapter(mPhotoAdapter);
        mPhotoList.setOnItemClickListener(mPhotoClickListener);

        mLoadingTask = new ContentLoadingTask();
        mLoadingTask.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Activity transition test (leave from right)
        this.overridePendingTransition(R.anim.activity_enter_from_right, R.anim.activity_leave_from_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mAlbumList.getVisibility() == View.VISIBLE) {
            // exit activity if action bar back action pressed on album list
            finish();
            // Activity transition test (leave from right)
            this.overridePendingTransition(R.anim.activity_enter_from_right, R.anim.activity_leave_from_right);
        } else {
            // back to album list otherwise
            showAlbums();
        }
        return true;

    }

    /**
     * Show the photo grid view (hide the album, progress and error views)
     *
     * @param photos Array of picture URL
     */
    public void showPhotos(List<String[]> photos) {
        mProgressBar.setVisibility(View.GONE);
        mAlbumList.setVisibility(View.GONE);

        mPhotoAdapter.clear();
        mPhotoAdapter.addAll(photos);
        mPhotoAdapter.notifyDataSetChanged();
        mPhotoList.setVisibility(View.VISIBLE);
    }

    /**
     * Show the album list view (hide the photos, progress and error views)
     */
    public void showAlbums() {
        showProgress(false);
        mAlbumAdapter.clear();
        if (mAlbums != null && !mAlbums.isEmpty()) {
            mErrorTextView.setVisibility(View.GONE);
            mAlbumAdapter.addAll(mAlbums);
        } else {
            mErrorTextView.setVisibility(View.VISIBLE);
        }
        mAlbumAdapter.notifyDataSetChanged();
    }

    /**
     * Show or hide the progress view (hide or show the album, photos and error views)
     */
    public void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAlbumList.setVisibility(View.GONE);
            mPhotoList.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mPhotoList.setVisibility(View.GONE);
            mAlbumList.setVisibility(View.VISIBLE);
            getActionBar().setTitle(getString(R.string.title_albums));
        }
    }

    /**
     * Get the Picture URL of a facebook photo object with its ID
     *
     * @param photoId Facebook API photo ID
     * @return
     */
    protected String getPictureUrlByPhotoId(String photoId) {
        final String[] curUrl = {""};
        Bundle params = new Bundle();
        params.putString("fields", "picture");

        new Request(
                Session.getActiveSession(),
                "/" + photoId,
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        GraphObject graphObj = response.getGraphObject();
                        JSONObject dataObj = graphObj.getInnerJSONObject();
                        try {
                            curUrl[0] = dataObj.getString("picture");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAndWait();
        if (curUrl.length > 0)
            return curUrl[0];
        return null;
    }

    /**
     * Get user facebook albums
     *
     * @return The album list (empty if none found and on errors)
     */
    protected List<String[]> getAlbums() {
        final List<String[]> albums = new ArrayList<String[]>();
        Bundle params = new Bundle();

        // TODO: Show a special album "Photo of me"
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
                    public void onCompleted(Response response) {
                        GraphObject graphObj = response.getGraphObject();
                        try {
                            JSONArray dataObj = graphObj.getInnerJSONObject().getJSONArray("data");
                            for (int i = 0; i < dataObj.length(); i++) {
                                JSONObject curObj = dataObj.getJSONObject(i);
                                String[] album = {
                                        curObj.getString("id"),
                                        curObj.getString("name"),
                                        curObj.getString("count"),
                                        getPictureUrlByPhotoId(curObj.getString("cover_photo"))
                                };
                                albums.add(album);
                            }
                        } catch (Exception e) {
                            // Nothing much to do, album list will be empty and the 'no album found'
                            // error will be displayed
                        }
                    }
                }
        ).executeAndWait();
        return albums;
    }

    /**
     * Get albums photos by album ID
     *
     * @param albumId The facebook album ID
     * @return A list of photos {{facebookId, url}, ...}
     */
    protected List<String[]> getPhotos(String albumId) {
        final List<String[]> photos = new ArrayList<String[]>();
        Bundle params = new Bundle();

        params.putString("fields", "images");
        new Request(
                Session.getActiveSession(),
                "/" + albumId + "/photos",
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        GraphObject graphObj = response.getGraphObject();
                        try {
                            JSONArray dataObj = graphObj.getInnerJSONObject().getJSONArray("data");
                            for (int i = 0; i < dataObj.length(); i++) {
                                JSONObject curObj = dataObj.getJSONObject(i);
                                try {
                                    String fitSrc = Profile.getBestFittedImage(curObj.getJSONArray("images"),
                                            Profile.DEFAULT_PICTURE_SIZE,
                                            Profile.DEFAULT_PICTURE_SIZE);
                                    if (fitSrc != null) {
                                        String[] photo = new String[]{
                                                curObj.getString("id"),
                                                fitSrc
                                        };
                                        photos.add(photo);
                                    }
                                } catch (IOException e) {
                                    // Nothing to do, skip
                                }
                            }
                        } catch (Exception e) {
                            // Nothing to do, skip
                        }
                    }
                }
        ).executeAndWait();
        return photos;
    }

    /**
     * Listen on album clicks to display the album photos
     */
    private class AlbumClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showProgress(true);
            String[] selectedAlbum = mAlbumAdapter.getItem(position);
            getActionBar().setTitle(selectedAlbum[1]);
            mLoadingTask = new ContentLoadingTask();
            mLoadingTask.execute(selectedAlbum[0]);
        }
    }

    /**
     * Listen on photo click
     */
    private class PhotoClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = getIntent();
            intent.putExtra("url", mPhotoAdapter.getItem(position)[1]);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Async load content from facebook API
     */
    private class ContentLoadingTask extends AsyncTask<String, Void, Boolean> {
        private List<String[]> curPhotos;

        @Override
        protected Boolean doInBackground(String... params) {
            curPhotos = null;
            if (params != null && params.length > 0) {
                curPhotos = getPhotos(params[0]);
            } else {
                mAlbums = getAlbums();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadingTask = null;

            if (curPhotos == null) {
                showAlbums();
            } else {
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
