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

import com.ucl.news.api.StoreComparisonQuestionnaire;
import com.ucl.news.api.StoreSUSQuestionnaire;
import com.ucl.news.dao.ComparisonQuestionnaireDAO;
import com.ucl.news.dao.SUSQuestionnaireDAO;
import com.ucl.news.main.MainActivity;
import com.ucl.news.utils.AutoLogin;
import com.ucl.newsreader.R;

import org.json.JSONException;
import org.json.JSONObject;


public class ComparisonQuestionnaire_Step2 extends Activity {

    private ProgressBar progressBarQuestionnaire;
    private CoordinatorLayout coordinatorLayout;

    private Button btnComparison_Submit;
    private Button btnComparison_Cancel;

    private String comparison_quest1;
    private String comparison_quest2;
    private String comparison_quest3;
    private String comparison_quest4;
    private String comparison_quest5;
    private Spinner comparison_quest6;
    private Spinner comparison_quest7;
    private Spinner comparison_quest8;
    private Spinner comparison_quest9;
    private Spinner comparison_quest10;

    private ComparisonQuestionnaireDAO comparisonQuestionnaireDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_comparison_questionnaire_step2);

        initViews();

        Intent intent = getIntent();
        comparison_quest1 = intent.getStringExtra("comparison_quest1");
        comparison_quest2 = intent.getStringExtra("comparison_quest2");
        comparison_quest3 = intent.getStringExtra("comparison_quest3");
        comparison_quest4 = intent.getStringExtra("comparison_quest4");
        comparison_quest5 = intent.getStringExtra("comparison_quest5");

    }

    private void initViews() {
        progressBarQuestionnaire = (ProgressBar) findViewById(R.id.progressBarComparisonQuestionnaire_Step2);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutComparison_Questionnaire_Step2);

        btnComparison_Submit = (Button) findViewById(R.id.btnComparison_Submit);
        btnComparison_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComparison_Questionnaire();
            }
        });

        btnComparison_Cancel = (Button) findViewById(R.id.btnComparison_Cancel);
        btnComparison_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        comparison_quest6 = (Spinner) findViewById(R.id.comparison_quest6);
        comparison_quest7 = (Spinner) findViewById(R.id.comparison_quest7);
        comparison_quest8 = (Spinner) findViewById(R.id.comparison_quest8);
        comparison_quest9 = (Spinner) findViewById(R.id.comparison_quest9);
        comparison_quest10 = (Spinner) findViewById(R.id.comparison_quest10);
    }

    private void submitComparison_Questionnaire(){

        comparisonQuestionnaireDAO = new ComparisonQuestionnaireDAO();
        comparisonQuestionnaireDAO.setUserID(AutoLogin.getUserID(AutoLogin
                .getSettingsFile(getApplicationContext())));
        comparisonQuestionnaireDAO.setQuestion1(comparison_quest1);
        comparisonQuestionnaireDAO.setQuestion2(comparison_quest2);
        comparisonQuestionnaireDAO.setQuestion3(comparison_quest3);
        comparisonQuestionnaireDAO.setQuestion4(comparison_quest4);
        comparisonQuestionnaireDAO.setQuestion5(comparison_quest5);
        comparisonQuestionnaireDAO.setQuestion6(comparison_quest6.getSelectedItem().toString());
        comparisonQuestionnaireDAO.setQuestion7(comparison_quest7.getSelectedItem().toString());
        comparisonQuestionnaireDAO.setQuestion8(comparison_quest8.getSelectedItem().toString());
        comparisonQuestionnaireDAO.setQuestion9(comparison_quest9.getSelectedItem().toString());
        comparisonQuestionnaireDAO.setQuestion10(comparison_quest10.getSelectedItem().toString());


        System.out.println("comparison_quest1: " + comparison_quest1);
        System.out.println("comparison_quest2: " + comparison_quest2);
        System.out.println("comparison_quest3: " + comparison_quest3);
        System.out.println("comparison_quest4 " + comparison_quest4);
        System.out.println("comparison_quest5: " + comparison_quest5);
        System.out.println("comparison_quest6: " + comparison_quest6.getSelectedItem().toString());
        System.out.println("comparison_quest7: " + comparison_quest7.getSelectedItem().toString());
        System.out.println("comparison_quest8: " + comparison_quest8.getSelectedItem().toString());
        System.out.println("comparison_quest9: " + comparison_quest9.getSelectedItem().toString());
        System.out.println("comparison_quest10: " + comparison_quest10.getSelectedItem().toString());

        StoreComparisonQuestionnaire storeComparisonQuestionnaire = new StoreComparisonQuestionnaire(getApplicationContext(), this, comparisonQuestionnaireDAO);

    }

    /**
     * Handles the result of the Study API - Comparison Questionnaire
     *
     * @param result
     */
    public void comparisonQuestionnaireResult(String result) {

       String comparison_id_out;

        try {
            JSONObject jObject = new JSONObject(result);
            comparison_id_out = jObject.getString("comparison_id_out");

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    ComparisonQuestionnaire_Step2.this);
            alertDialogBuilder.setTitle("Comparison Questionnaire");

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
            System.out.println("comparison_id_out:" + comparison_id_out);
        } catch (JSONException e) {
            Log.e("Parse result", e.getLocalizedMessage());
        }
    }


}
