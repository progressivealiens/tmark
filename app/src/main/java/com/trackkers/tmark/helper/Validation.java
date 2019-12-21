package com.trackkers.tmark.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Validation {
    public static boolean emailValidator(String email) {
        if (email.matches("")) {
            return false;
        } else {
            Pattern pattern;
            Matcher matcher;
            final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            pattern = Pattern.compile(EMAIL_PATTERN);
            matcher = pattern.matcher(email);
            return matcher.matches();
        }
    }

    public static boolean nameValidator(String name) {
        if (name.matches("")) {
            return false;
        } else if (name.length() < 3) {
            return false;
        } else {
            Pattern pattern;
            Matcher matcher;
            pattern = Pattern.compile("[a-zA-Z0-9\\-'\\s]++");
            matcher = pattern.matcher(name);
            return matcher.matches();
        }
    }

    public static boolean urlValidator(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean nullValidator(String value) {
        return value.matches("");
    }

    public static boolean passValidator(String pass) {
        if (pass.matches("")) {
            return false;
        } else return pass.length() >= 6;
    }

    public static boolean confirmPassValidator(String pass, String cpass) {
        if (cpass.matches("")) {
            return false;
        } else return pass.matches(cpass);
    }

    public static boolean termsValidator(CheckBox checkBox) {
        return checkBox.isChecked();
    }

    public static boolean mobileValidator(String phone) {
        if (phone.matches("")) {
            return false;
        } else return phone.length() >= 10;
    }

    public static class generalTextWatcher implements TextWatcher {
        EditText editText;
        String type;
        int pos;

        public generalTextWatcher(EditText editText, String type, int pos) {
            this.editText = editText;
            this.type = type;
            this.pos = pos;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
