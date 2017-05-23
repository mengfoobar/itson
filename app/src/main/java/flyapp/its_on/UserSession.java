package flyapp.its_on;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

/**
 * Created by HLiu on 05/08/2014.
 */
public class UserSession {

    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "UserPreference";

    // All Shared Preferences Keys
    public static final String KEY_NAME = "name";
    public static final String KEY_USERID = "user_id";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String KEY_EMAIL_ADDRESS="email_address";
    private static final String KEY_FULLNAME="full_name";
    private static final String KEY_PASSWORD="pass_word";
    private static final String KEY_DP_URL="dp_address";
    private static final String KEY_COVER_URL="cover_address";
    private static final String GCM_REGISTERED_SERVER="gcm_registered_server";
    private static final String GCM_REGISTERED_ID="gcm_registered_id";
    private static final String GCM_REGISTERED_APP_VERSION="gcm_registered_app_version";

    //user information. implement later have it stored and accessed from everywhere
    private static Bitmap displayPicture;
    private static Bitmap coverPicture;



    // Constructor
    public UserSession(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(int id, String name, String password){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        // Storing name in pref
        editor.putString(KEY_NAME, name);
        // Storing id in pref
        editor.putInt(KEY_USERID, id);
        // Storing id in pref
        editor.putString(KEY_PASSWORD, password);
        // commit changes
        editor.commit();
    }

    public void storeEmailAddress(String text){
        editor.putString(KEY_EMAIL_ADDRESS, text);
        editor.commit();
    }

    public void storeUserFullName(String text){
        editor.putString(KEY_FULLNAME, text);
        editor.commit();
    }

    public void storeDpAddress(String text){
        editor.putString(KEY_DP_URL, text);
        editor.commit();
    }

    public void storeCoverAddress(String text){
        editor.putString(KEY_COVER_URL, text);
        editor.commit();
    }

    public void setGCMRegisteredOnServer(){
        editor.putBoolean(GCM_REGISTERED_SERVER, true);
        editor.commit();
    }

    public void setGCMUnegisteredOnServer(){
        editor.putBoolean(GCM_REGISTERED_SERVER, false);
        editor.commit();
    }

    public void storeGCMRegistrationId(String regId){

        editor.putString(GCM_REGISTERED_ID, regId);

        int appVersion=CommonUtilities.getAppVersion(_context);

        if(appVersion!=0) {
            editor.putInt(GCM_REGISTERED_APP_VERSION, appVersion);
        }

        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, ActivityStartScreen.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }
    }

    /**
     * Get stored session data
     * */
    public String getUserName(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        return user.get(UserSession.KEY_NAME);
    }

    /**
     * Get stored session data
     * */
    public String getPassword(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        return user.get(UserSession.KEY_PASSWORD);
    }

    /**
     * Get stored session data
     * */
    public Integer getUserId(){
        HashMap<String, Integer> userID = new HashMap<String, Integer>();
        userID.put(KEY_USERID, pref.getInt(KEY_USERID, -1));
        return userID.get(UserSession.KEY_USERID);
    }

    /**
     * Get stored session data
     * */
    public String getUserFullName(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_FULLNAME, pref.getString(KEY_FULLNAME, null));
        return user.get(UserSession.KEY_FULLNAME);
    }

    /**
     * Get stored session data
     * */
    public String getUserEmailAddress(){
        HashMap<String, String> userEmail = new HashMap<String, String>();
        userEmail.put(KEY_EMAIL_ADDRESS, pref.getString(KEY_EMAIL_ADDRESS, null));
        return userEmail.get(UserSession.KEY_EMAIL_ADDRESS);
    }

    /**
     * Get stored session data
     * */
    public String getDisplayPictureUrl(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_DP_URL, pref.getString(KEY_DP_URL, null));
        return user.get(UserSession.KEY_DP_URL);
    }

    /**
     * Get stored session data
     * */
    public String getCoverPictureUrl(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_COVER_URL, pref.getString(KEY_COVER_URL, null));
        return user.get(UserSession.KEY_COVER_URL);
    }

    /**
     * Get stored session data
     * */
    public Boolean checkGCMRegisteredOnServer(){
        return pref.getBoolean(GCM_REGISTERED_SERVER, false);
    }


    /**
     * Get stored session data
     * */
    public String getGCMRegisteredId(){
        return pref.getString(GCM_REGISTERED_ID, "");
    }

    /**
     * Get stored session data
     * */
    public int getGCMRegisteredAppVersion(){
        return pref.getInt(GCM_REGISTERED_APP_VERSION, 0);
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, ActivityStartScreen.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void setDisplayImage (Bitmap image){
        displayPicture=image;
    }

    public void setCoverImage (Bitmap image){
        coverPicture=image;
    }

    public Bitmap getDisplayImage (){
        return displayPicture;
    }

    public Bitmap getCoverImage (){
        return coverPicture;
    }


}
