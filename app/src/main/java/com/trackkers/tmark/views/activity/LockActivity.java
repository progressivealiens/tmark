package com.trackkers.tmark.views.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.helper.PinEntryEditText;
import com.trackkers.tmark.helper.PrefData;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.et_password)
    PinEntryEditText etPassword;
    @BindView(R.id.btn_one)
    MyButton btnOne;
    @BindView(R.id.btn_two)
    MyButton btnTwo;
    @BindView(R.id.btn_three)
    MyButton btnThree;
    @BindView(R.id.btn_four)
    MyButton btnFour;
    @BindView(R.id.btn_five)
    MyButton btnFive;
    @BindView(R.id.btn_six)
    MyButton btnSix;
    @BindView(R.id.btn_seven)
    MyButton btnSeven;
    @BindView(R.id.btn_eight)
    MyButton btnEight;
    @BindView(R.id.btn_nine)
    MyButton btnNine;
    @BindView(R.id.btn_zero)
    MyButton btnZero;
    @BindView(R.id.btn_clear)
    MyButton btnClear;
    @BindView(R.id.btn_backspace)
    MyButton btnBackspace;

    String rememberedPassword="";
    String enteredPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        ButterKnife.bind(this);

        Locale locale = new Locale(PrefData.readStringPref(PrefData.PREF_selected_language));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        initialize();

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 4) {
                    String typedPassword = s.toString();
                    if (typedPassword.equalsIgnoreCase(rememberedPassword)) {
                        startActivity(new Intent(LockActivity.this, SplashActivity.class));
                        finish();
                    } else {
                        etPassword.setText("");
                        enteredPassword = "";
                        Toast.makeText(LockActivity.this, getResources().getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void initialize() {
        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        btnFive.setOnClickListener(this);
        btnSix.setOnClickListener(this);
        btnSeven.setOnClickListener(this);
        btnEight.setOnClickListener(this);
        btnNine.setOnClickListener(this);
        btnZero.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnBackspace.setOnClickListener(this);

        rememberedPassword=PrefData.readStringPref(PrefData.lockPassword);
        if(rememberedPassword.equalsIgnoreCase("")){
            rememberedPassword="1234";
        }else{
            rememberedPassword=PrefData.readStringPref(PrefData.lockPassword);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_one:
                enteredPassword = enteredPassword + "1";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_two:
                enteredPassword = enteredPassword + "2";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_three:
                enteredPassword = enteredPassword + "3";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_four:
                enteredPassword = enteredPassword + "4";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_five:
                enteredPassword = enteredPassword + "5";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_six:
                enteredPassword = enteredPassword + "6";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_seven:
                enteredPassword = enteredPassword + "7";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_eight:
                enteredPassword = enteredPassword + "8";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_nine:
                enteredPassword = enteredPassword + "9";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_zero:
                enteredPassword = enteredPassword + "0";
                etPassword.setText(enteredPassword);
                break;
            case R.id.btn_clear:
                enteredPassword="";
                etPassword.setText("");
                break;
            case R.id.btn_backspace:
                if (enteredPassword.length()>0){
                    enteredPassword=enteredPassword.substring(0,(enteredPassword.length()-1));
                    etPassword.setText(enteredPassword);
                }else{
                    //do nothing
                }

                break;
        }
    }
}
