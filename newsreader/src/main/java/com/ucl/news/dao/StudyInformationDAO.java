package com.ucl.news.dao;

/**
 * Created by marios on 07/09/17.
 */
public class StudyInformationDAO {
    private long userID;
    private String current_interface;
    private String last_open_app;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getCurrent_interface() {
        return current_interface;
    }

    public void setCurrent_interface(String current_interface) {
        this.current_interface = current_interface;
    }

    public String getLast_open_app() {
        return last_open_app;
    }

    public void setLast_open_app(String last_open_app) {
        this.last_open_app = last_open_app;
    }
}
