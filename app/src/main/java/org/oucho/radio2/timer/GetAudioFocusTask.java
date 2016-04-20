package org.oucho.radio2.timer;

import org.oucho.radio2.MainActivity;


public class GetAudioFocusTask implements Runnable {
    private final MainActivity context;


    public GetAudioFocusTask(MainActivity context) {
        this.context = context;
    }

    public void run() {

        MainActivity.stop(context);

    }
}
