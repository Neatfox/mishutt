package com.neatfox.mishutt.notification;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.gotev.uploadservice.BuildConfig;
import net.gotev.uploadservice.UploadServiceConfig;

import java.util.Objects;

public class App extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "mishutt_upload";

    private  void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "ANDROID",
                    NotificationManager.IMPORTANCE_HIGH);
            Objects.requireNonNull(mNotificationManager).createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        UploadServiceConfig.initialize(
                this,
                NOTIFICATION_CHANNEL_ID,
                BuildConfig.DEBUG
        );
    }
}
