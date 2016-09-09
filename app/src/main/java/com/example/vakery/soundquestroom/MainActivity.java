package com.example.vakery.soundquestroom;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {

    //Log для вывода вспомогательной информации
    private final String LOG_TAG="myLog";
    EditText kWord;
    private Camera camera;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = Camera.open();
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //для портретного режима

        kWord = (EditText)findViewById(R.id.kWord);
        kWord.setText("skynet");
        kWord.setClickable(false);
        kWord.setEnabled(false);

        Button speechBtn=(Button) findViewById(R.id.speech_btn);

        //проверяем, поддерживается ли распознование речи
        PackageManager packManager= getPackageManager();
        List<ResolveInfo> intActivities= packManager.queryIntentActivities(new
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if(intActivities.size()!=0){
// распознавание поддерживается, будем отслеживать событие щелчка по кнопке
            speechBtn.setOnClickListener(this);
        }else
        {
// распознавание не работает. Заблокируем
// кнопку и выведем соответствующее
// предупреждение.
            speechBtn.setEnabled(false);
            Toast.makeText(this,"Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(LOG_TAG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(LOG_TAG, "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
           //     Log.d(LOG_TAG, "onRmsChanged");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d(LOG_TAG, "onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(LOG_TAG, "onEndOfSpeech");
                ;
            }

            @Override
            public void onError(int error) {
                Log.d(LOG_TAG, "onError");
            }

            @Override
            public void onResults(Bundle results) {
                Log.d(LOG_TAG,"onResults 1");

                ArrayList<String> suggestedWords = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                // логи
                for (int i = 0; i < suggestedWords.size(); i++)
                {Log.d(LOG_TAG, "result " + suggestedWords.get(i));}

                onLight(suggestedWords);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d(LOG_TAG, "onPartialResults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(LOG_TAG, "onEvent");
            }
        });
    }

    public void startRecognize(){
        Log.d(LOG_TAG,"startRecognize");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"en-US");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        speechRecognizer.startListening(intent);
    }

    public void onLight(ArrayList wordList){
        Log.d(LOG_TAG,"onLight");

        String needWord = kWord.getText().toString();

        for(Object getWord : wordList ){
            String sWordList = getWord.toString();
            for(String splitGetWord : sWordList.split(" ") )
                if((needWord.toLowerCase()).equals(splitGetWord.toLowerCase())){
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.startPreview();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Camera.Parameters parameters = camera.getParameters();
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            camera.setParameters(parameters);
                            camera.startPreview();
                        }
                    }, 3000);
                }
        }
    }

/* media controls */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOG_TAG,"onKeyDown");

        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                startRecognize();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                startRecognize();
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
                startRecognize();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        Log.d(LOG_TAG,"onClick");

        if(view.getId() == R.id.speech_btn){
            startRecognize();
        }
    }

}
