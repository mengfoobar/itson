package flyapp.its_on;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ActivityProfileSettings extends Activity {
    private static String URL_UPDATE_USER_PROFILE;
    private static String URL_UPLOAD_DP;
    private static String URL_UPLOAD_COVER;

    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SUCCESS = "success";

    private static EditText etUsername, etEmailAddress, etFullName,etPassword;
    private static ImageView ivCover;
    private static CircleImageView ivDp;

    private static Bitmap dp, cover;

    private static int userId;

    private static UserSession userSession;

    private ProgressDialog progressDialog;      //dialog used to show task is running

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_settings, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_settings);
        setTitle("");

        URL_UPDATE_USER_PROFILE = getResources().getString(R.string.ipaddress) +"updateprofile.php";
        URL_UPLOAD_DP=getResources().getString(R.string.ipaddress)+"display_picture/uploadimage.php";
        URL_UPLOAD_COVER=getResources().getString(R.string.ipaddress)+"cover_picture/uploadimage.php";

        userSession=new UserSession(getApplicationContext());

        userId=userSession.getUserId();

        dp=userSession.getDisplayImage();
        cover=userSession.getCoverImage();

        etUsername=(EditText)findViewById(R.id.et_username);
        etUsername.setText(userSession.getUserName());

        etEmailAddress=(EditText)findViewById(R.id.et_emailAddress);
        etEmailAddress.setText(userSession.getUserEmailAddress());

        etFullName=(EditText)findViewById(R.id.et_fullName);
        etFullName.setText(userSession.getUserFullName());

        etPassword=(EditText) findViewById(R.id.et_password);
        etPassword.setText(userSession.getPassword());

        ivCover=(ImageView) findViewById(R.id.iv_cover);
        ivCover.setImageBitmap(cover);

        ivDp=(CircleImageView) findViewById(R.id.iv_dp);
        ivDp.setImageBitmap(dp);
    }

    public void changeCoverPhoto(View v){
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void takePhoto(View v) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    private static int RESULT_LOAD_IMAGE = 2;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            dp=imageBitmap;
            ivDp.setImageBitmap(dp);
        }else if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){
            ImageHandler imageHandler=new ImageHandler();
            cover=imageHandler.decodeImageFromGallery(data, this.getApplicationContext());
            ivCover.setImageBitmap(cover);
        }
    }

    public void updateUserProfile(View v)
    {
        new UpdateProfile().execute();
    }

    class UpdateProfile extends AsyncTask<String, String, String> {
        int success=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ActivityProfileSettings.this,"Loading...",
                    "Loading Page", false, false);
        }

        @Override
        protected String doInBackground(String... args) {

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String emailAddress = etEmailAddress.getText().toString().trim();

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("name", fullName));
                params.add(new BasicNameValuePair("email", emailAddress));

                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_UPDATE_USER_PROFILE, "POST", params);

                success = json.getInt(TAG_SUCCESS);

                ImageHandler imageHandler=new ImageHandler();

                imageHandler.UploadImage(dp, Integer.toString(userId), URL_UPLOAD_DP);
                imageHandler.UploadImage(cover, Integer.toString(userId), URL_UPLOAD_COVER);

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
            Toast.makeText(ActivityProfileSettings.this, file_url, Toast.LENGTH_SHORT).show();

            userSession=new UserSession(getApplicationContext());
            userSession.createLoginSession(userId, etUsername.getText().toString(), etPassword.getText().toString());
            userSession.storeEmailAddress(etEmailAddress.getText().toString());
            userSession.storeUserFullName(etFullName.getText().toString());

            userSession.setCoverImage(cover);
            userSession.setDisplayImage(dp);


            if (success==1) {
                Intent nextScreen = new Intent(getApplicationContext(), ActivityProfileHome.class);
                finish();
                startActivity(nextScreen);
            }

            progressDialog.dismiss();
        }
    }
}
