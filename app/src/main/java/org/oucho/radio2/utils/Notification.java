package org.oucho.radio2.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import org.oucho.radio2.MainActivity;
import org.oucho.radio2.R;

public class Notification {

    private static final int NOTIFY_ID = 32;

    private static boolean timer = false;

    public static void setState(boolean onOff){
        timer = onOff;
    }

    public static void updateNotification(Context ctx, String nom_radio, String action) {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);

        if (!timer) {
            builder.setSmallIcon(R.drawable.notification);
        } else {
            builder.setSmallIcon(R.drawable.notification_sleeptimer);
        }
        builder.setOngoing(true);

        Boolean unlock;
        unlock = "Lecture".equals(action);
        builder.setOngoing(unlock);

        android.app.Notification notification = builder.build();
        RemoteViews contentView = new RemoteViews(ctx.getPackageName(), R.layout.notification);

        contentView.setTextViewText(R.id.notif_name, nom_radio);
        contentView.setTextViewText(R.id.notif_text, action);

        notification.contentView = contentView;

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);

    }


    public static void removeNotification(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);

    }

}
