package com.entourage.app.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * <p/>
 * Profile fragment, show user profile and public informations
 * Allow user to edit tag line and profile pictures
 */
public class FragmentProfile extends Fragment {
    /**
     * Profile model instance
     * Initialized from the bundle
     */
    private Profile mProfile;
    /**
     * TextView - user first name (and age if available)
     */
    private TextView mTextFirstName;
    /**
     * Root view of the fragment
     */
    private View mRootView;
    /**
     * User profile public tagline (editable)
     */
    private EditText mTagLineTextView;
    /**
     * Circle transformation for Picasso image loader
     * Saved in class for reuse
     */
    private CircleTransform mCircleTransformer;
    /**
     * OnClick listener for the profile images
     * Allow the user to swap images and reorder them
     */
    private ProfileImageClickListener mImageClickListener;
    /**
     * ImageView containing the user profile pictures
     * mImgView[i] <=> Profile.profilePicture[i]
     */
    private ArrayList<ImageView> mImgViews;
    /**
     * Keep track of the last image selected for a potential swap action
     */
    private ImageView mLastImgSelected = null;
    /**
     * Picasso instance to load images async
     */
    private Picasso mPicasso;
    /**
     * Remove profile picture action (shows only when one picture is selected)
     */
    private MenuItem mActionDeletePicture = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Allow the fragment to create a menu with the onCreateOptionsMenu method
        setHasOptionsMenu(true);

        // Load the profile from bundle
        Bundle bundle = this.getArguments();
        mProfile = (Profile) bundle.getSerializable(Profile.EXTRA_USER_PROFILE);

        // Get the views
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mTagLineTextView = (EditText) mRootView.findViewById(R.id.editText);
        mTagLineTextView.setText(mProfile.getBio());
        mTagLineTextView.addTextChangedListener(new TagLineEventListener());
        mTextFirstName = (TextView) mRootView.findViewById(R.id.profile_FirstNameAge);
        mTextFirstName.setText(mProfile.getFirstName() + ", " + mProfile.getAge() + " " + getString(R.string.age_suffix));

        mImgViews = new ArrayList<ImageView>();
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_mainPicture));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture1));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture2));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture3));
        mImgViews.add((ImageView) mRootView.findViewById(R.id.profile_picture4));

        // Init the image loader and load profile pictures
        mPicasso = Picasso.with(getActivity());
        mImageClickListener = new ProfileImageClickListener();
        mCircleTransformer = new CircleTransform();
        for (int i = 0; i < mImgViews.size(); i++) {
            setProfileImageUrlToView(mImgViews.get(i), mProfile.getProfilePicture(i), Profile.DEFAULT_PICTURE_SIZE);
            mImgViews.get(i).setOnClickListener(mImageClickListener);
            //TODO: Future update -> reorder images by dragging them at the desired position
            //mImgViews.get(i).setOnLongClickListener(mImageClickListener);
        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

    /**
     * Result from the Pick picture from facebook activity
     * If a picture is picked, add it to the first available picture slot
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == requestCode) {
            String url = data.getStringExtra("url");
            for (int i = 0; i < mImgViews.size(); i++) {
                if (mProfile.getProfilePicture(i) == null) {
                    mProfile.addPicture(url);
                    mImgViews.get(i).setSelected(false);
                    setProfileImageUrlToView(mImgViews.get(i), url, Profile.DEFAULT_PICTURE_SIZE);
                    mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
                    break;
                }
            }
        }
    }

    /**
     * Remove the selected profile picture from the user profile
     */
    public void removeSelectedProfilePicture() {
        showDeletePictureAction(false);
        if (mLastImgSelected != null) {
            int index = mImgViews.indexOf(mLastImgSelected);
            mProfile.removePicture(index);
            mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
            sortProfilePictures();
        }
    }

    /**
     * Bind a picture URL to an ImageView using Picasso for asyn loading and caching
     * The picture is currently cropped in its center and circled
     *
     * @param imgView The ImageView container
     * @param url     The picture URL
     * @param minSize Minimum size to resize the picture for the picasso centerCrop action
     */
    public void setProfileImageUrlToView(ImageView imgView, String url, int minSize) {
        imgView.setSelected(false);
        if (url == null || url.isEmpty()) {
            imgView.setImageResource(R.drawable.ic_add);
        } else {
            mPicasso.load(url)
                    .resize(minSize, minSize)
                    .centerCrop()
                    .transform(mCircleTransformer)
                    .placeholder(R.drawable.picture_loading_animation)
                    .into(imgView);
            if (imgView == mImgViews.get(0)) {
                // Main picture changed, it also changes the picture in the main nav drawer
                ((MainActivity) getActivity()).setDrawerPicture(url);
            }
        }
    }

    /**
     * Move all profile picture to the left (avoid blank in the middle of the picture list)
     */
    public void sortProfilePictures() {
        mLastImgSelected = null;
        for (int i = 1; i < mImgViews.size(); i++) {
            setProfileImageUrlToView(mImgViews.get(i), mProfile.getProfilePicture(i), Profile.DEFAULT_PICTURE_SIZE);
        }
    }

    /**
     * Swap two profile pictures
     * This method can be called even when only one picture is selected so a check is required
     * on the lastSelected and currentSelected
     *
     * @param curSelected Current selected picture
     * @param curIdx      Current selected picture index
     */
    public void swapProfilePictures(ImageView curSelected, int curIdx) {
        curSelected.setSelected(!curSelected.isSelected());
        if (mLastImgSelected == null) {
            if (mImgViews.indexOf(curSelected) > 0) // hide the main picture delete action
                showDeletePictureAction(true);
            mLastImgSelected = curSelected;
        } else {
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

    /**
     * Set the delete picture action (actionBar) visibility
     *
     * @param visible
     */
    public void showDeletePictureAction(boolean visible) {
        mActionDeletePicture.setVisible(visible);
    }

    /**
     * Listen for change events on the Tag line
     * TODO: Should do a more clever save (instead of saving every time the text is changed)
     */
    private class TagLineEventListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mProfile.setTagLine(s.toString());
            mProfile.saveLocal(getActivity().getSharedPreferences(Profile.PREFS_USER_PROFILE, 0));
        }
    }

    /**
     * Listen for click on the profile picture views
     * Call the swap or show the delete action if necessary
     */
    private class ProfileImageClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ImageView curSelected = (ImageView) v;
            int curIdx = mImgViews.indexOf(curSelected);

            if (mProfile.getProfilePicture(curIdx) == null) {
                showDeletePictureAction(false);
                Intent intent = new Intent(getActivity(), PickPictureActivity.class);
                startActivityForResult(intent, Activity.RESULT_OK);
            } else {
                swapProfilePictures(curSelected, curIdx);
            }
        }
    }
}
