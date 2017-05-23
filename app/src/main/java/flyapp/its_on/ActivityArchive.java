package flyapp.its_on;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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


public class ActivityArchive extends Activity {

    private static String URL_FILL_ARCHIVE;      //url for php script that retrieves challenge requests

    private static final String PHPTAG_SUCCESS = "success";    //string to get integer value of success
    private static final String PHPTAG_ARCHIVE = "archived_challenges";    //string for retrieving POST data from php
    private static final String PHPTAG_MESSAGE = "message";    //key string used to retrieve JSON msgs from PHP
    private static final String PHPTAG_USERID = "user_id";

    UserSession userSession;     //usersession to access user shared preferences
    private ArchivedChallengeAdapter challengeAdapter;  //adapter for challenge request list item
    private List<Challenge> archivedChallengeList;      //list used to store the challenge requests from server

    private Challenge selectedChallenge;

    private ProgressDialog progressDialog;      //dialog used to show task is running


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        userSession = new UserSession(this.getApplicationContext());
        URL_FILL_ARCHIVE = getResources().getString(R.string.ipaddress)+ "fillarchive.php";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_archive, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //refresh the request list to prepare to task
        archivedChallengeList=new ArrayList<Challenge>();
        //background task for loading challenge requests from server
        new LoadArchivedChallenges().execute();
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




    public class LoadArchivedChallenges extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //start loading dialog while accessing server
            progressDialog = ProgressDialog.show(ActivityArchive.this, "Loading...",
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
                JSONObject json = jsonParser.makeHttpRequest(URL_FILL_ARCHIVE, "POST", params);

                success = json.getInt(PHPTAG_SUCCESS);

                JSONArray sqlData=null;

                if (success == 1) {

                    sqlData = json.getJSONArray(PHPTAG_ARCHIVE);
                    for(int i=0; i<sqlData.length(); i++) {
                        Challenge challenge = new Challenge();

                        if (challenge.loadArchivedChallenges(sqlData.getJSONObject(i))) {
                            archivedChallengeList.add(challenge);
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

            progressDialog.dismiss();
            if(result) {
                challengeAdapter = new ArchivedChallengeAdapter();

                ListView challengesListview = (ListView) findViewById(R.id.lv_archivedchallenges);
                challengesListview.setAdapter(challengeAdapter);
                challengesListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                            long arg3) {
                        //TODO: decide on content after
                    }


                });
            }else{
                Toast.makeText(ActivityArchive.this, "No Archived Challenges", Toast.LENGTH_LONG).show();
            }
        }
    }


    public class ArchivedChallengeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return archivedChallengeList.size();
        }

        @Override
        public Challenge getItem(int arg0) {
            return archivedChallengeList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if(arg1==null)
            {
                LayoutInflater inflater = (LayoutInflater) ActivityArchive.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem_challenge_archived, arg2,false);
            }

            final Challenge challenge = archivedChallengeList.get(arg0);

            TextView challengeName = (TextView)arg1.findViewById(R.id.tv_archivedChallengeName);
            challengeName.setText(challenge.getName());

            TextView challengeDesc = (TextView)arg1.findViewById(R.id.tv_archivedChallengeDescription);
            challengeDesc.setText(challenge.getDescription());

            TextView challengeUsername= (TextView)arg1.findViewById(R.id.tv_archivedChallengeEndDate);
            challengeUsername.setText(challenge.getEndDate());

            //TODO: have a better way of doing this
            selectedChallenge=challenge; //so that the confirm request can work


            return arg1;
        }

        public Challenge getChallenge(int position)
        {
            return archivedChallengeList.get(position);
        }

    }
}
