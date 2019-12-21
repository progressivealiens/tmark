package com.trackkers.tmark.views.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.helper.PrefData;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectLanguageActivity extends AppCompatActivity {

    @BindView(R.id.rb_english)
    RadioButton rbEnglish;
    @BindView(R.id.rb_hindi)
    RadioButton rbHindi;
    @BindView(R.id.rg_select_language)
    RadioGroup rgSelectLanguage;
    @BindView(R.id.btn_language_proceed)
    MyButton btnLanguageProceed;

    String languageToLoad="en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        ButterKnife.bind(this);


        initialize();

        rgSelectLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_english:
                        languageToLoad = "en";
                        Log.e("english","english");
                        btnLanguageProceed.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_hindi:
                        languageToLoad = "hi";
                        Log.e("hindi","hindi");
                        btnLanguageProceed.setVisibility(View.VISIBLE);
                        break;

                }
            }
        });


        btnLanguageProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PrefData.writeStringPref(PrefData.PREF_selected_language,languageToLoad);
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config,getResources().getDisplayMetrics());

                startActivity(new Intent(SelectLanguageActivity.this,IntroActivity.class));
            }
        });

    }

    private void initialize() {
        btnLanguageProceed.setVisibility(View.GONE);
    }



}
