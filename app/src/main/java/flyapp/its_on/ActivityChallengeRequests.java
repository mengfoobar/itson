package flyapp.its_on;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityChallengeRequests extends Activity {

    private static String URL_FILL_CHALLENGE_REQUEST;      //url for php script that retrieves challenge requests
    private static String URL_CONFIRM_CHALLENGE_REQUEST;    //url for php script that confirm challenge requests

    private static final String PHPTAG_SUCCESS = "success";    //string to get integer value of success
    private static final String PHPTAG_POSTS = "challenge_info";    //string for retrieving POST data from php
    private static final String PHPTAG_MESSAGE = "message";    //key string used to retrieve JSON msgs from PHP
    private static final String PHPTAG_USERID = "user_id";
    private static final String PHPTAG_CHALLENGEID="challenge_id";

    UserSession userSession;     //usersession to access user shared preferences
    private ChallengeRequestAdapter challengeAdapter;  //adapter for challenge request list item
    private List<Challenge> challengeRequestList;      //list used to store the challenge requests from server

    private Challenge selectedChallenge;

    private ProgressDialog progressDialog;      //dialog used to show task is running

    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_requests);

        //instantiate the userSession
        userSession = new UserSession(this.getApplicationContext());
        mContext=getApplicationContext();

        //retrieve the url for
        URL_FILL_CHALLENGE_REQUEST = getResources().getString(R.string.ipaddress)+ "fillchallengerequests.php";
        URL_CONFIRM_CHALLENGE_REQUEST= getResources().getString(R.string.ipaddress)+ "confirmchallenge.php";
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refresh the request list to prepare to task
        challengeRequestList=new ArrayList<Challenge>();
        //background task for loading challenge requests from server
        new LoadChallengeRequests().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.challenge_requests, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class LoadChallengeRequests extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //start loading dialog while accessing server
            progressDialog = ProgressDialog.show(ActivityChallengeRequests.this, "Loading...",
                    "Loading Page", false, false);
        }
        @Override
        protected Boolean doInBackground(Void... arg0) {
            int success;
            try {
                //POST paramters to input into PHP
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(PHPTAG_USERID,Integer.toString(userSession.getUserId())));

                //JSON parcer to retrieve response from PHP
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_FILL_CHALLENGE_REQUEST, "POST", params);

                //variable to check if HttpRequest was successful
                success = json.getInt(PHPTAG_SUCCESS);

                if (success == 1) {
                    //Used to contain the data from JSON Obj in a retrievable form
                    JSONArray sqlData = null;

                    Challenge challenge;
                    sqlData = json.getJSONArray(PHPTAG_POSTS);

                    for (int i = 0; i < sqlData.length(); i++) {
                        challenge=new Challenge(ActivityChallengeRequests.this.getApplicationContext());
                        if(challenge.loadChallengeInfo(sqlData.getJSONObject(i))) {
                            challengeRequestList.add(challenge);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            challengeAdapter = new ChallengeRequestAdapter();

            ListView challengesListview = (ListView) findViewById(R.id.lv_challengerequests);
            challengesListview.setAdapter(challengeAdapter);

            progressDialog.dismiss();

            challengesListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                        long arg3) {

                    final Challenge challenge=challengeAdapter.getItem(arg2);

                    final Dialog dialog = new Dialog(ActivityChallengeRequests.this);

                    dialog.setTitle("Confirm Challenge Request");
                    dialog.setContentView(R.layout.dialog_confirmrequest);

                    Button btn_confirm=(Button)dialog.findViewById(R.id.btn_confirmRequest);
                    Button btn_deny=(Button)dialog.findViewById(R.id.btn_denyRequest);

                    dialog.show();

                    btn_confirm.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v)
                        {
                            new ConfirmChallengeRequest().execute(challenge.getId());
                            dialog.dismiss();
                            challengeRequestList.remove(arg2);
                            challengeAdapter.notifyDataSetChanged();
                        }
                    });

                    btn_deny.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();

                            BasicNameValuePair[] params=new BasicNameValuePair[1];
                            params[0]=new BasicNameValuePair(PHPTAG_CHALLENGEID, Integer.toString(challenge.getId()));

                            Challenge deleteChallenge=new Challenge();
                            deleteChallenge.deleteChallenge(params, getApplicationContext());
                            dialog.dismiss();

                            challengeRequestList.remove(arg2);
                            challengeAdapter.notifyDataSetChanged();
                        }
                    });
                }
                //TODO: decide what to do when a friend is clicked->maybe next implementation

            });

        }
    }

    public class ChallengeRequestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return challengeRequestList.size();
        }

        @Override
        public Challenge getItem(int arg0) {
            return challengeRequestList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if(arg1==null)
            {
                LayoutInflater inflater = (LayoutInflater) ActivityChallengeRequests.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem_challenge_requests, arg2,false);
            }

            final Challenge challenge = challengeRequestList.get(arg0);

            TextView challengeName = (TextView)arg1.findViewById(R.id.tv_chlgReqName);
            challengeName.setText(challenge.getName());

            TextView challengeFriendUsername=(TextView)arg1.findViewById(R.id.tv_chlgReqHostUsername);
            challengeFriendUsername.setText(challenge.getUser1Username());

            TextView challengeDesc = (TextView)arg1.findViewById(R.id.tv_chlgReqDescription);
            challengeDesc.setText(challenge.getDescription());

            //TODO: have a better way of doing this
            selectedChallenge=challenge; //so that the confirm request can work


            return arg1;
        }

        public Challenge getChallenge(int position)
        {
            return challengeRequestList.get(position);
        }

    }

    public class ConfirmChallengeRequest extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... challengeId) {
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("challenge_id", Integer.toString(challengeId[0])));

                Log.d("request!", "starting");

                JSONParser jsonParser = new JSONParser();

                JSONObject json = jsonParser.makeHttpRequest(URL_CONFIRM_CHALLENGE_REQUEST, "POST", params);  //does this POST have to be here? what does it signify?
                Log.d("Register attempt", json.toString());

                success = json.getInt(PHPTAG_SUCCESS);
                if (success == 1) {
                    Log.d("Register Successful!", json.toString());
                    return true;
                } else {
                    Log.d("Register Failed!", json.toString());
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                Toast.makeText(ActivityChallengeRequests.this, "Request Confirmed", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ActivityChallengeRequests.this, "Unable to Confirm Request", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Context getContext() {
        return mContext;
    }

}
