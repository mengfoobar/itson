package flyapp.its_on;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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


public class ActivityFriendsPage extends Activity {

    UserSession userSession;

    private static String URL_ADDFRIEND;
    private static String URL_LOADFRIENDS;

    private static final String PHP_TAG_SUCCESS = "success";    //string to get integer value of success
    private static final String PHP_TAG_POSTS = "posts";    //string for retrieving POST data from php
    private static final String PHP_TAG_MESSAGE = "message";    //key string used to retrieve JSON msgs from PHP
    private static final String PHP_TAG_USER_ID = "user_id";

    private static final String PHP_TAG_FRIENDS="friends";   //tag to get post data
    private static final String PHP_TAG_FRIEND_USER_ID="friend_id";
    private static final String PHP_TAG_FRIEND_USERNAME="friend_username";
    private static final String PHP_TAG_FRIEND="friend";
    private static final String PHP_TAG_FRIEND_DP_URL="friend_dp_url";
    private static final String PHPTAG_FRIEND_EMAIL_ADDRESS = "friend_email_address";
    private static final String PHPTAG_IS_FRIEND_REQUEST_AVAILABLE="friend_request_available";

    private static final String EXTRA_TAG_FRIEND_ID="friend_id";
    private static final String EXTRA_TAG_FRIEND_DP="friend_dp";
    private static final String EXTRA_TAG_FRIEND_NAME="friend_name";

    private List<User> friendsList;
    private List<NameValuePair> params; //used to store value for input into database

    private  FriendAdapter friendsListAdapter;
    private ProgressDialog progressDialog;
    private int isThereFriendRequest=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //remove title from actionbar
        setTitle("");

        userSession = new UserSession(getApplicationContext());

        URL_ADDFRIEND= getApplicationContext().getResources().getString(R.string.ipaddress) + "addfriend.php";
        URL_LOADFRIENDS=getApplicationContext().getResources().getString(R.string.ipaddress)+"retrievefriends.php";
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadFriends().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.friends_page, menu);

        if(isThereFriendRequest==0) {
            menu.getItem(1).setVisible(false);
        }else{
            menu.getItem(1).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_addfriend:
                addFriend();
                return true;
            case R.id.action_friendrequest:
                Intent friendsRequest = new Intent(getApplicationContext(), ActivityFriendRequest.class);
                startActivity(friendsRequest);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addFriend()
    {
        final Dialog dialog = new Dialog(ActivityFriendsPage.this);
        dialog.setTitle("Add Friend");
        dialog.setContentView(R.layout.dialog_addfriend);

        final EditText etGetFriendUsername=(EditText)dialog.findViewById(R.id.et_friendUsername);
        final Button btnAddFriend=(Button)dialog.findViewById(R.id.btn_addFriend);

        dialog.show();

        btnAddFriend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                int userId=userSession.getUserId();

                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(PHP_TAG_USER_ID, Integer.toString(userId)));
                params.add(new BasicNameValuePair(PHP_TAG_FRIEND, etGetFriendUsername.getText().toString()));

                new AddFriend().execute();
                dialog.dismiss();
            }
        });

    }



    public class AddFriend extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            // Check for success tag
            int success;

            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_ADDFRIEND, "POST", params);

                // json success tag
                success = json.getInt(PHP_TAG_SUCCESS);
                if (success == 1) {
                    return true;
                }else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }


        }

        protected void onPostExecute(Boolean result) {
            if (result){
                Toast.makeText(ActivityFriendsPage.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ActivityFriendsPage.this, "Error Sending Request", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class LoadFriends extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ActivityFriendsPage.this, "Loading...",
                    "Loading Page", false, false);
        }
        @Override
        protected Boolean doInBackground(Void... arg0) {
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                int userId=userSession.getUserId();
                params.add(new BasicNameValuePair(PHP_TAG_USER_ID,Integer.toString(userId)));

                JSONParser jsonParser = new JSONParser();

                JSONObject json = jsonParser.makeHttpRequest(URL_LOADFRIENDS, "POST", params);

                success = json.getInt(PHP_TAG_SUCCESS);
                isThereFriendRequest=json.getInt(PHPTAG_IS_FRIEND_REQUEST_AVAILABLE);

                if (success == 1) {
                    ImageHandler imageHandler=new ImageHandler();
                    User userObj;
                    JSONArray jsonArray=null;
                    JSONObject jsonObject=null;

                    friendsList= new ArrayList<User>();
                    jsonArray= json.getJSONArray(PHP_TAG_FRIENDS);

                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);

                            userObj =new User();

                            userObj.username=jsonObject.getString(PHP_TAG_FRIEND_USERNAME);
                            userObj.id=Integer.parseInt(jsonObject.getString(PHP_TAG_FRIEND_USER_ID));
                            userObj.dp=imageHandler.loadImageFromUrl(jsonObject.getString(PHP_TAG_FRIEND_DP_URL));
                            userObj.emailAddress=jsonObject.getString(PHPTAG_FRIEND_EMAIL_ADDRESS);

                            friendsList.add(userObj);
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

            if(result) {
                friendsListAdapter = new FriendAdapter();

                ListView friendListView = (ListView) findViewById(R.id.lv_friends);
                friendListView.setAdapter(friendsListAdapter);

                friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                            long arg3) {

                        final User user =friendsListAdapter.getItem(arg2);
                        final Dialog dialog = new Dialog(ActivityFriendsPage.this);

                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_friend_options);

                        Button btn_newChallenge=(Button)dialog.findViewById(R.id.btn_friendOptionNewChallenge);
                        Button btn_defriend=(Button)dialog.findViewById(R.id.btn_friendOptionDefriend);

                        dialog.show();

                        btn_newChallenge.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v)
                            {
                                dialog.dismiss();
                                Intent addChallengeScreen = new Intent(getApplicationContext(), ActivityNewChallenge.class);
                                addChallengeScreen.putExtra(EXTRA_TAG_FRIEND_ID, user.id);
                                addChallengeScreen.putExtra(EXTRA_TAG_FRIEND_NAME, user.username);
                                addChallengeScreen.putExtra(EXTRA_TAG_FRIEND_DP, user.dp);
                                startActivity(addChallengeScreen);
                            }
                        });

                        btn_defriend.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                BasicNameValuePair[] params=new BasicNameValuePair[2];
                                params[0]=new BasicNameValuePair(PHP_TAG_USER_ID, Integer.toString(userSession.getUserId()));
                                params[1]=new BasicNameValuePair(PHP_TAG_FRIEND_USER_ID, Integer.toString(user.id));

                                User deleteTask=new User();
                                deleteTask.defriend(params, getApplicationContext());
                                dialog.dismiss();

                                friendsList.remove(arg2);
                                friendsListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    //TODO: decide what to do when a friend is clicked->maybe next implementation

                });

            }

            invalidateOptionsMenu ();
            progressDialog.dismiss();

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
                LayoutInflater inflater = (LayoutInflater) ActivityFriendsPage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem_friend, arg2,false);
            }

            final User user = friendsList.get(arg0);

            TextView challengeName = (TextView)arg1.findViewById(R.id.tv_friendUsername);
            challengeName.setText(user.username);

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






}
