package org.oucho.radio2.timer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import org.oucho.radio2.MainActivity;


public class GetAudioFocusTask implements Runnable {
    final MainActivity context;


    public GetAudioFocusTask(MainActivity context) {
        this.context = context;
    }

    public void run() {

        MainActivity.stop(context);

    }
}
