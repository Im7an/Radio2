package org.oucho.radio2.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import org.oucho.radio2.PlayerService;
import org.oucho.radio2.State;

public class StopReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        String etat = intent.getAction();

        if ("org.oucho.radio2.STATE".equals(etat)) {

            String fichier_préférence = "org.oucho.radio2_preferences";

            SharedPreferences préférences = context.getSharedPreferences(fichier_préférence, 0);

            String nom_radio = intent.getStringExtra("name");

            if (nom_radio == null) {
                nom_radio = préférences.getString("name", "");
            }

            String action_lecteur = intent.getStringExtra("state");

            Notification.updateNotification(context, nom_radio, action_lecteur);
        }


        if ( "org.oucho.radio2.STOP".equals(etat) && ( State.isPlaying() || State.isPaused() ) ) {

            String halt = intent.getStringExtra("halt");
            Intent player = new Intent(context, PlayerService.class);
            player.putExtra("action", halt);
            context.startService(player);

            final Context ctx = context;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @SuppressLint("SetTextI18n")
                public void run() {

                    if (!State.isPlaying())
                        Notification.removeNotification(ctx);

                }
            }, 500);
        }
    }
}
