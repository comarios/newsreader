package com.ucl.study;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.ucl.newsreader.R;

public class ComparisonQuestionnaire_Step1 extends Activity {

    private ProgressBar progressBarQuestionnaire;
    private CoordinatorLayout coordinatorLayout;
    private Button mBtnComparisonNextStep;

    private Spinner comparison_quest1;
    private Spinner comparison_quest2;
    private Spinner comparison_quest3;
    private Spinner comparison_quest4;
    private Spinner comparison_quest5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_comparison_questionnaire_step1);

        initViews();
    }

    private void initViews() {
        progressBarQuestionnaire = (ProgressBar) findViewById(R.id.progressBarSUSQuestionnaire_Step1);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSUS_Questionnaire_Step1);
        mBtnComparisonNextStep = (Button) findViewById(R.id.btn_comparison_nextStep);
        mBtnComparisonNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep();
            }
        });
        comparison_quest1 = (Spinner) findViewById(R.id.comparison_quest1);
        comparison_quest2 = (Spinner) findViewById(R.id.comparison_quest2);
        comparison_quest3 = (Spinner) findViewById(R.id.comparison_quest3);
        comparison_quest4 = (Spinner) findViewById(R.id.comparison_quest4);
        comparison_quest5 = (Spinner) findViewById(R.id.comparison_quest5);
    }

    private void nextStep(){

        Intent i = new Intent(ComparisonQuestionnaire_Step1.this, ComparisonQuestionnaire_Step2.class);
        i.putExtra("comparison_quest1", comparison_quest1.getSelectedItem().toString());
        i.putExtra("comparison_quest2", comparison_quest2.getSelectedItem().toString());
        i.putExtra("comparison_quest3", comparison_quest3.getSelectedItem().toString());
        i.putExtra("comparison_quest4", comparison_quest4.getSelectedItem().toString());
        i.putExtra("comparison_quest5", comparison_quest5.getSelectedItem().toString());
        startActivity(i);
        System.out.println("comparison_quest1: " + comparison_quest1.getSelectedItem().toString());
        System.out.println("comparison_quest2: " + comparison_quest2.getSelectedItem().toString());
        System.out.println("comparison_quest3: " + comparison_quest3.getSelectedItem().toString());
        System.out.println("comparison_quest4: " + comparison_quest4.getSelectedItem().toString());
        System.out.println("comparison_quest4: " + comparison_quest5.getSelectedItem().toString());
    }

}
