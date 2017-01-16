package com.jaunesarmiento.rctmessaging;

import android.content.Context;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.net.Uri;
import android.database.Cursor;
import android.os.Looper;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;

import java.util.HashMap;
import java.util.Map;

public class RCTMessagingObserver extends ContentObserver {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Uri uri = Uri.parse("content://sms/");

    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_TYPE = "type";
    private static final String[] PROJECTION = { COLUMN_ADDRESS, COLUMN_TYPE };
    private static final int MESSAGE_TYPE_ALL = 0;
    private static final int MESSAGE_TYPE_INBOX = 1;
    private static final int MESSAGE_TYPE_SENT = 2;
    private static final int MESSAGE_TYPE_DRAFT = 3;
    private static final int MESSAGE_TYPE_OUTBOX = 4;
    private static final int MESSAGE_TYPE_FAILED = 5; // failed outgoing messages
    private static final int MESSAGE_TYPE_QUEUED = 6; // queued to send later

    private RCTMessagingModule module;
    private ContentResolver resolver = null;
    private String[] successTypes;
    private Map<String, Integer> types;

    public RCTMessagingObserver(Context context, RCTMessagingModule module, ReadableMap options) {
        super(handler);

        types = new HashMap<>();
        types.put("all", MESSAGE_TYPE_ALL);
        types.put("inbox", MESSAGE_TYPE_INBOX);
        types.put("sent", MESSAGE_TYPE_SENT);
        types.put("draft", MESSAGE_TYPE_DRAFT);
        types.put("outbox", MESSAGE_TYPE_OUTBOX);
        types.put("failed", MESSAGE_TYPE_FAILED);
        types.put("queued", MESSAGE_TYPE_QUEUED);

        this.successTypes = new String[] { "sent", "queued" };
        this.module = module;
        this.resolver = context.getContentResolver();
    }

    public void start() {
        if (resolver != null) {
            resolver.registerContentObserver(uri, true, this);
        }
        else {
            throw new IllegalStateException("Current RCTMessagingObserver instance is invalid");
        }
    }

    public void stop() {
        if (resolver != null) {
            resolver.unregisterContentObserver(this);
        }
    }

    public void onMessageSuccess() {
        module.promiseResolver.resolve(true);
    }

    public void onMessageFailed() {
        module.promiseResolver.reject("Failed to send text message.");
    }

    @Override
    public void onChange(boolean selfChange) {

        Cursor cursor = null;

        try {
            cursor = resolver.query(uri, PROJECTION, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));

                System.out.println("onChange() type: " + type);

                // loop through provided success types
                boolean successful = false;
                for (int i = 0; i < successTypes.length; i++) {
                    if (type == types.get(successTypes[i])) {
                        successful = true;
                        break;
                    }
                }

                if (successful) {
                    onMessageSuccess();
                } else {
                    onMessageFailed();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
