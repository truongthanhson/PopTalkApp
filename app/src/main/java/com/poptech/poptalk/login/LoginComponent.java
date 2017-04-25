package com.poptech.poptalk.login;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;

/**
 * Created by sontt on 04/03/2017.
 */
@FragmentScoped
@Component(dependencies = AppComponent.class,modules = LoginPresenterModule.class)
public interface LoginComponent {
    void inject(LoginActivity loginActivity);
}
