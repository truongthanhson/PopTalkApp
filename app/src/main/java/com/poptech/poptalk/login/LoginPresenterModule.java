package com.poptech.poptalk.login;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sontt on 04/03/2017.
 */

@Module
public class LoginPresenterModule {
    private final LoginContract.View mView;

    public LoginPresenterModule(LoginContract.View view) {
        this.mView = view;
    }

    @Provides
    public LoginContract.View providesLoginView(){
        return mView;
    }
}
