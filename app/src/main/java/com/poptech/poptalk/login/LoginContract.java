package com.poptech.poptalk.login;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.Credentials;

/**
 * Created by sontt on 04/03/2017.
 */

public class LoginContract {

    interface View extends BaseView<Presenter> {
        void showErrorEmail(String error);

        void showErrorUsername(String error);

        void onLoginSuccessful();

        void onLoginFailed();
    }

    interface Presenter extends BasePresenter {
        void login(String userName, String email);

        void updateCredentials(Credentials credentials);
    }
}
