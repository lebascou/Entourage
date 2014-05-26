package com.entourage.app.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.entourage.app.CircleTransform;
import com.entourage.app.MainActivity;
import com.entourage.app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Fabrice on 5/10/14.
 */
public class FragmentProfile extends Fragment
{
    public static final int RESULT_PICTURE_SELECTED = 1;
    private Profile mProfile;
    private TextView mTextFirstName;
    private View mRootView;
    private EditText mBioTextView;
    private CircleTransform mCircleTransformer;
    private ProfileImageClickListener mImageClickListener;
    private ArrayList<ImageView> mImgViews;
    private ImageView mLastImgSelected = null;
    private Picasso mPicasso;
    private MenuItem mActionDeletePicture = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

        Bundle bundle = this.getArguments();
        mProfile = (Profile) bundle.getSerializable(Profile.EXTRA_USER_PROFILE);
        mBioTextView = (EditText) mRootView.findViewById(R.id.editText);
        mBioTextView.setText(mProfile.getBio());
        mBioTextView.addTextChangedListener(new BioEventListener());
        mTextFirstName = (TextView) mRootView.findViewById(R.id.profile_FirstNameAge);
        mTextFirstName.setText(mProfile.getFirstName() + ", " + mProfile.getAge() + " " + getString(R.string.age_suffix));

        mPicasso = Picasso.with(getActivity());
        mPicasso.setDebugging(false);
        mImageClickListener = new ProfileImageClickListener();
        mCircleTransformer = new CircleTransform();

        mImgViews = new ArrayList<ImageView>();
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_mainPicture));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture1));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture2));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture3));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture4));

        for (int i = 0; i < mImgViews.size(); i++)
        {
            setProfileImageUrlToView(mImgViews.get(i), mProfile.getProfilePicture(i), Profile.DEFAULT_PICTURE_SIZE);
            mImgViews.get(i).setOnClickListener(mImageClickListener);
            //mImgViews.get(i).setOnLongClickListener(mImageClickListener);
        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.profile, menu);
        mActionDeletePicture = menu.findItem(R.id.action_delete_picture);
        mActionDeletePicture.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeSelectedProfilePicture();
                return true;
            }
        });
        showDeletePictureAction(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == requestCode) {
            String url = data.getStringExtra("url");
            for (int i = 0; i < mImgViews.size(); i++)
            {
                if (mProfile.getProfilePicture(i) == null)
                {
                    mProfile.addPicture(url);
                    mImgViews.get(i).setSelected(false);
                    setProfileImageUrlToView(mImgViews.get(i), url, Profile.DEFAULT_PICTURE_SIZE);
                    mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
                    break;
                }
            }
        }
    }

    public void setProfileImageUrlToView(ImageView imgView, String url, int maxSize)
    {
        imgView.setSelected(false);
        if (url == null || url.isEmpty())
        {
            imgView.setImageResource(R.drawable.ic_add);
        }
        else
        {
            mPicasso.load(url)
               .resize(maxSize, maxSize)
               .centerCrop()
               .transform(mCircleTransformer)
               .placeholder(R.drawable.picture_loading_animation)
               .into(imgView);
            if (imgView == mImgViews.get(0))
                ((MainActivity) getActivity()).setDrawerPicture(url);
        }
    }

    public void removeSelectedProfilePicture()
    {
        showDeletePictureAction(false);
        if (mLastImgSelected != null)
        {
            int index = mImgViews.indexOf(mLastImgSelected);
            mProfile.removePicture(index);
            mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
            resortProfilePictures();
        }
    }

    public void resortProfilePictures()
    {
        mLastImgSelected = null;
        for (int i = 1; i < mImgViews.size(); i++) {
            setProfileImageUrlToView(mImgViews.get(i), mProfile.getProfilePicture(i), Profile.DEFAULT_PICTURE_SIZE);
        }
    }


    public void swapProfilePictures(ImageView curSelected, int curIdx)
    {
        curSelected.setSelected(!curSelected.isSelected());
        if (mLastImgSelected == null) {
            if (mImgViews.indexOf(curSelected) > 0) // impossible to delete main picture
                showDeletePictureAction(true);
            mLastImgSelected = curSelected;
        }
        else
        {
            showDeletePictureAction(false);
            int lastIdx = mImgViews.indexOf(mLastImgSelected);

            setProfileImageUrlToView(curSelected, mProfile.getProfilePicture(lastIdx), Profile.DEFAULT_PICTURE_SIZE);
            setProfileImageUrlToView(mLastImgSelected, mProfile.getProfilePicture(curIdx), Profile.DEFAULT_PICTURE_SIZE);

            mProfile.swapProfilePicture(curIdx, lastIdx);
            mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));

            mLastImgSelected.setSelected(false);
            mLastImgSelected = null;
            curSelected.setSelected(false);
        }
    }

    public void showDeletePictureAction(boolean visible)
    {
        mActionDeletePicture.setVisible(visible);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    private class BioEventListener implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mProfile.setBio(s.toString());
            mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
        }
    }

    private class ProfileImageClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            ImageView curSelected = (ImageView) v;
            int curIdx = mImgViews.indexOf(curSelected);

            if (mProfile.getProfilePicture(curIdx) == null)
            {
                showDeletePictureAction(false);
                Intent intent = new Intent(getActivity(), PickPictureActivity.class);
                startActivityForResult(intent, RESULT_PICTURE_SELECTED);
            }
            else
            {
                swapProfilePictures(curSelected, curIdx);
            }
        }
    }
}
