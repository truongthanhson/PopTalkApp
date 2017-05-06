package com.poptech.poptalk.login;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.Credentials;
import com.poptech.poptalk.provider.PopTalkDatabase;

import javax.inject.Inject;

/**
 * Created by sontt on 26/04/2017.
 */

public class LoginModel implements BaseModel {
    private PopTalkDatabase mDatabase;

    @Inject
    public LoginModel(PopTalkDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    public Credentials getUser(){
        return new Credentials("1","1");
    }

}
