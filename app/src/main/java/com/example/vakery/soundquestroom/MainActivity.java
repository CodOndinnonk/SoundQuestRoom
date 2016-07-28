package com.example.vakery.soundquestroom;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements OnClickListener,  TextToSpeech.OnInitListener {
    //переменная для проверки возможности
//распознавания голоса в телефоне
    private static final int VR_REQUEST=999;

    //ListView для отображения распознанных слов
    private ListView wordList;

    //Log для вывода вспомогательной информации
    private final String LOG_TAG="myLog";
//***здесь можно использовать собственный тег***

//переменные для работы TTS

    //переменная для проверки данных для TTS
    private int MY_DATA_CHECK_CODE=0;

    //Text To Speech интерфейс
    private TextToSpeech repeatTTS;

    EditText kWord;
    private Camera camera;
    Intent listenIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = Camera.open();

//запускаем интент, распознающий речь и передаем ему требуемые данные
        listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

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
        }
        else
        {
// распознавание не работает. Заблокируем
// кнопку и выведем соответствующее
// предупреждение.
            speechBtn.setEnabled(false);
            Toast.makeText(this,"Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }
    }



    public void onClickButton(View view) {
        Log.d(LOG_TAG,"onClickButton");

        firstStepToRecognize();
    }



    public void firstStepToRecognize(){
        Log.d(LOG_TAG, "firstStepToRecognize");

        if(kWord.getText().toString().length() > 0) {
// отслеживаем результат
            listenToSpeech();
        }else {
            Toast.makeText(this,"Введите ключевое слово", Toast.LENGTH_LONG).show();
        }
    }



    private void listenToSpeech(){
        Log.d(LOG_TAG,"listenToSpeech");

//указываем пакет
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getClass().getPackage().getName());
//В процессе распознования выводим сообщение
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say a word!");
//устанавливаем модель речи
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//указываем число результатов, которые могут быть получены
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,10);
//начинаем прослушивание
        startActivityForResult(listenIntent, VR_REQUEST);

        stopListeningIntent(7);
    }



public void stopListeningIntent(int sec){
    Log.d(LOG_TAG, "stopListeningIntent");

    // SLEEP * SECONDS HERE ...
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
        public void run() {
            stopService(listenIntent);
            Log.d(LOG_TAG, "после отключения интента");
        }
    }, sec*1000);
}



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(LOG_TAG,"onActivityResult");

//проверяем результат распознавания речи
        if(requestCode== VR_REQUEST & resultCode== RESULT_OK)
        {
//Добавляем распознанные слова в список результатов
            ArrayList<String> suggestedWords=
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            onLight(suggestedWords);
//Передаем список возможных слов через ArrayAdapter компоненту ListView
        }else {
            Toast.makeText(this,",бла-бла", Toast.LENGTH_LONG).show();
        }
//tss код здесь

//вызываем метод родительского класса
        super.onActivityResult(requestCode, resultCode, data);
    }



    public void onLight(ArrayList wordList){
        Log.d(LOG_TAG,"onLight");

        String needWord = kWord.getText().toString();

        for(Object getWord : wordList ){
            String sWordList = getWord.toString();
            for(String splitGetWord : sWordList.split(" ") )
                if((needWord.toLowerCase()).equals(splitGetWord.toLowerCase())){
                    // Toast.makeText(this,"Ok", Toast.LENGTH_SHORT).show();
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.startPreview();

                    // SLEEP 1 SECONDS HERE ...
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



    @Override
    public void onInit(int status) {
        Log.d(LOG_TAG,"onInit");}



    /* media controls */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOG_TAG,"onKeyDown");

        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                firstStepToRecognize();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                firstStepToRecognize();
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
                firstStepToRecognize();
                return true;
        }
        return false;
    }



    @Override
    public void onClick(View view) {
        Log.d(LOG_TAG,"onClick");

        if(view.getId() == R.id.speech_btn){
            firstStepToRecognize();
        }
    }



}
