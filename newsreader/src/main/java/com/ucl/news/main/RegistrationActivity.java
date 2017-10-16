package com.ucl.news.main;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import android.widget.TextView;

import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.Dialogs;
import com.ucl.newsreader.R;

public class RegistrationActivity extends Activity implements OnClickListener {

    private Button mSubmit;
    private Button mCancel;

    private EditText mFname;
    private EditText mLname;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mEmail;
    private Spinner mGender;
    private String Gen;
    private EditText mDoBTxt;
    private DatePickerDialog mDoB;
    private SimpleDateFormat dateFormatter;
    private ProgressBar progressRegister;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.register);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.US);

        initViews();
        setDateTimeField();
    }

    private void initViews() {
        progressRegister = (ProgressBar) findViewById(R.id.progressBarRegister);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        mSubmit = (Button) findViewById(R.id.nextRegistrationStep1);
        mSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registrationStep1();
            }
        });
        mFname = (EditText) findViewById(R.id.efname);
        mFname.setFocusableInTouchMode(true);
        mFname.setFocusable(true);
        mFname.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFname.getWindowToken(), 0);

        mLname = (EditText) findViewById(R.id.elname);
        mPassword = (EditText) findViewById(R.id.repass);
        mEmail = (EditText) findViewById(R.id.eemail);
        mGender = (Spinner) findViewById(R.id.spinner1);
        mDoBTxt = (EditText) findViewById(R.id.dateOfBirth);
        mDoBTxt.setInputType(InputType.TYPE_NULL);
        mDoBTxt.requestFocus();

    }

    private void setDateTimeField() {
        mDoBTxt.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        mDoB = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mDoBTxt.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void showInvalidFormError() {
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Please make sure all fields are filled out!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void registrationStep1() {

        boolean invalid = false;
        progressRegister.setVisibility(View.VISIBLE);

        if (mFname.getText().toString().equals("") ||
                mLname.getText().toString().equals("") ||
                mPassword.getText().toString().equals("") ||
                mEmail.getText().toString().equals("") ||
                mGender.getSelectedItem().toString().equals("")) {

            invalid = true;
            progressRegister.setVisibility(View.GONE);
            showInvalidFormError();
        }
//        if (mFname.getText().toString().equals("")) {
//            invalid = true;
//            mFname.setBackgroundResource(R.drawable.error);
//            progressRegister.setVisibility(View.GONE);
//        } else if (mLname.getText().toString().equals("")) {
//            invalid = true;
//            mLname.setBackgroundResource(R.drawable.error);
//            progressRegister.setVisibility(View.GONE);
//        } else if (mPassword.getText().toString().equals("")) {
//            invalid = true;
//            mPassword.setBackgroundResource(R.drawable.error);
//            progressRegister.setVisibility(View.GONE);
//        } else if (mEmail.getText().toString().equals("")) {
//            invalid = true;
//            mEmail.setBackgroundResource(R.drawable.error);
//            progressRegister.setVisibility(View.GONE);
//        }
        else if (invalid == false) {


            Calendar date = Calendar.getInstance();
            date.set(mDoB.getDatePicker().getYear(), mDoB.getDatePicker().getMonth(), mDoB.getDatePicker().getDayOfMonth());
            mDoBTxt.setText(dateFormatter.format(date.getTime()));
            String dobStr = dateFormatter.format(date.getTime());

            Intent i = new Intent(RegistrationActivity.this, QuestionnaireActivity.class);
            i.putExtra("firstname", mFname.getText().toString());
            i.putExtra("lastname", mLname.getText().toString());
            i.putExtra("emailaddress", mEmail.getText().toString());
            i.putExtra("password", mPassword.getText().toString());
            i.putExtra("gender", mGender.getSelectedItem().toString());
            i.putExtra("dateofbirth", dobStr);
            startActivity(i);
            progressRegister.setVisibility(View.GONE);
        }
    }

    public void register(String result) {

        Log.e("RESULT REGISTER", result);
        progressRegister.setVisibility(View.INVISIBLE);

        String userID = null;

        Log.e("RES1", result);

        try {
            JSONObject jObject = new JSONObject(result);
            userID = jObject.getString("UserID");
            Log.e("RES2", userID);
        } catch (JSONException e) {
            Log.e("Parse result", e.getLocalizedMessage());

        }

        if (result.equals("Not valid request")
                || result.equals("Json doesn't contain any values")
                || result.isEmpty()
                || userID.equals(null)
                || result.contains("Duplicate")) {
            new Dialogs().createDialogLoginERROR(RegistrationActivity.this,
                    getApplicationContext());
        } else {
            String credentials = "YES" + ";" + userID + ";"
                    + UUID.randomUUID().toString() + ";";
            AutoLogin.saveSettingsFile(getApplicationContext(), credentials);

            Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//			i.putExtra("userID", userID);
//			i.putExtra("ref", "RegistrationActivity");
            i.putExtra("ref", "RegistrationCaller");
            startActivity(i);
            //this.finish();
        }

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.dateOfBirth:
                Log.e("test", "test");
                mDoB.show();
                break;
        }
    }

//    @Override
//    public void onClick(View v) {
//        // TODO Auto-generated method stub
//
//        switch (v.getId()) {
//
////            case R.id.cancel:
////                Intent i = new Intent(getBaseContext(), LoginActivity.class);
////                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                startActivity(i);
////                // finish();
////                break;
////
////            case R.id.dateOfBirth:
////                Log.e("test","test");
////                mDoB.show();
////                break;
//
//            case R.id.nextRegistrationStep1:
//
//                progressRegister.setVisibility(View.VISIBLE);
//
//                String fname = mFname.getText().toString();
//                String lname = mLname.getText().toString();
//                String pass = mPassword.getText().toString();
//                String email = mEmail.getText().toString();
//                String gender = mGender.getSelectedItem().toString();
//
//
//                Calendar date = Calendar.getInstance();
//                date.set(mDoB.getDatePicker().getYear(), mDoB.getDatePicker().getMonth(), mDoB.getDatePicker().getDayOfMonth());
//                mDoBTxt.setText(dateFormatter.format(date.getTime()));
//                String dobStr = dateFormatter.format(date.getTime());
//                Log.e("dateStr: ", dobStr);
//                Log.e("dateFormatter", dateFormatter.toString());
//
//
//                boolean invalid = false;
//
//                if (fname.equals("")) {
//                    invalid = true;
//                    mFname.setBackgroundResource(R.drawable.error);
//                    progressRegister.setVisibility(View.GONE);
//                } else if (lname.equals("")) {
//                    invalid = true;
//                    mLname.setBackgroundResource(R.drawable.error);
//                    progressRegister.setVisibility(View.GONE);
//                } else if (pass.equals("")) {
//                    invalid = true;
//                    mPassword.setBackgroundResource(R.drawable.error);
//                    progressRegister.setVisibility(View.GONE);
//                } else if (email.equals("")) {
//                    invalid = true;
//                    mEmail.setBackgroundResource(R.drawable.error);
//                    progressRegister.setVisibility(View.GONE);
//                } else if (invalid == false) {
//                    RegistrationDAO registrationDAO = new RegistrationDAO();
//                    registrationDAO.setfName(fname);
//                    registrationDAO.setlName(lname);
//                    registrationDAO.setEmail_address(email);
//                    registrationDAO.setPassword(pass);
//                    registrationDAO.setGender(gender);
//                    registrationDAO.setDob(dobStr);
//
//                    RegistrationHttpPostTask rhpt = new RegistrationHttpPostTask(
//                            getApplicationContext(), this, registrationDAO);
//                }
//                break;
//        }
//    }

}
