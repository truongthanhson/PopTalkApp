package com.poptech.poptalk.login;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
    private static final String TAG = "LoginActivity";

    private AutoCompleteTextView mUserName;

    private AutoCompleteTextView mEmail;

    private Button mLoginButton;

    private View mView;

    private LoginButton mFacebookLogin;

    private CallbackManager mCallbackManager;

    private LoginContract.Presenter mPresenter;

    private RelativeLayout mLanguageSpinner;

    private TextView mLanguage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login_layout, container, false);
        mUserName = (AutoCompleteTextView) mView.findViewById(R.id.username);
        mEmail = (AutoCompleteTextView) mView.findViewById(R.id.useremail);
        mLoginButton = (Button) mView.findViewById(R.id.login_button_id);
        mLanguageSpinner = (RelativeLayout) mView.findViewById(R.id.language_spinner_id);
        mLanguageSpinner.setOnClickListener(this);
        mLanguage = (TextView) mView.findViewById(R.id.user_language_id);
        mLanguage.setText(SaveData.getInstance(getActivity()).getLanguage());
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
        SaveData.getInstance(getActivity()).setLanguage(mLanguage.getText().toString());
        Intent intent = new Intent(getActivity(), CollectionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showErrorEmail(String error) {
        mEmail.setError(error);
        AnimationUtils.shake(getActivity().getApplicationContext(), mEmail);
    }

    @Override
    public void showErrorUsername(String error) {
        mUserName.setError(error);
        AnimationUtils.shake(getActivity().getApplicationContext(), mUserName);
    }

    @Override
    public void showErrorLanguage(String error) {
        mLanguage.setError(error);
        AnimationUtils.shake(getActivity().getApplicationContext(), mLanguage);
    }

    @Override
    public void onLoginSuccessful() {
        Credentials credentials = new Credentials();
        credentials.setEmail(mEmail.getText().toString());
        credentials.setName(mUserName.getText().toString());
        mPresenter.updateCredentials(credentials);
        onOpenCollectionActivity();
    }

    @Override
    public void onLoginFailed() {
    }

    @Override
    public void onLanguageLoaded(List<String> languages) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.PopTalk_AlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.item_listview_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Select Language");
        alertDialog.setIcon(R.drawable.ic_language);
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        final AlertDialog dialog = alertDialog.create();

        ListView listView = (ListView) convertView.findViewById(R.id.list_view_id);
        AutoCompleteTextView searchText = (AutoCompleteTextView) convertView.findViewById(R.id.search_id);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_language_layout, R.id.language_name, languages);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String language = (String) parent.getItemAtPosition(position);
                mLanguage.setText(language);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button_id:
                mPresenter.login(mUserName.getText().toString(), mEmail.getText().toString(), mLanguage.getText().toString());
                break;
            case R.id.language_spinner_id:
                mPresenter.getLanguages();
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
