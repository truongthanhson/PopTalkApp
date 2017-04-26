package com.poptech.poptalk.login;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.poptech.poptalk.R;
import com.poptech.poptalk.collections.CollectionsActivity;
import com.poptech.poptalk.utils.AnimationUtils;

import java.io.File;

/**
 * Created by sontt on 04/03/2017.
 */

public class LoginFragment extends Fragment implements LoginContract.View, RippleView.OnRippleCompleteListener {

    private TextView mUserName;

    private EditText mPassword;

    private RippleView mLoginButton;

    private View mView;

    private static final String TAG = "LoginActivity";
    private LoginContract.Presenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
//            String pathPhoto = Environment.getExternalStorageDirectory() + Constants.PATH_APP + Constants.PATH_PHOTO;
//            String pathSound = Environment.getExternalStorageDirectory() + Constants.PATH_APP + Constants.PATH_SOUND;
//            File filePhoto = new File(pathPhoto);
//            File fileSound = new File(pathSound);
//            if (!filePhoto.exists()) {
//                Utils.forceMkdir(filePhoto);
//            }
//            if (!fileSound.exists()) {
//                Utils.forceMkdir(fileSound);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        SaveData.getInstance(getActivity()).setLoggedIn(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login_layout,container,false);
        mUserName = (TextView) mView.findViewById(R.id.user_name_id);
        mPassword = (EditText) mView.findViewById(R.id.password_id);
        mLoginButton = (RippleView) mView.findViewById(R.id.login_ripple_id);
        mLoginButton.setOnRippleCompleteListener(this);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onComplete(RippleView rippleView) {
        mPresenter.login(mUserName.getText().toString(), mPassword.getText().toString());
    }
    @Override
    public void showErrorPassword(String error) {
        mPassword.setError(getString(R.string.login_error_password));
        AnimationUtils.shake(getActivity().getApplicationContext(), mPassword);
    }

    @Override
    public void showErrorUsername(String error) {
        mUserName.setError(getString(R.string.login_error_username));
        AnimationUtils.shake(getActivity().getApplicationContext(), mUserName);
    }

    @Override
    public void onLoginSuccessful() {
        Intent intent = new Intent(getActivity(), CollectionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onLoginFailed() {
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
