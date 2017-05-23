package flyapp.its_on;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
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

import net.danlew.android.joda.JodaTimeAndroid;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ActivityChallengeMainPage extends Activity {

    private static final String PHPTAG_CHALLENGEID="challenge_id";
    private static final String PHP_TAG_USER_1_UPDATES="user_1_updates";
    private static final String PHP_TAG_USER_2_UPDATES="user_2_updates";
    private static final String PHP_TAG_SUCCESS="success";

    private static final String EXTRA_TAG_MODE="mode";

    private static String URL_PHP_GET_CHALLENGE_UPDATES;

    private List<Challenge.UpdateParcel> storedDates;
    UserSession userSession;
    //currently selected challenge. retrieved from profile home when clicked on
    private final Challenge selectedChallenge=ActivityProfileHome.selectedChallenge;
    private ListView challengesUpdatesListview;
    private View headerView;
    private List<Challenge.UpdateParcel> updatesList=new ArrayList<Challenge.UpdateParcel>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_main_page);
        setTitle("");
        userSession = new UserSession(ActivityChallengeMainPage.this);

        JodaTimeAndroid.init(this);

        URL_PHP_GET_CHALLENGE_UPDATES=getResources().getString(R.string.ipaddress)+ "getchallengeupdates.php";

        //sets the custom list item adapter to the listview
        headerView = (View)getLayoutInflater().inflate(R.layout.headerview_challenge_main,null,false);
        challengesUpdatesListview = (ListView) findViewById(R.id.lv_challengeUpdates);

        challengesUpdatesListview.addHeaderView(headerView);


    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if(selectedChallenge!=null) {
            new FillChallengeTask().execute();
        }else{
            Toast.makeText(ActivityChallengeMainPage.this, "Selected Challenge is null", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.challenge_main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_deleteFriend:
                BasicNameValuePair[] params=new BasicNameValuePair[1];
                params[0]=new BasicNameValuePair(PHPTAG_CHALLENGEID, Integer.toString(selectedChallenge.getId()));

                Challenge challenge=new Challenge();
                challenge.deleteChallenge(params,this);
                return true;
            case R.id.action_challengeSettings:
                Intent addChallengeScreen = new Intent(getApplicationContext(), ActivityNewChallenge.class);
                addChallengeScreen.putExtra(EXTRA_TAG_MODE, 1);
                startActivity(addChallengeScreen);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class DateDispAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return storedDates.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return storedDates.get(arg0);
            //??: what is arg0, arg1....?
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            //Q: don't understand what a view is and viewgroup

            final Challenge.UpdateParcel curItem = storedDates.get(arg0);
            boolean isComp=curItem.iscomp;

            TextView tv_month;
            TextView tv_date;

            if ( !isComp) {
                LayoutInflater inflater = (LayoutInflater) ActivityChallengeMainPage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.view_challengedates_unfilled, arg2, false);

            }
            else if(isComp)
            {
                LayoutInflater inflater = (LayoutInflater) ActivityChallengeMainPage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.view_challengedates_filled, arg2, false);
            }

            if(!isComp)
            {
                tv_month = (TextView) arg1.findViewById(R.id.tv_chlgDateMonthUnfilled);
                tv_date = (TextView) arg1.findViewById(R.id.tv_chlgDateUnfilled);
            }
            else
            {
                tv_month = (TextView) arg1.findViewById(R.id.tv_chlgDateMonthFilled);
                tv_date = (TextView) arg1.findViewById(R.id.tv_chlgDateFilled);
            }

            DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM"); // Make sure we use English month names
            String monthText = formatter.print(curItem.date);

            formatter = DateTimeFormat.forPattern("dd");
            String dayText = formatter.print(curItem.date);

            if(curItem.ishead==true)
                tv_month.setText(monthText);
            else
                tv_month.setText("");
            tv_date.setText(dayText);

            return arg1;
        }

        public Challenge.UpdateParcel getUpdateParcel(int position)
        {
            return storedDates.get(position);
        }
    }

    private void populateChallenge(){

    }

    //Custom Adapter for the challenges Listview
    public class UpdatesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return updatesList.size();
        }

        @Override
        public Challenge.UpdateParcel getItem(int arg0) {
            return updatesList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            if(arg1==null)
            {
                LayoutInflater inflater = (LayoutInflater) ActivityChallengeMainPage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem_challenge_updates, arg2,false);
            }

            //retrieve currently selected challenge
            final Challenge.UpdateParcel updateParcel = updatesList.get(arg0);

            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

            TextView tvUsername = (TextView)arg1.findViewById(R.id.tv_username);
            CircleImageView ivDp=(CircleImageView)arg1.findViewById(R.id.iv_dp);


            if(selectedChallenge.getUser1Id()==updateParcel.userId){
                tvUsername.setText(selectedChallenge.getUser1Username());
                ivDp.setImageBitmap(selectedChallenge.getUser1Dp());
            }else{
                tvUsername.setText(selectedChallenge.getUser2Username());
                ivDp.setImageBitmap(selectedChallenge.getUser2Dp());
            }

            TextView tvDate=(TextView)arg1.findViewById(R.id.tv_date);
            TextView tvDescription=(TextView)arg1.findViewById(R.id.tv_message);

            tvDate.setText(dtf.print(updateParcel.date));
            tvDescription.setText(updateParcel.message);

            return arg1;
        }

        public Challenge.UpdateParcel getUpdate(int position)
        {
            return updatesList.get(position);
        }

    }


    private class FillChallengeTask extends AsyncTask<Void, Void, Boolean > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            //we will develop this method in version 2
            int success;

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("challenge_id", Integer.toString(selectedChallenge.getId())));

                JSONParser jsonParser = new JSONParser();

                JSONObject json = jsonParser.makeHttpRequest(URL_PHP_GET_CHALLENGE_UPDATES, "POST", params);

                success = json.getInt(PHP_TAG_SUCCESS);

                if (success == 1) {
                    selectedChallenge.storeUpdatesUser1(json.getJSONArray(PHP_TAG_USER_1_UPDATES));
                    selectedChallenge.storeUpdatesUser2(json.getJSONArray(PHP_TAG_USER_2_UPDATES));
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                storedDates=selectedChallenge.getUserUpdatesParcelList(userSession.getUserId());
                //populating the display for date bubbles
                DateDispAdapter aItems = new DateDispAdapter();
                TwoWayView lvTest = (TwoWayView)headerView.findViewById(R.id.twv_dateDisp);
                lvTest.setAdapter(aItems);

                //filling image and text regarding the challenge
                ImageView ivChallengeCover = (ImageView) headerView.findViewById(R.id.iv_cover);
                ivChallengeCover.setImageBitmap(selectedChallenge.getCoverBitmap());

                TextView tvChallengeName = (TextView) headerView.findViewById(R.id.tv_dpChlgName);
                tvChallengeName.setText(selectedChallenge.getName());

                TextView tvChallengeDescription = (TextView) headerView.findViewById(R.id.tv_dpChlgDesc);
                tvChallengeDescription.setText(selectedChallenge.getDescription());

                UpdatesAdapter updatesAdapter=new UpdatesAdapter();

                updatesList=selectedChallenge.getCompleteUpdateParcelList();
                challengesUpdatesListview.setAdapter(updatesAdapter);

            }else{
                Toast.makeText(ActivityChallengeMainPage.this, "Error Retrieving data", Toast.LENGTH_SHORT).show();
            }

        }

    }
}
