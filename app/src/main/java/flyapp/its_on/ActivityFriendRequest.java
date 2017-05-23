package flyapp.its_on;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.ImageView;
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

public class ActivityFriendRequest extends Activity {

    private static UserSession userSession;

    private static String URL_LOADFRIENDREQUESTS;
    private static String URL_CONFIRMFRIENDREQUESTS;

    private static final String PHPTAG_SUCCESS = "success";    //string to get integer value of success
    private static final String PHPTAG_POSTS = "posts";    //string for retrieving POST data from php
    private static final String PHPTAG_MESSAGE = "message";    //key string used to retrieve JSON msgs from PHP
    private static final String PHPTAG_USER_ID = "user_id";

    private static final String PHPTAG_FRIENDS="friends";
    private static final String PHPTAG_FRIEND_USER_ID="friend_id";
    private static final String PHPTAG_FRIEND_USERNAME="friend_username";
    private static final String PHPTAG_FRIEND_DP_URL = "friend_dp_url";
    private static final String PHPTAG_FRIEND_EMAIL_ADDRESS = "friend_email_address";

    private static List<User> friendsList;
    private static FriendAdapter friendsListAdapter;
    private ProgressDialog progressDialog;

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        //remove title from actionbar
        setTitle("");

        userSession = new UserSession(getApplicationContext());

        URL_LOADFRIENDREQUESTS=getApplicationContext().getResources().getString(R.string.ipaddress)+"retrievefriendrequests.php";
        URL_CONFIRMFRIENDREQUESTS=getApplicationContext().getResources().getString(R.string.ipaddress)+"confirmfriend.php";
    }

    @Override
    protected void onResume() {
        mContext=getApplicationContext();

        super.onResume();
        new LoadFriendRequests().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.friend_request, menu);
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

    public class LoadFriendRequests extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //start loading dialog while accessing server
            progressDialog = ProgressDialog.show(ActivityFriendRequest.this, "Loading...",
                    "Loading Page", false, false);
        }
        @Override
        protected Boolean doInBackground(Void... arg0) {
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair(PHPTAG_USER_ID, Integer.toString(userSession.getUserId())));

                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_LOADFRIENDREQUESTS, "POST", params);

                success = json.getInt(PHPTAG_SUCCESS);

                if (success == 1) {

                    JSONArray jsonArray=null;

                    User user;
                    ImageHandler imageHandler=new ImageHandler();
                    JSONObject jsonObject;

                    friendsList= new ArrayList<User>();
                    jsonArray= json.getJSONArray(PHPTAG_FRIENDS);

                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            user =new User();
                            user.id=Integer.parseInt(jsonObject.getString(PHPTAG_FRIEND_USER_ID));
                            user.username=jsonObject.getString(PHPTAG_FRIEND_USERNAME);
                            user.dp=imageHandler.loadImageFromUrl(jsonObject.getString(PHPTAG_FRIEND_DP_URL));
                            user.emailAddress=jsonObject.getString(PHPTAG_FRIEND_EMAIL_ADDRESS);
                            friendsList.add(user);
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
                friendsListAdapter = new FriendAdapter();

                ListView friendListView = (ListView)findViewById(R.id.lv_friendRequests);
                friendListView.setAdapter(friendsListAdapter);

                friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                            long arg3) {

                    final User user =friendsListAdapter.getItem(arg2);
                    final Dialog dialog = new Dialog(ActivityFriendRequest.this);

                    dialog.setTitle("Confirm Friend Request");
                    dialog.setContentView(R.layout.dialog_confirmrequest);

                    Button btn_confirm=(Button)dialog.findViewById(R.id.btn_confirmRequest);
                    Button btn_deny=(Button)dialog.findViewById(R.id.btn_denyRequest);

                    dialog.show();

                    btn_confirm.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v)
                            {
                            new ConfirmFriendRequest().execute(user.id);
                            dialog.dismiss();
                            Toast.makeText(ActivityFriendRequest.this, "Friend Request Accepted!", Toast.LENGTH_SHORT).show();
                            friendsList.remove(arg2);
                            friendsListAdapter.notifyDataSetChanged();
                            }
                        });

                    btn_deny.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v)
                            {

                            BasicNameValuePair[] params=new BasicNameValuePair[2];
                            params[0]=new BasicNameValuePair(PHPTAG_USER_ID, Integer.toString(userSession.getUserId()));
                            params[1]=new BasicNameValuePair(PHPTAG_FRIEND_USER_ID, Integer.toString(user.id));

                            User deleteTask=new User();
                            deleteTask.denyFriendRequest(params, getApplicationContext());
                            dialog.dismiss();

                            friendsList.remove(arg2);
                            friendsListAdapter.notifyDataSetChanged();
                        }
                        });
                    }
                    //TODO: decide what to do when a friend is clicked->maybe next implementation

                });
            }
            else{
                //Implement different messages for different scenarios
                Toast.makeText(ActivityFriendRequest.this, "No Friend Request", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class FriendAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friendsList.size();
        }

        @Override
        public User getItem(int arg0) {
            return friendsList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if(arg1==null)
            {
                LayoutInflater inflater = (LayoutInflater) ActivityFriendRequest.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem_friend, arg2,false);
            }

            final User user = friendsList.get(arg0);

            TextView tvFriendName = (TextView)arg1.findViewById(R.id.tv_friendUsername);
            tvFriendName.setText(user.username);

            TextView tvFriendEmail = (TextView)arg1.findViewById(R.id.tv_friendEmail);
            tvFriendEmail.setText(user.emailAddress);

            ImageView ivFriendDp=(ImageView)arg1.findViewById(R.id.iv_friendDp);
            ivFriendDp.setImageBitmap(user.dp);

            return arg1;
        }

        public User getFriend(int position)
        {
            return friendsList.get(position);
        }

    }

    public class ConfirmFriendRequest extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... friendId) {
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                int userId = userSession.getUserId();

                params.add(new BasicNameValuePair(PHPTAG_USER_ID, Integer.toString(userId)));
                params.add(new BasicNameValuePair(PHPTAG_FRIEND_USER_ID, Integer.toString(friendId[0])));

                JSONParser jsonParser = new JSONParser();

                JSONObject json = jsonParser.makeHttpRequest(URL_CONFIRMFRIENDREQUESTS, "POST", params);

                success = json.getInt(PHPTAG_SUCCESS);
                if (success == 1) {
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
            if(result){
                Toast.makeText(ActivityFriendRequest.this, "Friend Confirmed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ActivityFriendRequest.this, "Unable to Confirm Friend", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Context getContext() {
        return mContext;
    }





}
