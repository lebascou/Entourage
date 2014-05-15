package com.entourage.app.profile;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.entourage.app.MainActivity;
import com.entourage.app.R;

/**
 * Created by Fabrice on 5/10/14.
 */
public class FragmentProfile extends Fragment
{
    private Profile mProfile;
    private ImageView mProfilePicture;
    private TextView mTextFirstName;
    private TextView mTextAge;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

        Bundle bundle = this.getArguments();
        mProfile = (Profile) bundle.getSerializable(MainActivity.EXTRA_USER_PROFILE);

        mProfilePicture = (ImageView) mRootView.findViewById(R.id.profile_mainPicture);
        mTextFirstName = (TextView) mRootView.findViewById(R.id.profile_FirstName);
        mTextAge = (TextView) mRootView.findViewById(R.id.profile_Age);

        mProfilePicture.setImageBitmap(mProfile.getProfilePicture(0));
        mTextFirstName.setText(mProfile.getFirstName() + ", " + mProfile.getAge() + " " + getString(R.string.age_suffix));
        mTextAge.setVisibility(View.INVISIBLE);
        //mTextFirstName.setText(mProfile.getFirstName());
        //mTextAge.setText(mProfile.getAge() + " " + getString(R.string.age_suffix));

        setProfileImageToView(R.id.profile_picture1, mProfile.getProfilePicture(1));
        setProfileImageToView(R.id.profile_picture2, mProfile.getProfilePicture(2));
        setProfileImageToView(R.id.profile_picture3, mProfile.getProfilePicture(3));
        setProfileImageToView(R.id.profile_picture4, mProfile.getProfilePicture(4));
        return mRootView;
    }

    public void setProfileImageToView(int viewId, Bitmap bmp)
    {
        ImageView view = (ImageView) mRootView.findViewById(viewId);
        if (bmp == null)
            view.setImageResource(R.drawable.ic_action_new_picture);
        else
            view.setImageBitmap(bmp);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }
}
