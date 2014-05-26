package com.entourage.app.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.entourage.app.MainActivity;
import com.entourage.app.R;
import com.entourage.app.profile.Profile;
import com.facebook.Session;
import com.facebook.SessionState;

import java.util.Arrays;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity
{
    private Button mFbAuthButton;
    private FbSessionStatusCallback mFbStatusCallback;
    private ProfileLoader mProfileLoader = null;
    private Profile mProfile;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProfile = Profile.get(getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
        if (mProfile != null) {
            Session.openActiveSessionFromCache(getApplicationContext());
            startMainActivity();
        }

        mFbStatusCallback = new FbSessionStatusCallback();
        mFbAuthButton = (Button) findViewById(R.id.fb_auth_button);
        mFbAuthButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAuthButtonClick();
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    protected void onAuthButtonClick()
    {
        mFbAuthButton.setEnabled(false);

        Session session = Session.getActiveSession();
        if (session == null) {
            session = new Session.Builder(this).build();
            session.addCallback(mFbStatusCallback);
            Session.setActiveSession(session);
        }
        if (!session.isOpened() && !session.isClosed()) {
            List<String> permissions = Arrays.asList("public_profile", "user_birthday",
                                                     "user_friends", "user_interests", "user_photos");
            session.openForRead(new Session.OpenRequest(this)
                    .setPermissions(permissions));
        }
        else {
            Session.openActiveSession(this, true, mFbStatusCallback);
        }
    }

    public void startMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Profile.EXTRA_USER_PROFILE, mProfile);
        startActivity(intent);
        finish();
    }

    private void loadProfile()
    {
        mProfileLoader = new ProfileLoader();
        mProfileLoader.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE)
        {
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private class FbSessionStatusCallback implements Session.StatusCallback
    {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                loadProfile();
            }
        }
    }

    public class ProfileLoader extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            mProfile = Profile.initFromFbApi(getSharedPreferences(Profile.PREFS_USER_PROFILE, 0), this);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Profile.EXTRA_USER_PROFILE, mProfile);
            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            startMainActivity();
        }

        public void updateProgress(int progress) {
            publishProgress(progress);
        }
    }
}
