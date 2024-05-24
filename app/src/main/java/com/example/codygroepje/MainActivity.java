package com.example.codygroepje;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

public class MainActivity extends AppCompatActivity {

    private SpeechSynthesizer synthesizer;

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

        // Initialize Microsoft Azure Text-to-Speech
        String apiKey = "50ebcb27116f4c669a3f595d905b4d27";
        String region = "westeurope";
        SpeechConfig config = SpeechConfig.fromSubscription(apiKey, region);
        synthesizer = new SpeechSynthesizer(config);
        new Handler(Looper.getMainLooper()).postDelayed(this::speakIntroText, 2000); // 2 seconds delay
    }

    private void speakIntroText() {
        String text = "Hoi, ik ben Cody! Jullie kunnen samen met mij een spel spelen. Deze spellen zullen het mogelijk maken om moeilijke onderwerpen bespreekbaar te maken. Jullie kunnen kiezen tussen: De tijd tikt, levend organogram en wat vind ik erger. ";
        try {
            SpeechSynthesisResult result = synthesizer.SpeakText(text);
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                Log.i("TTS", "Speech synthesis succeeded.");
            } else {
                Log.e("TTS", "Speech synthesis failed. Reason: " + result.getReason());
            }
            result.close();
        } catch (Exception e) {
            Log.e("TTS", "Error in speech synthesis: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        // Shutdown synthesizer when activity is destroyed
        if (synthesizer != null) {
            synthesizer.close();
        }
        super.onDestroy();
    }
}
