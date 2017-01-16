package com.jaunesarmiento.rctmessaging;

import android.content.Intent;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RCTMessagingPackage implements ReactPackage {

    private RCTMessagingModule module = null;
    private static RCTMessagingPackage instance = null;

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        module = new RCTMessagingModule(reactApplicationContext);
        if (instance == null) {
            instance = this;
        }
        return Arrays.<NativeModule>asList(module);
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        module.onActivityResult(null, requestCode, resultCode, data);
    }

}
