package flyapp.its_on;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static flyapp.its_on.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static flyapp.its_on.CommonUtilities.EXTRA_MESSAGE;


public class ActivityProfileHome extends Activity {

    private static UserSession userSession;  //usersession to check sharedpreferences

    private static String URL_FILL_PROFILE;    //link to php file used to fill the Profile page with challenges

    private static final String PHPTAG_SUCCESS = "success";    //string to get integer value of success
    private static final String PHPTAG_IS_CHALLENGE_REQUEST_AVAILABLE ="is_challenge_request_available";
    private static final String PHPTAG_ARCHIVED ="archived";
    private static final String PHPTAG_CHALLENGES_AVAILABLE ="is_challenge_available";
    private static final String PHPTAG_CHALLENGE_INFO="challenge_info";


    private static List<Challenge> challengeList;   //list to hold all the challenges retrieved from server
    private static ChallengeAdapter challengeListAdapter;   //custom adapter for the listview displaying the challenges

    public static Challenge selectedChallenge;  //when a challenge is selected, this variable is
                                                // used to pass information to ChallengeMain Screen
    private Bitmap dpBitmap;
    private Bitmap coverBitmap;
    private ProgressDialog progressDialog;      //dialog used to show task is running

    private int isChallengeRequestAvailable=0;

