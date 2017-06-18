package com.poptech.poptalk.login;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Credentials;
import com.poptech.poptalk.collections.CollectionsActivity;
import com.poptech.poptalk.utils.AnimationUtils;
import com.poptech.poptalk.utils.SaveData;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sontt on 04/03/2017.
 */

public class LoginFragment extends Fragment implements LoginContract.View, View.OnClickListener, FacebookCallback<LoginResult> {

    private TextView mUserName;

    private EditText mEmail;

    private Button mLoginButton;

    private View mView;

    private LoginButton mFacebookLogin;

    private CallbackManager mCallbackManager;

    private static final String TAG = "LoginActivity";
    private LoginContract.Presenter mPresenter;

    private MaterialSpinner mLanguageSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login_layout, container, false);
        mUserName = (TextView) mView.findViewById(R.id.user_name_id);
        mEmail = (EditText) mView.findViewById(R.id.user_email_id);
        mLoginButton = (Button) mView.findViewById(R.id.login_button_id);
        mLanguageSpinner = (MaterialSpinner) mView.findViewById(R.id.language_spinner_id);
//        mLanguageSpinner.setItems(ANDROID_VERSIONS);
        mLanguageSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
            }
        });
        mLanguageSpinner.setOnNothingSelectedListener(new MaterialSpinner.OnNothingSelectedListener() {

            @Override
            public void onNothingSelected(MaterialSpinner spinner) {
            }
        });
        mLoginButton.setOnClickListener(this);
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookLogin = (LoginButton) mView.findViewById(R.id.facebook_login_id);
        mFacebookLogin.setReadPermissions(Arrays.asList("email"));
        mFacebookLogin.registerCallback(mCallbackManager, this);
        mFacebookLogin.setFragment(this);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onOpenCollectionActivity() {
        SaveData.getInstance(getActivity()).setLoggedIn(true);
        Intent intent = new Intent(getActivity(), CollectionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showErrorEmail(String error) {
        mEmail.setError(getString(R.string.login_error_email));
        AnimationUtils.shake(getActivity().getApplicationContext(), mEmail);
    }

    @Override
    public void showErrorUsername(String error) {
        mUserName.setError(getString(R.string.login_error_username));
        AnimationUtils.shake(getActivity().getApplicationContext(), mUserName);
    }

    @Override
    public void showErrorLanguage(String error) {
        mLanguageSpinner.requestFocus();
        mLanguageSpinner.setError(error);
        AnimationUtils.shake(getActivity().getApplicationContext(), mLanguageSpinner);
    }

    @Override
    public void onLoginSuccessful() {
        Credentials credentials = new Credentials();
        credentials.setEmail(mEmail.getText().toString());
        credentials.setName(mUserName.getText().toString());
        mPresenter.updateCredentials(credentials);
        SaveData.getInstance(getActivity()).setLanguage(mLanguageSpinner.getText().toString());
        onOpenCollectionActivity();
    }

    @Override
    public void onLoginFailed() {
    }

    @Override
    public void onLanguageLoaded(List<String> languages) {
        languages.add(0, "");
        mLanguageSpinner.setItems(languages);
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button_id:
                mPresenter.login(mUserName.getText().toString(), mEmail.getText().toString(), mLanguageSpinner.getText().toString());
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        final AccessToken accessToken = loginResult.getAccessToken();

        GraphRequestAsyncTask request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                Credentials credentials = new Credentials();
                if (user.has("email")) {
                    credentials.setEmail(user.optString("email"));
                }
                if (user.has("name")) {
                    credentials.setName(user.optString("name"));
                }
                if (user.has("id")) {
                    credentials.setProfilePicture("https://graph.facebook.com/" + user.optString("id") + "/picture?type=large");
                }
                mPresenter.updateCredentials(credentials);
                onOpenCollectionActivity();
            }
        }).executeAsync();

    }

    @Override
    public void onCancel() {
        Toast.makeText(getActivity(), "Facebook permission is not granted", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(FacebookException error) {
        Toast.makeText(getActivity(), "Facebook login error " + error.toString(), Toast.LENGTH_LONG).show();
    }
}
