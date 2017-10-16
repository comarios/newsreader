package com.ucl.news.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ucl.news.dao.RegistrationDAO;
import com.ucl.news.api.RegistrationHttpPostTask;
import com.ucl.news.utils.AutoLogin;
import com.ucl.newsreader.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class QuestionnaireActivity extends Activity {

    private ProgressBar progressBarQuestionnaire;
    private String firstname;
    private String lastname;
    private String emailaddress;
    private String password;
    private String gender;
    private String dateofbirth;

    private Button mBtnCompleteRegistration;
    private Button mBtnCancelRegistration;
    private CoordinatorLayout coordinatorLayout;
    private Spinner quest1;
    private Spinner quest2;
    private Spinner quest3;
    private Spinner quest4;
    private Spinner quest5;
    private Spinner quest6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_questionnaire);

        initViews();

        Intent intent = getIntent();
        firstname = intent.getStringExtra("firstname");
        lastname = intent.getStringExtra("lastname");
        emailaddress = intent.getStringExtra("emailaddress");
        password = intent.getStringExtra("password");
        gender = intent.getStringExtra("gender");
        dateofbirth = intent.getStringExtra("dateofbirth");

        Log.e("firstname: ", firstname);
        Log.e("lastname: ", lastname);
        Log.e("emailaddress: ", emailaddress);
        Log.e("password: ", password);
        Log.e("gender: ", gender);
        Log.e("dateofbirth: ", dateofbirth);

    }

    private void initViews() {
        progressBarQuestionnaire = (ProgressBar) findViewById(R.id.progressBarQuestionnaire);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mBtnCompleteRegistration = (Button) findViewById(R.id.btnQuestionnaireSubmit);
        mBtnCompleteRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRegistration();
            }
        });
        mBtnCancelRegistration = (Button) findViewById(R.id.btnQUestionnaireCancel);
        mBtnCancelRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        quest1 = (Spinner) findViewById(R.id.quest1);
        quest2 = (Spinner) findViewById(R.id.quest2);
        quest3 = (Spinner) findViewById(R.id.quest3);
        quest4 = (Spinner) findViewById(R.id.quest4);
        quest5 = (Spinner) findViewById(R.id.quest5);
        quest6 = (Spinner) findViewById(R.id.quest6);
    }

    private void submitRegistration() {

        RegistrationDAO registrationDAO = new RegistrationDAO();
        registrationDAO.setfName(firstname);
        registrationDAO.setlName(lastname);
        registrationDAO.setEmail_address(emailaddress);
        registrationDAO.setPassword(password);
        registrationDAO.setGender(gender);
        registrationDAO.setDob(dateofbirth);

        registrationDAO.setQ1_frequency(quest1.getSelectedItem().toString());
        registrationDAO.setQ2_readingtime(quest2.getSelectedItem().toString());
        registrationDAO.setQ3_browsingstrategy(quest3.getSelectedItem().toString());
        registrationDAO.setQ4_readingstyle(quest4.getSelectedItem().toString());
        registrationDAO.setQ5_location(quest5.getSelectedItem().toString());
        registrationDAO.setQ6_timeofday(quest6.getSelectedItem().toString());

        RegistrationHttpPostTask rhpt = new RegistrationHttpPostTask(
                getApplicationContext(), this, registrationDAO);
    }

    private void serverError() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Server Error - Not a valid request", Snackbar.LENGTH_LONG)
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

    public void registerCompleted(String result) {
        progressBarQuestionnaire.setVisibility(View.INVISIBLE);

        String userID = null;
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
            serverError();
        } else {
            String credentials = "YES" + ";" + userID + ";"
                    + UUID.randomUUID().toString() + ";";
            AutoLogin.saveSettingsFile(getApplicationContext(), credentials);

            Intent i = new Intent(QuestionnaireActivity.this, MainActivity.class);
            i.putExtra("ref", "RegistrationCaller");
            startActivity(i);
        }
    }
}
