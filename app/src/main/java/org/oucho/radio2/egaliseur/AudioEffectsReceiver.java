package org.oucho.radio2.egaliseur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AudioEffectsReceiver extends BroadcastReceiver {

    public static final String EXTRA_AUDIO_SESSION_ID = "org.oucho.radio2.EXTRA_AUDIO_SESSION_ID";

    public static final String ACTION_OPEN_AUDIO_EFFECT_SESSION = "org.oucho.radio2.OPEN_AUDIO_EFFECT_SESSION";
    public static final String ACTION_CLOSE_AUDIO_EFFECT_SESSION = "org.oucho.radio2.CLOSE_AUDIO_EFFECT_SESSION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int audioSessionId = intent.getIntExtra(EXTRA_AUDIO_SESSION_ID, 0);
        if(ACTION_OPEN_AUDIO_EFFECT_SESSION.equals(action))
        {
            AudioEffects.openAudioEffectSession(context, audioSessionId);
        }
        else if(ACTION_CLOSE_AUDIO_EFFECT_SESSION.equals(action))
        {
            AudioEffects.closeAudioEffectSession();
        }
    }
}
