package com.example.r_upgrade.common;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class UpgradeNotification {
    public static final String TAG = "UpgradeNotification";

    private static final String CHANNEL_NAME = "r_upgrade_notification";

    static void createNotification(Context context, int id, String title, int current_length, int max_length, String planTime, int status) {
        if (status == DownloadStatus.STATUS_CANCEL.getValue()) {
            removeNotification(context, id);
            return;
        }
        Notification notification;
        if (status == DownloadStatus.STATUS_RUNNING.getValue()) {
            Intent pauseIntent = new Intent();
            pauseIntent.setAction(UpgradeService.RECEIVER_PAUSE);
            pauseIntent.putExtra(UpgradeManager.PARAMS_ID, id);
            PendingIntent pausePendingIntent =
                    PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            boolean indeterminate = max_length == -1;

            notification = new NotificationCompat.Builder(context, CHANNEL_NAME)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setContentText(indeterminate?"":planTime)
                    .setContentIntent(pausePendingIntent)
                    .setProgress(indeterminate?0:max_length, indeterminate?0:current_length, indeterminate)
                    .build();
        } else if (status == DownloadStatus.STATUS_SUCCESSFUL.getValue()) {
            Intent installIntent = new Intent();
            installIntent.setAction(UpgradeManager.DOWNLOAD_INSTALL);
            installIntent.putExtra(UpgradeService.DOWNLOAD_ID, id);

            PendingIntent installPendingIntent =
                    PendingIntent.getBroadcast(context, 0, installIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(context, CHANNEL_NAME)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setContentIntent(installPendingIntent)
                    .setContentText("Download Finished")
                    .build();
        } else if (status == DownloadStatus.STATUS_PAUSED.getValue()) {
            Intent reStartIntent = new Intent();
            reStartIntent.setAction(UpgradeService.RECEIVER_RESTART);
            reStartIntent.putExtra(UpgradeManager.PARAMS_ID, id);
            PendingIntent reStartPendingIntent =
                    PendingIntent.getBroadcast(context, 0, reStartIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new NotificationCompat.Builder(context, CHANNEL_NAME)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setContentIntent(reStartPendingIntent)
                    .setContentText("Download Paused")
                    .build();
        } else if (status == DownloadStatus.STATUS_FAILED.getValue()) {
            Intent reStartIntent = new Intent();
            reStartIntent.setAction(UpgradeService.RECEIVER_RESTART);
            reStartIntent.putExtra(UpgradeManager.PARAMS_ID, id);
            PendingIntent reStartPendingIntent =
                    PendingIntent.getBroadcast(context, 0, reStartIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new NotificationCompat.Builder(context, CHANNEL_NAME)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setContentIntent(reStartPendingIntent)
                    .setContentText("Download Failed")
                    .build();
        } else {
            notification = new NotificationCompat.Builder(context, CHANNEL_NAME)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setContentTitle(title)
                    .setProgress(0, 0, true)
                    .build();
        }
        NotificationManagerCompat compat = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(createNotificationChannel());
        }
        compat.notify(id, notification);

//        notificationManager.notify(id, notification);
    }

    static void removeNotification(Context context, long id) {
        NotificationManagerCompat compat = NotificationManagerCompat.from(context);
        compat.cancel((int) id);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createNotificationChannel() {
        String description = "Upgrade Application";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_NAME, CHANNEL_NAME, importance);
        channel.setDescription(description);
        channel.enableVibration(false);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        return channel;
    }
}