package com.ucl.news.main;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ucl.news.api.LoginDAO;
import com.ucl.news.api.LoginHttpPostTask;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.Dialogs;
import com.ucl.news.utils.NetworkConnection;
import com.ucl.newsreader.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private Button mBtnSignIn;
    private Button mBtnSignUp;
    private ProgressBar progressLogin;
    private CoordinatorLayout coordinatorLayout;
    private final String NOT_AUTHORISED_USER = "-1";
    private NetworkConnection network = new NetworkConnection(
            LoginActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (AutoLogin.getIsLoggedIN(AutoLogin
                .getSettingsFile(getApplicationContext()))) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        } else {

            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.login);
            initViews();

            if (!network.haveNetworkConnection()) {
                noNetworkConnectionError();
            }
        }
    }

    private void noNetworkConnectionError(){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_LONG)
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

    private void invalidCredentialsError(){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Invalid credentials", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signIn(mUsername.getText().toString(), mPassword.getText().toString());
                    }
                });

        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void initViews() {

        progressLogin = (ProgressBar) findViewById(R.id.progressBarLogin);
        mUsername = (EditText) findViewById(R.id.txtUsername);
        mPassword = (EditText) findViewById(R.id.txtPassword);

        mUsername.setFocusableInTouchMode(true);
        mUsername.setFocusable(true);
        mUsername.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mUsername.getWindowToken(), 0);

        mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mUsername.getText().toString(), mPassword.getText().toString());
            }
        });
        mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
        mBtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
    }

    /**
     * Handles the result of the authentication api call
     *
     * @param result
     */
    public void authenticate(String result) {

        progressLogin.setVisibility(View.INVISIBLE);

        String userID = null;

        try {
            JSONObject jObject = new JSONObject(result);
            userID = jObject.getString("UserID");
            // Log.e("RES2", userID);
        } catch (JSONException e) {
            Log.e("Parse result", e.getLocalizedMessage());
        }

        if (result.equals("Not valid json")
                || result.equals("Json doesn't contain any values")
                || result.isEmpty() || userID.equals(null)
                || userID.equals(NOT_AUTHORISED_USER)) {

                invalidCredentialsError();

//            new Dialogs().createDialogLoginERROR(LoginActivity.this,
//                    getApplicationContext());
        } else {
            String credentials = "YES" + ";" + userID + ";"
                    + UUID.randomUUID().toString() + ";";
            AutoLogin.saveSettingsFile(getApplicationContext(), credentials);

            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("ref", "LoginCaller");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }


    private void signIn(String username, String password) {
        // Call API to authenticate USER
        LoginDAO users = new LoginDAO();
        users.setEmail_address(username);
        users.setPassword(password);

        progressLogin.setVisibility(View.VISIBLE);
        LoginHttpPostTask hpt = new LoginHttpPostTask(getApplicationContext(),
                this, users);
    }

    private void signUp() {
        progressLogin.setVisibility(View.VISIBLE);
        Intent i = new Intent(LoginActivity.this, ConsentForm.class);
        startActivity(i);
        progressLogin.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
