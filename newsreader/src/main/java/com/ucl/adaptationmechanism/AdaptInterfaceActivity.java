package com.ucl.adaptationmechanism;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ucl.news.adaptation.main.MainActivityDippers;
import com.ucl.news.adaptation.main.MainActivityReviewers;
import com.ucl.news.adaptation.main.MainActivityTrackers;
import com.ucl.newsreader.R;

import java.util.ArrayList;

public class AdaptInterfaceActivity extends AppCompatActivity {

    private static ArrayList<String> readingLevel;
    private String type;
    private boolean topDipper = false;
    private boolean topTracker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapt_interface);

        //Get the list of features from MainActivity intent
        Intent i = getIntent();
        ArrayList<String> featureList = i.getStringArrayListExtra("featureList");
        readingLevel = new ArrayList<>();

        //Create the correct layout based on the feature list
        generateLayout(featureList);

    }

    public void generateLayout(ArrayList<String> fList){
        for(String s:fList){
            switch (s){
                //Search box
                case "dipperTop":
                    topDipper = true;
                    break;
                //Tracked articles
                case "trackerTop":
                    topTracker = true;
                    break;
                case "trackerLayout":
                    type = "Trackers";
                    break;
                case "reviewerLayout":
                    type = "Reviewers";
                    break;
                case "dipperLayout":
                    type = "Dippers";
                    break;
                //All the reading level features + push notifications
                default:
                    readingLevel.add(s);
                    break;
            }

        }
        createLayout(type);
    }

    //Check base layout type from set type and start the correct activity with set features
    public void createLayout(String type){
        Intent intent;
        if (type.equals("Trackers")) {
            intent = new Intent(this, MainActivityTrackers.class);
        } else if (type.equals("Reviewers")) {
            intent = new Intent(this, MainActivityReviewers.class);
        } else {
            intent = new Intent(this, MainActivityDippers.class);
        }
        intent.putExtra("trackerflag", topTracker);
        intent.putExtra("dipperflag", topDipper);
        intent.putExtra("readingFeatures", readingLevel);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.startActivity(intent);
        finish();


    }

    public static ArrayList<String> getReadingLevelFeatures(){
        return readingLevel;
    }
}
