package flyapp.its_on;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityNewChallenge extends Activity {

    private class ImageData
    {
        final int id;
        String name="";
        Bitmap image=null;

        ImageData(int id)
        {
            this.id=id;
        }
    }

    UserSession userSession;

    private List<ImageData> imageDataCollection = new ArrayList<ImageData>();

    final Calendar myCalendar = Calendar.getInstance();

    private static String URL_ADD_CHALLENGE;
    private static String URL_LOAD_CHALLENGE_COVERS;
    private static String URL_LOADFRIENDS;
    private static String URL_UPDATE_CHALLENGE;

    private static final String PHP_TAG_POSTS = "data";
    private static final String PHP_TAG_URL = "url";
    private static final String PHP_TAG_ID = "id";
    private static final String PHP_TAG_MESSAGE = "message";
    private static final String PHP_TAG_SUCCESS = "success";
    private static final String PHP_TAG_FRIENDS="friends";
    private static final String PHP_TAG_FRIEND_USER_ID="friend_id";
    private static final String PHP_TAG_FRIEND_USERNAME="friend_username";
    private static final String PHP_TAG_FRIEND_DP_URL="friend_dp_url";
    private static final String PHPTAG_FRIEND_EMAIL_ADDRESS = "friend_email_address";

    private static final String EXTRA_TAG_OPERATION_MODE="mode";

    private static final String EXTRA_TAG_FRIEND_DP="friend_dp";
    private static final String EXTRA_TAG_FRIEND_NAME="friend_name";
    private static final String EXTRA_TAG_FRIEND_ID="friend_id";

    private static int OPERATION_MODE;
    private int friendId;

    private boolean isImagesLoaded=false;

    private static Challenge thisChallenge;

    private JSONParser jsonParser = new JSONParser();

    private List<Bitmap> covers=new ArrayList<Bitmap>();
    private List<CheckBox> checkboxList=new ArrayList<CheckBox>();



    private static EditText etName, etDescription, etCategory, etStartDate, etEndDate;
    private static RadioButton rbDailyMode, rbWeeklyMode;
    private static LinearLayout layoutWeekdays;


    private List<User> friendsList;




    //temporary->store image id directly to database
    private int curCoverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_challenge);
        userSession = new UserSession(ActivityNewChallenge.this);

        Intent intent = getIntent();

        OPERATION_MODE=intent.getIntExtra(EXTRA_TAG_OPERATION_MODE,0);
        int extraFriendId = intent.getIntExtra(EXTRA_TAG_FRIEND_ID, 0);
        String extraFriendName=intent.getStringExtra(EXTRA_TAG_FRIEND_NAME);
        Bitmap extraFriendDp = intent.getParcelableExtra(EXTRA_TAG_FRIEND_DP);

        URL_ADD_CHALLENGE = getResources().getString(R.string.ipaddress) +"addchallenge.php";
        URL_LOAD_CHALLENGE_COVERS = getResources().getString(R.string.ipaddress) +"fillimageurls.php";
        URL_LOADFRIENDS=getResources().getString(R.string.ipaddress)+"retrievefriends.php";
        URL_UPDATE_CHALLENGE=getResources().getString(R.string.ipaddress)+"updatechallengesettings.php";

        setTitle("");

        layoutWeekdays=(LinearLayout) findViewById(R.id.layout_weekdays);

        rbDailyMode=(RadioButton)findViewById(R.id.rb_daily);
        rbWeeklyMode=(RadioButton)findViewById(R.id.rb_weekly);

        etName = (EditText) findViewById(R.id.et_newChlgName);
        etDescription = (EditText) findViewById(R.id.et_newChlgDesc);
        etCategory = (EditText) findViewById(R.id.et_newChlgCgry);
        etStartDate = (EditText) findViewById(R.id.et_newChlgStartDate);
        etEndDate= (EditText) findViewById(R.id.et_newChlgEndDate);

        checkboxList.add((CheckBox)findViewById(R.id.cb_monday));
        checkboxList.add((CheckBox)findViewById(R.id.cb_tuesday));
        checkboxList.add((CheckBox)findViewById(R.id.cb_wednesday));
        checkboxList.add((CheckBox)findViewById(R.id.cb_thursday));
        checkboxList.add((CheckBox)findViewById(R.id.cb_friday));
        checkboxList.add((CheckBox)findViewById(R.id.cb_saturday));
        checkboxList.add((CheckBox)findViewById(R.id.cb_sunday));

        if(extraFriendId!=0 && extraFriendName!=null && extraFriendDp!=null){
            TextView tvFriendName = (TextView) findViewById(R.id.tv_newchallengefriendname);
            ImageView ivFriendDp = (ImageView) findViewById(R.id.iv_newChallengeFriendDp);

            tvFriendName.setText(extraFriendName);
            ivFriendDp.setImageBitmap(extraFriendDp);

            friendId=extraFriendId;
        }



        if(OPERATION_MODE==1){
            thisChallenge=ActivityProfileHome.selectedChallenge;

            etName.setText(thisChallenge.getName());
            etDescription.setText(thisChallenge.getDescription());
            etCategory.setText(thisChallenge.getCategory());
            etStartDate.setText(thisChallenge.getStartDate());
            etEndDate.setText(thisChallenge.getEndDate());

            Button btnCommit=(Button)findViewById(R.id.btn_commit);
            btnCommit.setText("Update");

            TextView tvPageTitle=(TextView)findViewById(R.id.tv_title);
            tvPageTitle.setText("Goal Settings");

            RelativeLayout rlAddParticipant=(RelativeLayout)findViewById(R.id.rl_addParticipant);
            rlAddParticipant.setVisibility(View.GONE);

            RelativeLayout rlAddCover=(RelativeLayout)findViewById(R.id.rl_addCover);
            rlAddCover.setVisibility(View.GONE);

            setSelectedWeekdays();
        }
    }


    //look into fixing it later. make thing fill entire layout
    public void RetrieveCategory(View v)
    {
        final EditText editText = (EditText) findViewById(R.id.et_newChlgCgry);

        final Dialog dialog = new Dialog(ActivityNewChallenge.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_selectcategory);

        String[] categories = new String[] {"Diet and Health","Fitness", "Happiness", "Relationships", "Productivity", "Academics"};
        final ArrayAdapter categoriesAdptr = new ArrayAdapter<String>(this, R.layout.listitem_category, categories);
        ListView categoryList= (ListView)dialog.findViewById(R.id.lv_popupCtgr);
        categoryList.setAdapter(categoriesAdptr);

        dialog.show();

        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //HL: showing msg that the challenge is selected
                String item = categoriesAdptr.getItem(arg2).toString();
                editText.setText(item);
                dialog.dismiss();
            }
        });


    }

    //Edit so that later there is only one function for both start and end date
    //Learn how this function works
    public void  RetrieveStartDate(View v)
    {
        final EditText editText = (EditText) findViewById(R.id.et_newChlgStartDate);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                editText.setText(sdf.format(myCalendar.getTime()));
            }

        };

        new DatePickerDialog(ActivityNewChallenge.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public void  RetrieveEndDate(View v) {
        final EditText editText = (EditText) findViewById(R.id.et_newChlgEndDate);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                editText.setText(sdf.format(myCalendar.getTime()));
            }

        };

        new DatePickerDialog(ActivityNewChallenge.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    public void setFormatWeekly(View v) {
        LinearLayout layoutWeekdays=(LinearLayout) findViewById(R.id.layout_weekdays);
        layoutWeekdays.setVisibility(View.VISIBLE);
    }

    public void setFormatDaily(View v) {

        layoutWeekdays.setVisibility(View.GONE);
    }

    public void RetrieveChallengeCoverImage(View v)
    {
        new RetrieveImages().execute();

    }


    class RetrieveImages extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {
            try {
                if(!isImagesLoaded) {
                    List<String> imageurls = updateImageURLs();

                    ImageHandler imageHandler=new ImageHandler();

                    for(int i=0; i<imageurls.size(); i++)
                    {
                        covers.add(imageHandler.loadImageFromUrl(imageurls.get(i)));
                    }
                    isImagesLoaded=true;
                }
                return "true";
            }
            catch(Exception e) {
                e.printStackTrace();
                return "bad";
            }

        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {

            final Dialog dialog = new Dialog(ActivityNewChallenge.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setContentView(R.layout.dialog_imageselect);


            GridView gridView = (GridView)dialog.findViewById(R.id.gridview);
            gridView.setAdapter(new GridImageView(dialog.getContext()));
            dialog.show();
            final ImageView iconimage=(ImageView) findViewById(R.id.iv_chlgCover);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                {
                    //curCoverId=imageDataCollection.get(position).id;    this is actually the correct way. look into fixing it
                    curCoverId=position+1;
                    iconimage.setImageBitmap(covers.get(position));
                    dialog.dismiss();
                }
            });
        }
    }

    public List<String> updateImageURLs() {

        List<String> imageurls=new ArrayList<String>();

        JSONParser jParser = new JSONParser();

        JSONObject json = jParser.getJSONFromUrl(URL_LOAD_CHALLENGE_COVERS);
        JSONArray mComments = null;

        try {

            mComments = json.getJSONArray(PHP_TAG_POSTS);

            for (int i = 0; i < mComments.length(); i++) {
                JSONObject c = mComments.getJSONObject(i);

                imageDataCollection.add(new ImageData(Integer.parseInt(c.getString(PHP_TAG_ID))));
                imageurls.add(c.getString(PHP_TAG_URL));

            }
            return imageurls;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_challenge, menu);
        return true;
    }


    public void NewChallengeAdd(View V) {
        new AddChallengeTask().execute();
    }


    class AddChallengeTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;

            List<NameValuePair> params=getChallengeParams();
            try {

                JSONObject json;
                if(OPERATION_MODE==1) {
                    json = jsonParser.makeHttpRequest(URL_UPDATE_CHALLENGE, "POST", params);
                }else{
                    json = jsonParser.makeHttpRequest(URL_ADD_CHALLENGE, "POST", params);
                }
                success = json.getInt(PHP_TAG_SUCCESS);
                if (success == 1) {

                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    return json.getString(PHP_TAG_MESSAGE);

                } else {
                    return json.getString(PHP_TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {

            if (file_url != null) {
                Toast.makeText(ActivityNewChallenge.this, file_url, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class GridImageView extends BaseAdapter  {
        private LayoutInflater inflater;

        public GridImageView(Context context)
        {
            inflater = LayoutInflater.from(context);

            for(int i=0; i<covers.size(); i++)
            {
                //might have error here later. add safety
                imageDataCollection.get(i).image=covers.get(i);
                imageDataCollection.get(i).name="Image "+Integer.toString(i);
            }
        }

        @Override
        public int getCount() {
            return imageDataCollection.size();
        }

        @Override
        public Object getItem(int i)
        {
            return imageDataCollection.get(i);
        }

        @Override
        public long getItemId(int i)
        {
            return imageDataCollection.get(i).id;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View v = view;
            ImageView picture;
            TextView name;

            if(v == null)
            {
                v = inflater.inflate(R.layout.griditem_image, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                //v.setTag(R.id.text, v.findViewById(R.id.text));
                //commented out for now. this is for the text on the image
            }

            picture = (ImageView)v.getTag(R.id.picture);
            //name = (TextView)v.getTag(R.id.text);
            //sets text of image

            picture.setImageBitmap(covers.get(i));
            //name.setText("Image" +i);

            return v;
        }
    }

    public void AddFriend(View V)
    {
        new LoadFriends().execute();
    }

    public class LoadFriends extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... arg0) {
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                int userId=userSession.getUserId();
                params.add(new BasicNameValuePair("user_id",Integer.toString(userId)));

                JSONParser jsonParser = new JSONParser();

                JSONObject json = jsonParser.makeHttpRequest(URL_LOADFRIENDS, "POST", params);

                // json success tag
                success = json.getInt(PHP_TAG_SUCCESS);


                if (success == 1) {
                    User userObj;
                    ImageHandler imageHandler=new ImageHandler();
                    JSONArray jsonArray=null;
                    friendsList= new ArrayList<User>();
                    jsonArray= json.getJSONArray(PHP_TAG_FRIENDS);
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
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
                e.printStackTrace();
                return false;
            }

        }

        FriendAdapter friendsListAdapter;

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                final Dialog dialog = new Dialog(ActivityNewChallenge.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_selectfriend);

                friendsListAdapter = new FriendAdapter();

                ListView friendListView = (ListView) dialog.findViewById(R.id.lv_selectfriend);
                friendListView.setAdapter(friendsListAdapter);

                dialog.show();
                final TextView tvFriendName = (TextView) findViewById(R.id.tv_newchallengefriendname);
                final CircleImageView ivFriendDp = (CircleImageView) findViewById(R.id.iv_newChallengeFriendDp);

                friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {

                        User user = friendsListAdapter.getFriend(arg2);
                        friendId = user.id;
                        tvFriendName.setText(user.username);
                        ivFriendDp.setImageBitmap(user.dp);
                        dialog.dismiss();

                    }
                });
            } else {
                Toast.makeText(ActivityNewChallenge.this, "No friends to add :(", Toast.LENGTH_SHORT).show();
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
                LayoutInflater inflater = (LayoutInflater) ActivityNewChallenge.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private HashSet<Integer> getSelectedWeekdays()  {

        HashSet<Integer> weekdays=new HashSet<Integer>();

        for(int i=0; i<checkboxList.size(); i++){
            if(checkboxList.get(i).isChecked()){
                weekdays.add(i+1);
            }
        }
        return weekdays;
    }

    private void setSelectedWeekdays(){
        HashSet<Integer> weekdays=thisChallenge.getSelectedWeekdaysHash();
        if(weekdays.size()<7){
            rbDailyMode.setChecked(false);
            rbWeeklyMode.setChecked(true);
            layoutWeekdays.setVisibility(View.VISIBLE);
        }
        for(int i=0; i<checkboxList.size(); i++){
            if(weekdays.contains(i+1)){
                checkboxList.get(i).setChecked(true);
            }else{
                checkboxList.get(i).setChecked(false);
            }
        }
    }

    private String computeSelectedDaysOfWeekAsString(){

        if(rbDailyMode.isChecked()){
            return "1111111";
        }else{
            HashSet<Integer> daysOfWeek=getSelectedWeekdays();
            StringBuffer weekdaysString=new StringBuffer();
            for(int i=1; i<=7; i++){
                if(daysOfWeek.contains(i)){
                    weekdaysString.append('1');
                }else{
                    weekdaysString.append('0');
                }
            }
            return weekdaysString.toString();
        }
    }

    private List<NameValuePair> getChallengeParams(){

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String category = etCategory.getText().toString();
        String start_date = etStartDate.getText().toString();
        String end_date = etEndDate.getText().toString();
        String user_1=Integer.toString(userSession.getUserId());
        String user_2=Integer.toString(friendId);
        String image_id =Integer.toString(curCoverId);

        String selectedDaysOfWeek= computeSelectedDaysOfWeekAsString();

        params.add(new BasicNameValuePair("challenge_id", Integer.toString(thisChallenge.getId())));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("description", description));
        params.add(new BasicNameValuePair("category", category));
        params.add(new BasicNameValuePair("start_date", start_date));
        params.add(new BasicNameValuePair("end_date", end_date));
        params.add(new BasicNameValuePair("user_1", user_1 ));
        params.add(new BasicNameValuePair("user_2", user_2 ));
        params.add(new BasicNameValuePair ("image_id", image_id));
        params.add(new BasicNameValuePair ("days_of_week", selectedDaysOfWeek));

        return params;
    }




}