    private ListView challengesListview;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_home);

        //remove title from actionbar
        setTitle("");

        //populate the address for the profile
        URL_FILL_PROFILE = getResources().getString(R.string.ipaddress)+ "fillprofile.php";

        //checks if user is logged in. if not, directs them to login page
        userSession = new UserSession(getApplicationContext());
        userSession.checkLogin();

        //sets the custom list item adapter to the listview
        headerView = (View)getLayoutInflater().inflate(R.layout.headerview_profilehome,null,false);
        challengesListview = (ListView) findViewById(R.id.lv_challenges);

        challengesListview.addHeaderView(headerView);

        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //if user is logged in and the user_id is valid, then Load the Profile
        if(userSession.getUserId()!=-1) {
            new LoadProfile().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_home, menu);
        if(isChallengeRequestAvailable==0) {
            menu.getItem(2).setVisible(false);
        }else{
            menu.getItem(2).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addchallenge:
                Intent addChallengeScreen = new Intent(getApplicationContext(), ActivityNewChallenge.class);
                startActivityForResult(addChallengeScreen,1);
                return true;
            case R.id.action_addFriend:
                Intent friendScreen = new Intent(getApplicationContext(), ActivityFriendsPage.class);
                startActivity(friendScreen);
                return true;
            case R.id.action_seeRequests:
                Intent challengeRequestScreen = new Intent(getApplicationContext(), ActivityChallengeRequests.class);
                startActivity(challengeRequestScreen);
                return true;
            case R.id.action_archived:
                Intent archivScreen = new Intent(getApplicationContext(), ActivityArchive.class);
                startActivity(archivScreen);
                return true;
            case R.id.action_logout:
                userSession.logoutUser();
                Intent loginScreen = new Intent(getApplicationContext(), ActivitySignInScreen.class);
                startActivity(loginScreen);
                return true;
            case R.id.action_profile_settings:
                Intent settingScreen=new Intent(getApplicationContext(), ActivityProfileSettings.class);
                startActivity(settingScreen);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Custom Adapter for the challenges Listview
    public class ChallengeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return challengeList.size();
        }

        @Override
        public Challenge getItem(int arg0) {
            return challengeList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            //NOTE: all the information here is populated from the challenge variable on challengeList

            //Inflate the layout listitem_challenge for each list item
            if(arg1==null)
            {
                LayoutInflater inflater = (LayoutInflater) ActivityProfileHome.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem_challenge, arg2,false);
            }

            //retrieve currently selected challenge
            final Challenge challenge = challengeList.get(arg0);

            //popualte name of challenge into listitem textview
            TextView challengeName = (TextView)arg1.findViewById(R.id.tv_profileChlgTitle);
            challengeName.setText(challenge.getName());

            //sets the imagview of the challenge cover photo
            ImageView challengeCover=(ImageView)arg1.findViewById(R.id.iv_profileChlgCover);
            challengeCover.setImageBitmap(challenge.getCoverBitmap());

            //TODO: check why each challenge is called twice
            final ImageView check=(ImageView)arg1.findViewById(R.id.iv_update);

            if(challenge.getTodayUpdateStatus()==-1) {
                check.setImageDrawable(getResources().getDrawable(R.drawable.icon_update_disabled));
                challengeName.setTextColor(Color.GRAY);
                challengeCover.setColorFilter(Color.GRAY, PorterDuff.Mode.DARKEN);
            }else if(challenge.getTodayUpdateStatus()==1){
                check.setImageDrawable(getResources().getDrawable(R.drawable.icon_updated));
            }else if(challenge.getTodayUpdateStatus()==0){
                challengeCover.clearColorFilter();
                challengeName.setTextColor(Color.WHITE);
                check.setImageDrawable(getResources().getDrawable(R.drawable.icon_update));
            }


            //Listener event for when the challenge is clicked
            challengeCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                          selectedChallenge=challenge;
                          Intent nextScreen = new Intent(getApplicationContext(), ActivityChallengeMainPage.class);
                          startActivity(nextScreen);
                    }
                }
            );

            //Listener event when the check icon has been clicked
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(challenge.getTodayUpdateStatus()==0)
                    {
                        //populates the dialog to allow user to enter a message for the update
                        final Dialog dialog = new Dialog(ActivityProfileHome.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_updatechallenge);

                        TextView upd_name = (TextView) dialog.findViewById(R.id.tv_updateDialogChlgTitle);
                        upd_name.setText(challenge.getName());

                        TextView upd_desc = (TextView) dialog.findViewById(R.id.tv_updateDialogChlgDesc);
                        upd_desc.setText(challenge.getDescription());

                        //HL: defining text inputbox, buttons from the dialog
                        Button btn_upd = (Button) dialog.findViewById(R.id.btn_updateDialogCommit);
                        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_updateDialogCancel);

                        //shows dialog
                        dialog.show();

                        //update button listener event. gets string from edit text box
                        btn_upd.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                EditText etUpdateMessage = (EditText) dialog.findViewById(R.id.et_updateDialogMsg);
                                //sole purpose is update the check image
                                challengeList.get(arg0).updateToday(etUpdateMessage.getText().toString());
                                challengeListAdapter.notifyDataSetChanged();
                                //TODO: see if adapter will update
                                //challenge.updateTodayOnServer();
                                dialog.dismiss();
                            }

                        });

                        //HL: No Message-> exits window without retrieving text
                        btn_cancel.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                }
            }
            );

            return arg1;
        }

        public Challenge getChallenge(int position)
        {
            return challengeList.get(position);
        }

    }

    //Task to load the user information and challenges from server
    public class LoadProfile extends AsyncTask<Void, Void, Boolean> {

        int success;
        int challengeAvailable;
        int archivedChallenges;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //start loading dialog while accessing server
            progressDialog = ProgressDialog.show(ActivityProfileHome.this,"Loading...",
                    "Loading Page", false, false);
        }
        @Override
        protected Boolean doInBackground(Void... arg0) {

            //refresh challengeList as we are repopulating the challenges
            challengeList = new ArrayList<Challenge>();

            try {
                //POST paramters to input into PHP
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user_id",Integer.toString(userSession.getUserId())));

                //JSON parcer to retrieve response from PHP
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_FILL_PROFILE, "POST", params);

                //variable to check if HttpRequest was successful
                success = json.getInt(PHPTAG_SUCCESS);
                challengeAvailable=json.getInt(PHPTAG_CHALLENGES_AVAILABLE);
                archivedChallenges=json.getInt(PHPTAG_ARCHIVED);
                isChallengeRequestAvailable=json.getInt(PHPTAG_IS_CHALLENGE_REQUEST_AVAILABLE);

                if (success == 1) {

                    //custom image handler class
                    ImageHandler imageHandler=new ImageHandler();
                    //loads the dp image by retrieving the url from JSON Object
                    dpBitmap=userSession.getDisplayImage();
                    coverBitmap=userSession.getCoverImage();

                    if(dpBitmap==null){
                        dpBitmap= imageHandler.loadImageFromUrl(userSession.getDisplayPictureUrl());
                        userSession.setDisplayImage(dpBitmap);
                    }
                    if(coverBitmap==null) {
                        coverBitmap = imageHandler.loadImageFromUrl(userSession.getCoverPictureUrl());
                        userSession.setCoverImage(coverBitmap);
                    }

                    //container for JSON string
                    JSONArray chlgJsonArray = null;
                    Challenge challenge;
                    List<List<Challenge>> sortedChallenges = new ArrayList<List<Challenge>>();

                    sortedChallenges.add(new ArrayList<Challenge>());
                    sortedChallenges.add(new ArrayList<Challenge>());
                    sortedChallenges.add(new ArrayList<Challenge>());


                    if(challengeAvailable==1) {
                        //retrieves challenge information from JSON Object
                        chlgJsonArray = json.getJSONArray(PHPTAG_CHALLENGE_INFO);

                        //creates and populates challenge object based on retrieved info form JSON
                        for (int i = 0; i < chlgJsonArray.length(); i++) {
                            challenge = new Challenge(ActivityProfileHome.this.getApplicationContext());

                            JSONObject challengeInfoJsonObj=chlgJsonArray.getJSONObject(i);

                            if (challenge.loadChallengeInfo(challengeInfoJsonObj)) {
                                if(challenge.getTodayUpdateStatus()==0){
                                    sortedChallenges.get(0).add(challenge);
                                }else if(challenge.getTodayUpdateStatus()==1){
                                    sortedChallenges.get(1).add(challenge);
                                }else if(challenge.getTodayUpdateStatus()==-1){
                                    sortedChallenges.get(2).add(challenge);
                                }
                                //challengeList.add(challenge);
                            }
                        }

                        for(int i=0; i<sortedChallenges.size(); i++){
                            for(Challenge c:sortedChallenges.get(i)){
                                challengeList.add(c);
                            }
                        }
                    }
                    return true;
                } else {
                    //return false indicating failure
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result){
                //refreshes custom adapter for challenge list item
                challengeListAdapter = new ChallengeAdapter();

                //populates the imageview with display picture downloaded
                CircleImageView ivDp=(CircleImageView)headerView.findViewById(R.id.iv_dp);
                ivDp.setImageBitmap(dpBitmap);

                ImageView ivCover=(ImageView)headerView.findViewById(R.id.iv_cover);
                //ivCover.setColorFilter(Color.GRAY, PorterDuff.Mode.DARKEN);
                ivCover.setImageBitmap(coverBitmap);


                TextView tvUsername=(TextView)headerView.findViewById(R.id.tv_dpusername);
                tvUsername.setText(userSession.getUserName());

                challengesListview.setAdapter(challengeListAdapter);

                if(challengeList.size()==0){
                    Toast.makeText(ActivityProfileHome.this,"No Challenges", Toast.LENGTH_SHORT).show();
                    Toast.makeText(ActivityProfileHome.this,"Create a challenge!", Toast.LENGTH_SHORT).show();
                }

                invalidateOptionsMenu ();
                if(archivedChallenges>0){
                    Toast.makeText(ActivityProfileHome.this, Integer.toString(archivedChallenges)
                            +" Challenge(s) Archived", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(ActivityProfileHome.this, "Failed to Access Server", Toast.LENGTH_LONG).show();
            }

            progressDialog.dismiss();
        }
    }


    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());

            /**
             * Take appropriate action on this message
             * depending upon your app requirement
             * For now i am just displaying it on the screen
             * */

            // Showing received message
            Toast.makeText(getApplicationContext(), "New Message: " + newMessage, Toast.LENGTH_LONG).show();

            // Releasing wake lock
            WakeLocker.release();
        }
    };

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }
}
