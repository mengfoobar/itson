package flyapp.its_on;

import static flyapp.its_on.CommonUtilities.SENDER_ID;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ActivityRegistration extends Activity {


    private static String URL_REGISTER_USER;
    private static String URL_UPLOAD_DP;
    private static String URL_UPLOAD_COVER;
    private Bitmap profileDp;
    private Bitmap profileCover;
    private String userId;

    private EditText user, pass, name, email;

    private static String regId;

    private GoogleCloudMessaging gcm;

    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SUCCESS = "success";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        URL_REGISTER_USER = getResources().getString(R.string.ipaddress) +"register.php";
        URL_UPLOAD_DP=getResources().getString(R.string.ipaddress)+"display_picture/uploadimage.php";
        URL_UPLOAD_COVER=getResources().getString(R.string.ipaddress)+"cover_picture/uploadimage.php";

        ActionBar bar = getActionBar();
        bar.hide();
    }

    public void Register(View v) {
        new AttemptRegister().execute();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void TakePhoto(View v) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    private static int RESULT_LOAD_IMAGE = 2;

    public void retrieveCoverImage(View v)
    {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profileDp=imageBitmap;
        }else if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){
            ImageHandler imageHandler=new ImageHandler();
            profileCover=imageHandler.decodeImageFromGallery(data, this.getApplicationContext());
        }
    }


    class AttemptRegister extends AsyncTask<String, String, String> {
        int success=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag


            user = (EditText) findViewById(R.id.et_regUsername);
            pass = (EditText) findViewById(R.id.et_regPassword);
            name = (EditText) findViewById(R.id.et_regName);
            email = (EditText) findViewById(R.id.et_regEmail);

            String user_text = user.getText().toString().trim();
            String pass_text = pass.getText().toString().trim();
            String name_text = name.getText().toString().trim();
            String email_text = email.getText().toString().trim();

            if(user_text.isEmpty()||pass_text.isEmpty()||name_text.isEmpty()||email_text.isEmpty()){
                return "Please Enter all fields";
            }

            if(profileDp==null){
                return "Display Picture Needed";
            }
            if(profileCover==null){
                return "Please Select a Cover Photo";
            }

            try {
                try{
                    if(checkPlayServices()) {
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                        }
                        regId = gcm.register(SENDER_ID);
                    }

                } catch (IOException ex) {
                    return ex.getMessage();
                }

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", user_text));
                params.add(new BasicNameValuePair("password", pass_text));
                params.add(new BasicNameValuePair("name", name_text));
                params.add(new BasicNameValuePair("email", email_text));
                params.add(new BasicNameValuePair("gcm_reg_id", regId));

                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_REGISTER_USER, "POST", params);

                success = json.getInt(TAG_SUCCESS);
                userId=json.getString("user_id");

                ImageHandler imageHandler=new ImageHandler();

                imageHandler.UploadImage(profileDp, userId, URL_UPLOAD_DP);
                imageHandler.UploadImage(profileCover, userId, URL_UPLOAD_COVER);

                if (success == 1) {
                    return json.getString(TAG_MESSAGE);
                } else {
                    return json.getString(TAG_MESSAGE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            Toast.makeText(ActivityRegistration.this, file_url, Toast.LENGTH_SHORT).show();
            if (success==1) {
                Intent nextScreen = new Intent(getApplicationContext(), ActivityProfileHome.class);
                finish();
                startActivity(nextScreen);
            }
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(ActivityRegistration.this, "Device not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }




}
