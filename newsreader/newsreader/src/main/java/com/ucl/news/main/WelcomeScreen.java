package com.ucl.news.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.ucl.news.adaptation.main.MainActivityDippers;
import com.ucl.news.adaptation.main.MainActivityReviewers;
import com.ucl.news.adaptation.main.MainActivityTrackers;
import com.ucl.news.utils.AutoLogin;
import com.ucl.newsreader.R;

public class WelcomeScreen extends Activity {

	private static int SPLASH_TIME_OUT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!AutoLogin.getIsLoggedIN(AutoLogin
				.getSettingsFile(getApplicationContext()))) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.welcome_screen);

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent i = new Intent(WelcomeScreen.this,
							LoginActivity.class);
					startActivity(i);
					finish();
				}
			}, SPLASH_TIME_OUT);
		} else {
//			System.out.println("credentials: "
//					+ AutoLogin.getSettingsFile(getApplicationContext()));
			Intent id = new Intent(this, MainActivity.class);
			id.putExtra("ref", "WelcomeScreenCaller");
			startActivity(id);
		}
	}
}
