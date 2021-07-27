package com.example.translator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromspinner, tospinner;
    private TextInputEditText sourcetext;
    private ImageView micimg;
    private MaterialButton translatebtn;
    private TextView translatedtext;

    String fromlang[] = {"From", "English", "African", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"};
    String tolang[] = {"To", "English", "African", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int langcode, tolangcode=0, fromlangcode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromspinner = findViewById(R.id.idfromspinner);
        tospinner = findViewById(R.id.idtospinner);
        sourcetext = findViewById(R.id.idsourcetext);
        micimg = findViewById(R.id.idmic);
        translatebtn = findViewById(R.id.idbuttontranslate);
        translatedtext = findViewById(R.id.idtranslated);

        fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromlangcode = getlangcode(fromlang[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter fromadapter = new ArrayAdapter(this, R.layout.spinner_item, fromlang);

        fromadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromadapter);


        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tolangcode = getlangcode(tolang[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toadapter = new ArrayAdapter(this, R.layout.spinner_item, tolang);
        toadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tospinner.setAdapter(toadapter);

        translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedtext.setText("");
                if(sourcetext.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Please enter your text!", Toast.LENGTH_SHORT).show();
                }
                else if(fromlangcode == 0)
                {
                    Toast.makeText(MainActivity.this, "Please select source language.", Toast.LENGTH_SHORT).show();
                }
                else if(tolangcode == 0)
                {
                    Toast.makeText(MainActivity.this, "Please select a language to make translation", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    translatetext(fromlangcode, tolangcode, sourcetext.getText().toString());
                }
            }
        });

        micimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "speak to convert into text");

                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION_CODE)
        {
            if(resultCode == RESULT_OK && data!=null)
            {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourcetext.setText(res.get(0));
            }
        }
    }

    private void translatetext(int fromlangcode, int tolangcode, String sourcetext)
    {
        translatedtext.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlangcode)
                .setTargetLanguage(tolangcode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                .getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedtext.setText("Translating...");
                translator.translate(sourcetext).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedtext.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download language model"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // {"From", "English", "African", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"}
    public int getlangcode(String languages)
    {
        int langcode = 0;
        switch (languages)
        {
            case "English":
                langcode = FirebaseTranslateLanguage.EN;
                break;

            case "African":
                langcode = FirebaseTranslateLanguage.AF;
                break;

            case "Arabic":
                langcode = FirebaseTranslateLanguage.AR;
                break;

            case "Belarusian":
                langcode = FirebaseTranslateLanguage.BE;
                break;

            case "Bengali":
                langcode = FirebaseTranslateLanguage.BN;
                break;

            case "Catalan":
                langcode = FirebaseTranslateLanguage.CA;
                break;

            case "Czech":
                langcode = FirebaseTranslateLanguage.CS;
                break;

            case "Hindi":
                langcode = FirebaseTranslateLanguage.HI;
                break;

            case "Welsh":
                langcode = FirebaseTranslateLanguage.CY;
                break;

            case "Urdu":
                langcode = FirebaseTranslateLanguage.UR;
                break;

            case "Bulgarian":
                langcode = FirebaseTranslateLanguage.BG;
                break;

            default:
                langcode = 0;
        }
        return langcode;
    }
}