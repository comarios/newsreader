package com.ucl.adaptationmechanism;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import com.ucl.news.utils.Range;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by danyaalmasood on 17/11/2016.
 */

public class RuleLoader {

    private static final String ns = null;

    private List<Rule> rules = new ArrayList<>();

    private Context context;

    public RuleLoader(Context context){
        this.context = context;
    }

    //Returns a list of all the rules in rules.xml
    public List<Rule> getRules() throws XmlPullParserException, IOException {
        InputStream inputstream = context.getAssets().open("rules.xml");
        rules = handleParserSetup(inputstream);
        return rules;
    }

    //Setup XML parser
    public List<Rule> handleParserSetup(InputStream inputStream) throws XmlPullParserException, IOException{
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return parseXML(parser);
        } finally {
            inputStream.close();
        }
    }

    //Parse the rules.xml file
    public List<Rule> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException{

        List<Rule> rules = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "rules");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("rule")) {
                Rule r = readEntry(parser);
                rules.add(r);
            } else {
                skip(parser);
            }
        }

        return rules;

    }

    //Create a rule object from each entry and return it to be added to the rules list
    private Rule readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "rule");
        String ruleName = null;
        ArrayList<Range> ranges = new ArrayList<>();
        ArrayList<String> featureList = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                ruleName = readName(parser);
            } else if (name.equals("trackerPercent")) {
                String[] range = readTracker(parser).split("-");
                ranges.add(new Range(Float.valueOf(range[0]), Float.valueOf(range[1])));
            } else if (name.equals("reviewerPercent")) {
                String[] range = readReviewer(parser).split("-");
                ranges.add(new Range(Float.valueOf(range[0]), Float.valueOf(range[1])));
            } else if (name.equals("dipperPercent")) {
                String[] range = readDipper(parser).split("-");
                ranges.add(new Range(Float.valueOf(range[0]), Float.valueOf(range[1])));
            } else if (name.equals("features")) {
                featureList = readFeatures(parser);
            } else {
                skip(parser);
            }
        }
        return new Rule(ruleName, ranges, featureList);
    }

    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    private String readTracker(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "trackerPercent");
        String trackerPercent = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "trackerPercent");
        return trackerPercent;
    }

    private String readReviewer(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "reviewerPercent");
        String reviewerPercent = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "reviewerPercent");
        return reviewerPercent;
    }

    private String readDipper(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "dipperPercent");
        String dipperPercent = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "dipperPercent");
        return dipperPercent;
    }

    //Add all the features for each rule to a list and return it
    private ArrayList<String> readFeatures(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<String> featureList = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "features");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("feature")) {
                String feature = readFeature(parser);
                featureList.add(feature);
            } else {
                skip(parser);
            }
        }

        return featureList;
    }

    private String readFeature(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "feature");
        String feature = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "feature");
        return feature;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static class Rule {

        private String ruleName;
        private List<Range> ranges;
        private ArrayList<String> featureList;

        public Rule(String ruleName, List<Range> ranges, ArrayList<String> featureList) {
            this.ruleName = ruleName;
            this.ranges = ranges;
            this.featureList = featureList;
        }

        public String getRuleName(){
            return ruleName;
        }

        public List<Range> getRanges() {
            return ranges;
        }

        public ArrayList<String> getFeatureList() {
            return featureList;
        }
    }

}