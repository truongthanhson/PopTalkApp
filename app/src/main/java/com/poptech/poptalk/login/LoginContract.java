package com.poptech.poptalk.login;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.Credentials;

import java.util.List;

/**
 * Created by sontt on 04/03/2017.
 */

public class LoginContract {

    interface View extends BaseView<Presenter> {
        void showErrorEmail(String error);

        void showErrorUsername(String error);

        void showErrorLanguage(String error);

        void onLoginSuccessful();

        void onLoginFailed();

        void onLanguageLoaded(List<String> languages);
    }

    interface Presenter extends BasePresenter {
        void login(String userName, String email, String language);

        void getLanguages();

        void updateCredentials(Credentials credentials);
    }
}
