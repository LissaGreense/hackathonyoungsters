package com.youngsters.myapplication2;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * Created by Yoshimoo12 on 2017-06-12.
 */

public class Speech {

    private TextToSpeech textToSpeech;
    private Context context;

    Speech(Context context){
        this.context = context;

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    public void speak(final String text) {
        textToSpeech.speak(text, 1, null, null);
    }
}
