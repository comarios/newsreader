package com.ucl.study;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.ucl.newsreader.R;

public class SUS_Questionnaire_Step1 extends Activity {

    private ProgressBar progressBarQuestionnaire;
    private CoordinatorLayout coordinatorLayout;
    private Button mBtnSUSNextStep;

    private Spinner sus_quest1;
    private Spinner sus_quest2;
    private Spinner sus_quest3;
    private Spinner sus_quest4;
    private Spinner sus_quest5;
    private String environment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sus_questionnaire_step1);

        initViews();
    }

    private void initViews() {
        progressBarQuestionnaire = (ProgressBar) findViewById(R.id.progressBarSUSQuestionnaire_Step1);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSUS_Questionnaire_Step1);
        mBtnSUSNextStep = (Button) findViewById(R.id.btn_sus_nextStep);
        mBtnSUSNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep();
            }
        });
        sus_quest1 = (Spinner) findViewById(R.id.sus_quest1);
        sus_quest2 = (Spinner) findViewById(R.id.sus_quest2);
        sus_quest3 = (Spinner) findViewById(R.id.sus_quest3);
        sus_quest4 = (Spinner) findViewById(R.id.sus_quest4);
        sus_quest5 = (Spinner) findViewById(R.id.sus_quest5);

        Intent intent = getIntent();
        environment = intent.getStringExtra("environment");
    }

    private void nextStep(){

        Intent i = new Intent(SUS_Questionnaire_Step1.this, SUS_Questionnaire_Step2.class);
        i.putExtra("sus_quest1", sus_quest1.getSelectedItem().toString());
        i.putExtra("sus_quest2", sus_quest2.getSelectedItem().toString());
        i.putExtra("sus_quest3", sus_quest3.getSelectedItem().toString());
        i.putExtra("sus_quest4", sus_quest4.getSelectedItem().toString());
        i.putExtra("sus_quest5", sus_quest5.getSelectedItem().toString());
        i.putExtra("environment", environment);
        startActivity(i);
        System.out.println("sus_quest1: " + sus_quest1.getSelectedItem().toString());
        System.out.println("sus_quest2: " + sus_quest2.getSelectedItem().toString());
        System.out.println("sus_quest3: " + sus_quest3.getSelectedItem().toString());
        System.out.println("sus_quest4: " + sus_quest4.getSelectedItem().toString());
        System.out.println("sus_quest5: " + sus_quest5.getSelectedItem().toString());
    }

}
