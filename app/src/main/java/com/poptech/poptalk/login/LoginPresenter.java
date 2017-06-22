package com.poptech.poptalk.login;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Credentials;
import com.poptech.poptalk.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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
    public void login(String userName, String email, String language) {
        if (userName.isEmpty()) {
            mView.showErrorUsername(mContext.getString(R.string.login_error_username));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mView.showErrorEmail(mContext.getString(R.string.login_error_email));
        } else if (TextUtils.isEmpty(language)) {
            mView.showErrorLanguage(mContext.getString(R.string.login_error_language));
        }else {
            mView.onLoginSuccessful();
        }
    }

    @Override
    public void updateCredentials(Credentials credentials) {
        if (mModel.isCredentialsExisted()) {
            Credentials existedCredentials = mModel.getCredentials();
            // User existed
            if (!StringUtils.isNullOrEmpty(credentials.getEmail()) &&
                    existedCredentials.getEmail().equalsIgnoreCase(credentials.getEmail())) {
                if (!StringUtils.isNullOrEmpty(credentials.getName())) {
                    existedCredentials.setName(credentials.getName());
                }
                if (!StringUtils.isNullOrEmpty(credentials.getEmail())) {
                    existedCredentials.setEmail(credentials.getEmail());
                }
                if (!StringUtils.isNullOrEmpty(credentials.getPhone())) {
                    existedCredentials.setPhone(credentials.getPhone());
                }
                if (!StringUtils.isNullOrEmpty(credentials.getPassword())) {
                    existedCredentials.setPassword(credentials.getPassword());
                }
                if (!StringUtils.isNullOrEmpty(credentials.getProfilePicture())) {
                    existedCredentials.setProfilePicture(credentials.getProfilePicture());
                }
                mModel.updateCredentials(existedCredentials);
            } else {
                mModel.updateCredentials(credentials);
            }
        } else {
            mModel.addNewCredentials(credentials);
        }
    }

    @Override
    public void getLanguages() {
        Locale[] locales = Locale.getAvailableLocales();
        List<String> languages = new ArrayList<>();
        for (Locale l : locales) {
            String language = l.getDisplayLanguage();
            if (!StringUtils.isNullOrEmpty(language)) {
                String[] regexChars = {"\\s+", "\\s*-\\s*", "\\s*'\\s*"};
                String space = " ";
                for (String regex : regexChars) {
                    language = language.replaceAll(regex, space);
                }
                language = language.replaceAll("^\\s+", "");
                language = language.replaceAll("\\s+$", "");
                languages.add(language);
            }

        }
        List<String> sortedLanguages = new ArrayList<>(new HashSet<>(languages));
        Collections.sort(sortedLanguages, String.CASE_INSENSITIVE_ORDER);
        mView.onLanguageLoaded(sortedLanguages);
    }

    @Override
    public void start() {

    }
}
