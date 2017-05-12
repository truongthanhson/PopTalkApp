package com.poptech.poptalk.login;

import android.content.Context;
import android.util.Patterns;

import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Credentials;

import javax.inject.Inject;

/**
 * Created by sontt on 04/03/2017.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View mView;

    @Inject
    Context mContext;

    private LoginModel mModel;

    @Inject
    public LoginPresenter(LoginModel model, LoginContract.View view) {
        this.mModel = model;
        this.mView = view;
    }

    /**
     * Method injection is used here to safely reference {@code this} after the object is created.
     * For more information, see Java Concurrency in Practice.
     */
    @Inject
    void setupListeners() {
        mView.setPresenter(this);
    }

    @Override
    public void login(String userName, String email) {
        if (userName.isEmpty()) {
            mView.showErrorUsername(mContext.getString(R.string.login_error_username));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mView.showErrorEmail(mContext.getString(R.string.login_error_email));
        } else {
            mView.onLoginSuccessful();
        }
    }

    @Override
    public void updateCredentials(Credentials credentials) {
        if (mModel.isCredentialsExisted()) {
            mModel.updateCredentials(credentials);
        } else {
            mModel.addNewCredentials(credentials);
        }
    }

    @Override
    public void start() {

    }
}
