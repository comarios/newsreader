package com.ucl.news.api;

import java.sql.Timestamp;
import java.util.Date;

public class RegistrationDAO {

    private String fName;
    private String lName;
    private String gender;
    private String dob;
    private String email_address;
    private String password;
    private String q1_frequency;
    private String q2_readingtime;
    private String q3_browsingstrategy;
    private String q4_readingstyle;
    private String q5_location;

    public String getQ1_frequency() {
        return q1_frequency;
    }

    public void setQ1_frequency(String q1_frequency) {
        this.q1_frequency = q1_frequency;
    }

    public String getQ2_readingtime() {
        return q2_readingtime;
    }

    public void setQ2_readingtime(String q2_readingtime) {
        this.q2_readingtime = q2_readingtime;
    }

    public String getQ3_browsingstrategy() {
        return q3_browsingstrategy;
    }

    public void setQ3_browsingstrategy(String q3_browsingstrategy) {
        this.q3_browsingstrategy = q3_browsingstrategy;
    }

    public String getQ4_readingstyle() {
        return q4_readingstyle;
    }

    public void setQ4_readingstyle(String q4_readingstyle) {
        this.q4_readingstyle = q4_readingstyle;
    }

    public String getQ5_location() {
        return q5_location;
    }

    public void setQ5_location(String q5_location) {
        this.q5_location = q5_location;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
