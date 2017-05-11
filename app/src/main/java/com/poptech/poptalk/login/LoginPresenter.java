package com.poptech.poptalk.login;

import android.content.Context;

import com.poptech.poptalk.*;
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
    public void login(String userName, String password) {
        if (userName.isEmpty()) {
            mView.showErrorUsername(mContext.getString(R.string.login_error_username));
        } else if (password.isEmpty()) {
            mView.showErrorPassword(mContext.getString(R.string.login_error_password));
        } else {
            if (mModel.getCredentials().getName().equalsIgnoreCase(userName) && mModel.getCredentials().getPassword().equalsIgnoreCase(password)) {
                mView.onLoginSuccessful();
            } else {
                mView.onLoginFailed();
            }
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
