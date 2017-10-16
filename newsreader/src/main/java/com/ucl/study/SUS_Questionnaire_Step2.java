package com.ucl.study;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.ucl.news.api.StoreSUSQuestionnaire;
import com.ucl.news.dao.SUSQuestionnaireDAO;
import com.ucl.news.main.MainActivity;
import com.ucl.news.utils.AutoLogin;
import com.ucl.newsreader.R;

import org.json.JSONException;
import org.json.JSONObject;


public class SUS_Questionnaire_Step2 extends Activity {

    private ProgressBar progressBarQuestionnaire;
    private CoordinatorLayout coordinatorLayout;

    private Button btnSUS_Submit;
    private Button btnSUS_Cancel;

    private String sus_quest1;
    private String sus_quest2;
    private String sus_quest3;
    private String sus_quest4;
    private String sus_quest5;
    private Spinner sus_quest6;
    private Spinner sus_quest7;
    private Spinner sus_quest8;
    private Spinner sus_quest9;
    private Spinner sus_quest10;
    private String environment;

    private SUSQuestionnaireDAO susQuestionnaireDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sus_questionnaire_step2);

        initViews();

        Intent intent = getIntent();
        sus_quest1 = intent.getStringExtra("sus_quest1");
        sus_quest2 = intent.getStringExtra("sus_quest2");
        sus_quest3 = intent.getStringExtra("sus_quest3");
        sus_quest4 = intent.getStringExtra("sus_quest4");
        sus_quest5 = intent.getStringExtra("sus_quest5");
        environment = intent.getStringExtra("environment");

    }

    private void initViews() {
        progressBarQuestionnaire = (ProgressBar) findViewById(R.id.progressBarSUSQuestionnaire_Step2);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSUS_Questionnaire_Step2);

        btnSUS_Submit = (Button) findViewById(R.id.btnSUS_Submit);
        btnSUS_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSUS_Questionnaire();
            }
        });

        btnSUS_Cancel = (Button) findViewById(R.id.btnSUS_Cancel);
        btnSUS_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sus_quest6 = (Spinner) findViewById(R.id.sus_quest6);
        sus_quest7 = (Spinner) findViewById(R.id.sus_quest7);
        sus_quest8 = (Spinner) findViewById(R.id.sus_quest8);
        sus_quest9 = (Spinner) findViewById(R.id.sus_quest9);
        sus_quest10 = (Spinner) findViewById(R.id.sus_quest10);
    }

    private void submitSUS_Questionnaire(){

        susQuestionnaireDAO = new SUSQuestionnaireDAO();
        susQuestionnaireDAO.setUserID(AutoLogin.getUserID(AutoLogin
                .getSettingsFile(getApplicationContext())));
        susQuestionnaireDAO.setEnvironment(environment);
        susQuestionnaireDAO.setQuestion1(sus_quest1);
        susQuestionnaireDAO.setQuestion2(sus_quest2);
        susQuestionnaireDAO.setQuestion3(sus_quest3);
        susQuestionnaireDAO.setQuestion4(sus_quest4);
        susQuestionnaireDAO.setQuestion5(sus_quest5);
        susQuestionnaireDAO.setQuestion6(sus_quest6.getSelectedItem().toString());
        susQuestionnaireDAO.setQuestion7(sus_quest7.getSelectedItem().toString());
        susQuestionnaireDAO.setQuestion8(sus_quest8.getSelectedItem().toString());
        susQuestionnaireDAO.setQuestion9(sus_quest9.getSelectedItem().toString());
        susQuestionnaireDAO.setQuestion10(sus_quest10.getSelectedItem().toString());


        System.out.println("sus_quest1: " + sus_quest1);
        System.out.println("sus_quest2: " + sus_quest2);
        System.out.println("sus_quest3: " + sus_quest3);
        System.out.println("sus_quest4 " + sus_quest4);
        System.out.println("sus_quest5: " + sus_quest5);
        System.out.println("sus_quest6: " + sus_quest6.getSelectedItem().toString());
        System.out.println("sus_quest7: " + sus_quest7.getSelectedItem().toString());
        System.out.println("sus_quest8: " + sus_quest8.getSelectedItem().toString());
        System.out.println("sus_quest9: " + sus_quest9.getSelectedItem().toString());
        System.out.println("sus_quest10: " + sus_quest10.getSelectedItem().toString());
        System.out.println("environment: " + environment);

        StoreSUSQuestionnaire storeSUSQuestionnaire = new StoreSUSQuestionnaire(getApplicationContext(), this, susQuestionnaireDAO);

    }

    /**
     * Handles the result of the Study API - SUS Questionnaire
     *
     * @param result
     */
    public void susQuestionnaireResult(String result) {

       String sus_id_out;

        try {
            JSONObject jObject = new JSONObject(result);
            sus_id_out = jObject.getString("sus_id_out");

            System.out.println("sus_id_out:" + sus_id_out);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    SUS_Questionnaire_Step2.this);
            alertDialogBuilder.setTitle("Adapted SUS Questionnaire");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Thanks for completing the questionnaire. Please press OK to continue using Habito News.")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            // terminate activity and return back to MainActivity
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);

                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                            finish();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        } catch (JSONException e) {
            Log.e("Parse result", e.getLocalizedMessage());
        }
    }


}
