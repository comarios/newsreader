package com.ucl.news.main;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.ucl.newsreader.R;

public class ConsentForm extends Activity {
    private Button mBtnAgreeConsentForm;
    private Button mBtnNotAgreeConsentForm;
    private ProgressBar progressBarConsentForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_consent_form);

        initViews();

    }

    private void initViews() {
        progressBarConsentForm = (ProgressBar) findViewById(R.id.progressBarConsentForm);
        mBtnAgreeConsentForm = (Button) findViewById(R.id.btnAgree);
        mBtnAgreeConsentForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarConsentForm.setVisibility(View.VISIBLE);
                Intent i = new Intent(ConsentForm.this, RegistrationActivity.class);
                startActivity(i);
                progressBarConsentForm.setVisibility(View.GONE);

            }
        });
        mBtnNotAgreeConsentForm = (Button) findViewById(R.id.btnDontAgree);
        mBtnNotAgreeConsentForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
