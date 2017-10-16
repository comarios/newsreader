package com.ucl.news.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.ucl.news.adaptation.main.MainActivityDippers;
import com.ucl.news.adaptation.main.MainActivityReviewers;
import com.ucl.news.adaptation.main.MainActivityTrackers;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.StudyInformation;
import com.ucl.newsreader.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WelcomeScreen extends Activity {

	private static int SPLASH_TIME_OUT = 2000;
	private StudyInformation studyInformation;
	private String GLOBAL_current_interface;
	private int GLOBAL_days_used_app = 0;
	private String GLOBAL_sus_current_environment_answered;
	private int GLOBAL_is_comparison_questionnaire_answered;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.welcome_screen);

		if (!AutoLogin.getIsLoggedIN(AutoLogin
				.getSettingsFile(getApplicationContext()))) {


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

			String user_session = AutoLogin.getSettingsFile(getApplicationContext());
			long user_id = AutoLogin.getUserID(user_session);

			studyInformation = new StudyInformation(getApplicationContext(), user_id);

			studyInformation.getStudyInformation(user_id, new StudyInformation.VolleyCallback() {
				@Override
				public void onSuccess(String response) {
					System.out.println("volley: " + response);
					parseJSONResponse(response);

					System.out.println("welcome screen");
					System.out.println("global" + GLOBAL_current_interface + "," + GLOBAL_days_used_app + "," + GLOBAL_sus_current_environment_answered + "," + GLOBAL_is_comparison_questionnaire_answered);

					if(GLOBAL_days_used_app < 4){
						Intent id = new Intent(WelcomeScreen.this, MainActivity.class);
						id.putExtra("ref", "WelcomeScreenCaller");
						startActivity(id);
					}else if (GLOBAL_days_used_app == 4){

						handleAdaptation(newReaderTypePercentages, 0);
					}
				}
			});


//			Intent id = new Intent(this, MainActivity.class);
//			id.putExtra("ref", "WelcomeScreenCaller");
//			startActivity(id);
		}
	}


	private void parseJSONResponse(String result){
		String current_interface = null;
		String first_open_app = null;
		String last_open_app = null;
		String sus_current_environment_answered = null;
		String is_comparison_questionnaire_answered = null;

		try {
			JSONObject jObject = new JSONObject(result);
			current_interface = jObject.getString("current_interface_out");
			first_open_app = jObject.getString("first_open_app_out");
			last_open_app = jObject.getString("last_open_app_out");
			sus_current_environment_answered = jObject.getString("sus_current_environment_answered_out");
			is_comparison_questionnaire_answered = jObject.getString("is_comparison_questionnaire_answered_out");

			System.out.println("study info:" + current_interface + ", " + first_open_app + "," + last_open_app);

			GLOBAL_current_interface = current_interface;
			GLOBAL_sus_current_environment_answered = sus_current_environment_answered;
			GLOBAL_is_comparison_questionnaire_answered = Integer.parseInt(is_comparison_questionnaire_answered);

			try {

				SimpleDateFormat dateformat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date startDay = new java.util.Date();
				Date currentDay = new java.util.Date();

				startDay = dateformat.parse(first_open_app);
				currentDay = dateformat.parse(last_open_app);

				// get diff in milliseconds
				long diff = currentDay.getTime() - startDay.getTime();

				int days = (int) (diff / (1000*60*60*24));
				System.out.println("study info diff:" + diff + "," + days);
				GLOBAL_days_used_app = days;

			} catch (Exception e) {
				Log.e("TEST", "Exception", e);
			}

		} catch (JSONException e) {
			Log.e("Parse result", e.getLocalizedMessage());
		}
	}
}
