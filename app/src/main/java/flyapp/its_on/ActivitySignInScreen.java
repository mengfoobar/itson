package flyapp.its_on;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ActivitySignInScreen extends Activity {
;
    private static String LOGIN_URL;
    private static final String TAG_POSTS = "posts";    //string for retrieving POST data from php
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SUCCESS = "success";

    private static final String PHP_TAG_USER_ID ="user_id";
    private static final String PHP_TAG_USERNAME ="username";
    private static final String PHP_TAG_FULL_NAME ="fullname";
    private static final String PHP_TAG_REG_ID="gcm_reg_id";
    private static final String PHP_TAG_USER_EMAIL_ADDRESS ="emailaddr";
    private static final String PHPTAG_DP_URL="dp_url";
    private static final String PHPTAG_COVER_URL="cover_url";
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);
        LOGIN_URL = getResources().getString(R.string.ipaddress)+"login.php";
        userSession=new UserSession(getApplicationContext());
        ActionBar bar = getActionBar();
        bar.hide();
    }


    public void SignIn(View v)
    {
        new AttemptLogin().execute();
    }

    private EditText user, pass;

    UserSession userSession;





    class AttemptLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //start loading dialog while accessing server
            progressDialog = ProgressDialog.show(ActivitySignInScreen.this, "Loading...",
                    "Loading Page", false, false);
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;


            user = (EditText)findViewById(R.id.et_signinUsername);
            pass = (EditText)findViewById(R.id.et_signinPassword);


            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", user.getText().toString()));
                params.add(new BasicNameValuePair("password", pass.getText().toString()));
                //TODO: modify  the PHP code so that it checks for password as well


                Log.d("request!", "starting");

                JSONParser jsonParser = new JSONParser();
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {



                    populateUserInfoFromJSON();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Login Failure!", json.toString());
                    //Toast.makeText(SignInScreen.this, "Login Fail", Toast.LENGTH_LONG).show();
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            progressDialog.dismiss();
            if (file_url != null){
                Toast.makeText(ActivitySignInScreen.this, file_url, Toast.LENGTH_SHORT).show();
                Intent nextScreen = new Intent(getApplicationContext(), ActivityProfileHome.class);
                finish();
                startActivity(nextScreen);
            }
        }

    }

    public void populateUserInfoFromJSON() {

        // Instantiate JSONParser to parse JSON data
        JSONParser jParser = new JSONParser();
        //Retrieves an object from php file as a JSON obj
        JSONObject json = jParser.getJSONFromUrl(LOGIN_URL);
        //Used to contain the data form JSON Obj as strings
        JSONArray sqlData = null;

        try {
            ImageHandler imageHandler=new ImageHandler();
            sqlData = json.getJSONArray(TAG_POSTS);
            int user_id=sqlData.getJSONObject(0).getInt(PHP_TAG_USER_ID);
            String emailAddress=sqlData.getJSONObject(0).getString(PHP_TAG_USER_EMAIL_ADDRESS);
            String fullName=sqlData.getJSONObject(0).getString(PHP_TAG_FULL_NAME);
            String gcmRegId=sqlData.getJSONObject(0).getString(PHP_TAG_REG_ID);

            //loads the dp image by retrieving the url from JSON Object
            userSession.createLoginSession(user_id, user.getText().toString(), pass.getText().toString());
            userSession.storeDpAddress(sqlData.getJSONObject(0).getString(PHPTAG_DP_URL));
            userSession.storeCoverAddress(sqlData.getJSONObject(0).getString(PHPTAG_COVER_URL));
            Bitmap dpBitmap= imageHandler.loadImageFromUrl(userSession.getDisplayPictureUrl());
            userSession.setDisplayImage(dpBitmap);

            Bitmap coverBitmap = imageHandler.loadImageFromUrl(userSession.getCoverPictureUrl());
            userSession.setCoverImage(coverBitmap);

            userSession.storeUserFullName(fullName);
            userSession.storeEmailAddress(emailAddress);
            userSession.storeGCMRegistrationId(gcmRegId);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
