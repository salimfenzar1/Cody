package com.example.codygroepje;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private SpeechSynthesizer synthesizer;
    private SpeechRecognizer speechRecognizer;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request microphone permission
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionToRecordAccepted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (permissionToRecordAccepted) {
            initializeSpeechComponents();
        } else {
            Toast.makeText(this, "Permission to use microphone denied", Toast.LENGTH_SHORT).show();
            finish(); // Close the app if permission is denied
        }
    }

    private void initializeSpeechComponents() {
        // Initialize Microsoft Azure Text-to-Speech
        String apiKey = "50ebcb27116f4c669a3f595d905b4d27";
        String region = "westeurope";
        SpeechConfig config = SpeechConfig.fromSubscription(apiKey, region);
        synthesizer = new SpeechSynthesizer(config);

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Do something when ready for speech
            }

            @Override
            public void onBeginningOfSpeech() {
                // Do something at the beginning of speech
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Do something when RMS changed
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Do something when buffer received
            }

            @Override
            public void onEndOfSpeech() {
                // Do something at the end of speech
            }

            @Override
            public void onError(int error) {
                Log.e("SpeechRecognizer", "Error: " + error);
                speakAndRestartListening("Ik verstond je niet. Probeer het opnieuw.");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    handleSpeechResults(matches.get(0));
                } else {
                    speakAndRestartListening("Ik verstond je niet. Probeer het opnieuw.");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Do something with partial results
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Handle events
            }
        });

        // Add a delay to ensure the activity is fully loaded
        new Handler(Looper.getMainLooper()).postDelayed(() -> new SynthesisTask().execute(), 2000); // 2 seconds delay
    }

    private class SynthesisTask extends AsyncTask<Void, Void, SpeechSynthesisResult> {
        @Override
        protected SpeechSynthesisResult doInBackground(Void... voids) {
            String text = "Hoi, ik ben Cody! Jullie kunnen samen met mij een spel spelen.";
            try {
                return synthesizer.SpeakText(text);
            } catch (Exception e) {
                Log.e("TTS", "Error in speech synthesis: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(SpeechSynthesisResult result) {
            if (result != null) {
                if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                    Log.i("TTS", "Speech synthesis succeeded.");
                    Toast.makeText(MainActivity.this, "Speech synthesis succeeded.", Toast.LENGTH_SHORT).show();
                    startListening();
                } else {
                    Log.e("TTS", "Speech synthesis failed. Reason: " + result.getReason());
                    Toast.makeText(MainActivity.this, "Speech synthesis failed.", Toast.LENGTH_SHORT).show();
                }
                result.close();
            } else {
                Toast.makeText(MainActivity.this, "Error in speech synthesis.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Zeg de naam van het spel");
        speechRecognizer.startListening(intent);
    }

    private void handleSpeechResults(String spokenText) {
        if (spokenText.equalsIgnoreCase("wat vind ik erger")) {
            Toast.makeText(this, "You said: Wat vind ik erger", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, watVindIkErger.class));

        } else if(spokenText.equalsIgnoreCase("Twijfel")){
            speakAndRestartListening("Samen met mij kunnen jullie spellen spelen die moeilijke onderwerpen bespreekbaar maakt. Jullie kunnen kiezen tussen: de tijd tikt, levend organogram en wat vind ik erger. Welk spel willen jullie spelen?");
        }else {
            speakAndRestartListening("Ik verstond je niet. Probeer het opnieuw.");
        }
    }

    private void speakAndRestartListening(String message) {
        new SpeakTask().execute(message);
    }


    private class SpeakTask extends AsyncTask<String, Void, SpeechSynthesisResult> {
        @Override
        protected SpeechSynthesisResult doInBackground(String... texts) {
            try {
                return synthesizer.SpeakText(texts[0]);
            } catch (Exception e) {
                Log.e("TTS", "Error in speech synthesis: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(SpeechSynthesisResult result) {
            if (result != null && result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                Log.i("TTS", "Speech synthesis succeeded.");
                startListening();
            } else {
                Log.e("TTS", "Speech synthesis failed.");
                startListening();
            }
            if (result != null) {
                result.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Shutdown synthesizer and speech recognizer when activity is destroyed
        if (synthesizer != null) {
            synthesizer.close();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
