package com.jaunesarmiento.rctmessaging;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.lang.Exception;

public class RCTMessagingModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String TAG = RCTMessagingModule.class.getName();

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private final ReactApplicationContext reactContext;

    public Promise promiseResolver;

    public RCTMessagingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "Messaging";
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // When sending intent is cancelled
        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            promiseResolver.resolve(true);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @ReactMethod
    public void canSendText(Promise promise) {
        // This always returns to true
        promise.resolve(true);
    }

    @ReactMethod
    public void sendText(ReadableMap options, Promise promise) {
        try {
            // This causes the app to crash due to multiple callbacks being registered
            // in the module. Fix this some time in the future.
            // new RCTMessagingObserver(reactContext, this, options).start();

            promiseResolver = promise;

            String body = options.hasKey("body") ? options.getString("body") : "";
            ReadableArray recipients = options.hasKey("recipients") ? options.getArray("recipients") : null;

            Intent intent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(reactContext);
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (defaultSmsPackage != null) {
                    intent.setPackage(defaultSmsPackage);
                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("vnd.android-dir/mms-sms");
            }

            intent.putExtra("sms_body", body);

            if (recipients != null) {
                // Samsung for some reason uses commas and not semicolons as a delimiter
                String separator = "; ";
                if (Build.MANUFACTURER.equalsIgnoreCase("Samsung")){
                    separator = ", ";
                }

                String recipientString = "";
                for (int i = 0; i < recipients.size(); i++) {
                    recipientString += recipients.getString(i);
                    recipientString += separator;
                }

                intent.putExtra("address", recipientString);
            }

            reactContext.startActivityForResult(intent, 0, intent.getExtras());
        } catch (Exception e) {
            promise.reject(e.getMessage());
        }
    }

}
