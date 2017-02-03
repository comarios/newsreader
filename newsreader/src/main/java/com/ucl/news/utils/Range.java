package com.ucl.news.utils;

/**
 * Created by danyaalmasood on 28/01/2017.
 */

//Class to create percentages ranges for rules
public class Range {
    private float lower;
    private float upper;

    public Range(float lower, float upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public boolean contains(float val) {
        if (val >= lower && val <= upper) {
            return true;
        } else {
            return false;
        }
    }
}
