package com.poptech.poptalk.login;

import android.content.Context;

import com.poptech.poptalk.*;

import javax.inject.Inject;

/**
 * Created by sontt on 04/03/2017.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View mView;
    @Inject
    Context mContext;

    @Inject
    public LoginPresenter(LoginContract.View view) {
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
            mView.showErrorUsernam(mContext.getString(R.string.login_error_username));
        } else if (password.isEmpty()) {
            mView.showErrorPassword(mContext.getString(R.string.login_error_password));
        } else {
            mView.onLoginSuccessful();
        }
    }

    @Override
    public void start() {

    }
}
