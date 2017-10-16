package com.ucl.news.utils;

/**
 * Created by marios on 22/07/17.
 */
public class Constants {

//    public static final String SERVER = "http://habito.cs.ucl.ac.uk:9000/";
//    public static final String SERVER = "http://10.97.73.185:9000/";
//    public static final String SERVER = "http://192.168.0.7:9000/";

    public static final String SERVER = "http://192.168.0.7:9000/";
//    public static final String SERVER = "http://128.16.64.207:9000/";
    public static final String USERS_API = "users/";
    public static final String NEWS_BEHAVIOR_API = "newsbehavior/";
    public static final String UM_API = "um/";
    public static final String STUDY_API = "study/";

    //USERS_API Methods
    public static final String LOGIN = "login";
    public static final String ADD_USER = "addUser";
    public static final String IS_EMAIL_UNIQUE = "isEmailUnique";

    //NEWS_BEHAVIOR_API Methods
    public static final String READING = "storeReadingBehavior";
    public static final String READING_SCROLL = "storeReadingScroll";
    public static final String NAVIGATION = "storeNavigationBehavior";
    public static final String NAVIGATION_METADATA = "storeNavigationalMetaData";
    public static final String RUNNING_NEWS_APPS = "storeRunningNewsApps";

    //UM_API Methods
    public static final String NEWS_READER_TYPE = "getNewsReaderType";

    //EVALUATION STUDY Methods
    public static final String SUS_QUESTIONNAIRE = "storeSUSQuestionnaire";
    public static final String Comparison_QUESTIONNAIRE = "storeComparisonQuestionnaire";
    public static final String POST_StudyInformation = "storeStudyInformation";
    public static final String GET_StudyInformation = "getStudyInformation";

}
