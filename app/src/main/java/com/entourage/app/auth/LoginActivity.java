package com.entourage.app.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.entourage.app.MainActivity;
import com.entourage.app.R;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity
{
    public static final String EXTRA_USER_SESSION = "extra_user_session_facebook";

    // Fb Client
    private Session.StatusCallback fbStatusCallback;
    private UiLifecycleHelper fbUiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup facebook login
        fbStatusCallback = new FbSessionStatusCallback();
        fbUiHelper = new UiLifecycleHelper(this, fbStatusCallback);
        fbUiHelper.onCreate(savedInstanceState);
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        LoginButton fbAuthButton = (LoginButton) findViewById(R.id.fbAuthButton);
        fbAuthButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends", "user_interests", "user_photos"));
    }

    private class FbSessionStatusCallback implements Session.StatusCallback
    {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                processFacebookLogin(session);
            }
        }
    }

    public void processFacebookLogin(Session session)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_USER_SESSION, session);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            processFacebookLogin(session);
        }
        else {
            fbUiHelper.onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        fbUiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fbUiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fbUiHelper.onSaveInstanceState(outState);
    }
}
