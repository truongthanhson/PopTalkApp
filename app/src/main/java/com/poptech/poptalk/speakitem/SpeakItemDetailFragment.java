package com.poptech.poptalk.speakitem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItemDetailFragment extends Fragment {

    public static SpeakItemDetailFragment newInstance(int speakItemId) {
        Bundle args = new Bundle();
        args.putInt("speak_item_id",speakItemId);
        SpeakItemDetailFragment fragment = new SpeakItemDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private int mSpeakItemId = -1;
    private View mMainView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpeakItemId = getArguments().getInt("speak_item_id");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView = new TextView(getActivity());
        ((TextView)mMainView).setText(mSpeakItemId + "");
        return mMainView;
    }
}
