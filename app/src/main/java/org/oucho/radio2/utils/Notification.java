package org.oucho.radio2.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import org.oucho.radio2.MainActivity;
import org.oucho.radio2.PlayerService;
import org.oucho.radio2.R;

public class Notification extends BroadcastReceiver  {

    private static final int NOTIFY_ID = 32;


    private static void updateNotification(Context ctx, String nom_radio, String action) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);
        builder.setSmallIcon(R.drawable.notification);
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


    @Override
    public void onReceive(Context context, Intent intent) {


        String nom_radio = intent.getStringExtra("name");
        String action_lecteur = intent.getStringExtra("state");

        updateNotification(context, nom_radio, action_lecteur);


        String halt = intent.getStringExtra("halt");

        if ("Stop".equals(halt)) {

            Intent player = new Intent(context, PlayerService.class);
            player.putExtra("action", "stop");
            context.startService(player);

        }

    }

    public static void removeNotification(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);

    }

}
