package org.oucho.radio2.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.oucho.radio2.PlayerService;

public class StopReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        String fichier_préférence = "org.oucho.radio2_preferences";

        SharedPreferences préférences = context.getSharedPreferences(fichier_préférence, 0);

        String nom_radio;

        String state = intent.getAction();

        if ("org.oucho.radio2.STATE".equals(state)) {

            nom_radio = intent.getStringExtra("name");

            if (nom_radio == null) {
                nom_radio = préférences.getString("name", "");
            }

            String action_lecteur = intent.getStringExtra("state");

            Notification.updateNotification(context, nom_radio, action_lecteur);
        }


        if ("org.oucho.radio2.STOP".equals(state)) {

            String halt = intent.getStringExtra("halt");

            Intent player = new Intent(context, PlayerService.class);
            player.putExtra("action", halt);
            context.startService(player);

        }
    }
}
