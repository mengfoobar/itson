package flyapp.its_on;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by HLiu on 05/08/2014.
 */
public class Challenge {

    private static String UPDATE_URL;     //link to php file for updating a challange
    private static String URL_DELETECHALLENGE;

    private static final String PHP_TAG_SUCCESS = "success";    //string to get integer value of success
    private static final String PHP_TAG_MESSAGE = "message";    //key string used to retrieve JSON msgs from PHP
    private static final String PHP_TAG_ID = "id";      //key string used for retrieve id from POST in PHP
    private static final String PHP_TAG_NAME = "name";  //key string used for retrieve name from POST in PHP
    private static final String PHP_TAG_DESCRIPTION = "description";    //key string used for retrieve description from POST in PHP
    private static final String PHP_TAG_COVER_URL = "cover_url";    //key string used for retrieve description from POST in PHP
    private static final String PHP_TAG_SELECTED_WEEKDAYS="days_of_week";
    private static final String PHP_TAG_START_DATE="start_date";
    private static final String PHP_TAG_END_DATE="end_date";
    private static final String PHP_TAG_USER_1="user_1";
    private static final String PHP_TAG_USER_2="user_2";
    private static final String PHP_TAG_USER_1_USERNAME="user_1_username";
    private static final String PHP_TAG_USER_2_USERNAME="user_2_username";
    private static final String PHP_TAG_USER_1_DP_URL="user_1_dp_url";
    private static final String PHP_TAG_USER_2_DP_URL="user_2_dp_url";
    private static final String PHP_TAG_DATE = "date";
    private static final String PHP_TAG_MSG = "message";
    private static final String PHP_TAG_UPDATE_STATUS = "update_status";
    private static final String PHP_TAG_CATEGORY="category";

    private User user1;
    private User user2;

    private List<String> completeDatesList=new ArrayList<String>();
    private HashSet<String> completeDatesHash=new HashSet<String>();
    private HashMap<String, String> user1UpdatesHash;
    private HashMap<String, String> user2UpdatesHash;


    private int ID;
    private String NAME;
    private String DESCRIPTION;
    private String CATEGORY;
    private Bitmap COVER;
    private String SELECTED_WEEKDAYS;
    private String START_DATE;
    private String END_DATE;
    private int UPDATE_STATUS;

    private static UserSession userSession;

    private Context context;

    private DateHandler dateHandler;
    private static final ImageHandler imageHandler=new ImageHandler();

    private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");


    class UpdateParcel{
        int userId;
        DateTime date;
        boolean iscomp;
        boolean ishead;
        String message;
    }

    // constructor
    public Challenge(Context arg0) {
        context=arg0;
        UPDATE_URL= context.getResources().getString(R.string.ipaddress) + "update.php";
        userSession = new UserSession(context);
    }

    // constructor
    public Challenge() {
    }

    public boolean loadChallengeInfo(JSONObject data) {
        try {

            ID =Integer.parseInt(data.getString(PHP_TAG_ID));
            NAME = data.getString(PHP_TAG_NAME);
            DESCRIPTION = data.getString(PHP_TAG_DESCRIPTION);
            CATEGORY=data.getString(PHP_TAG_CATEGORY);
            COVER=imageHandler.loadImageFromUrl(data.getString(PHP_TAG_COVER_URL));

            START_DATE=data.getString(PHP_TAG_START_DATE);
            END_DATE=data.getString(PHP_TAG_END_DATE);
            SELECTED_WEEKDAYS=data.getString(PHP_TAG_SELECTED_WEEKDAYS);

            if(START_DATE!=null && END_DATE!=null && SELECTED_WEEKDAYS!=null){
                dateHandler=new DateHandler(START_DATE, END_DATE, SELECTED_WEEKDAYS);
                completeDatesList=dateHandler.getCompleteDatesList();
                completeDatesHash=dateHandler.getCompleteDatesHash();
            }

            user1=new User();
            user1.id=data.getInt(PHP_TAG_USER_1);
            user1.username=data.getString(PHP_TAG_USER_1_USERNAME);
            user1.dp=imageHandler.loadImageFromUrl(data.getString(PHP_TAG_USER_1_DP_URL));

            user2=new User();
            user2.id=data.getInt(PHP_TAG_USER_2);
            user2.username=data.getString(PHP_TAG_USER_2_USERNAME);
            user2.dp=imageHandler.loadImageFromUrl(data.getString(PHP_TAG_USER_2_DP_URL));


            UPDATE_STATUS=data.getInt(PHP_TAG_UPDATE_STATUS);
            if(!checkIfUpdatable()){
                UPDATE_STATUS=-1;
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadArchivedChallenges(JSONObject data) {
        try {

            ID =Integer.parseInt(data.getString(PHP_TAG_ID));
            NAME = data.getString(PHP_TAG_NAME);
            DESCRIPTION=data.getString(PHP_TAG_DESCRIPTION);
            END_DATE = data.getString(PHP_TAG_END_DATE);

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfUpdatable(){
        LocalDate localDate = new LocalDate();
        String localDateString=dtf.print(localDate);
        if(completeDatesHash.contains(localDateString)){
            return true;
        }
        return false;
    }

    public boolean checkIfUpdated(){
        LocalDate localDate = new LocalDate();
        String s=dtf.print(localDate);
        HashMap<String, String> updatesHash;
        if (userSession.getUserId() == user1.id) {
            updatesHash=user1UpdatesHash;
        } else {
            updatesHash=user2UpdatesHash;
        }
        if(completeDatesHash.contains(s)&& updatesHash.containsKey(s))
        {
            return true;
        }else{
            return false;
        }
    }

    public int getId() { return ID;}

    public int getUser1Id() { return user1.id;}

    public int getUser2Id() { return user2.id;}

    public String getUser1Username() { return user1.username;}

    public String getUser2Username() { return user2.username;}

    public Bitmap getUser1Dp() { return user1.dp;}

    public Bitmap getUser2Dp() { return user2.dp;}

    public String getName() { return NAME; }

    public String getDescription() { return DESCRIPTION; }

    public String getCategory() { return CATEGORY; }

    public Bitmap getCoverBitmap() { return COVER;}

    public String getStartDate() { return START_DATE; }

    public String getEndDate() { return END_DATE; }

    public HashSet<Integer> getSelectedWeekdaysHash() {
        HashSet<Integer> weekdaysHash=new HashSet<Integer>();
        char[] weekdaysArray=SELECTED_WEEKDAYS.toCharArray();

        for(int i=0; i<7; i++) {
            if (weekdaysArray[i] == '1') {
                weekdaysHash.add(i + 1);
            }
        }
        return weekdaysHash;
    }

    public int getTodayUpdateStatus() { return UPDATE_STATUS; }

    public Boolean storeUpdatesUser1(JSONArray updates){
        if(updates==null){
            return false;
        }else{
            user1UpdatesHash=storeUpdates(updates);
            return true;
        }
    }

    public Boolean storeUpdatesUser2(JSONArray updates){
        if(updates==null){
            return false;
        }else{
            user2UpdatesHash=storeUpdates(updates);
            return true;
        }
    }

    private HashMap<String, String> storeUpdates(JSONArray json){

        HashMap<String, String> updatedDatesHash=new HashMap<String, String>();
        if (json != null) {
            for (int i = 0; i < json.length(); i++) {
                try {
                    JSONObject c = json.getJSONObject(i);
                    String date=c.getString(PHP_TAG_DATE);
                    String message=c.getString(PHP_TAG_MSG);

                    if(date!=null){
                        updatedDatesHash.put(date, message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return updatedDatesHash;

    }   //what is this used for?

    public void updateToday(String message) {
        UPDATE_STATUS=1;
        new UpdateChallengeTask().execute(message);
    }

    public void deleteChallenge(BasicNameValuePair[] params, Context context){

        DeleteChallengeTask deleteChallengeTask=new DeleteChallengeTask(context);
        deleteChallengeTask.execute(params);
    }

    public List<UpdateParcel> getCompleteUpdateParcelList(){

        UpdateParcel updateParcel;
        List<UpdateParcel> listUpdateParcelFull=new ArrayList<UpdateParcel>();
        List<UpdateParcel> listUpdateParcelUser1=getUserUpdatesParcelList(user1.id);
        List<UpdateParcel> listUpdateParcelUser2=getUserUpdatesParcelList(user2.id);

        int maxListSizeAmongUsers;
        if(listUpdateParcelUser1.size()>listUpdateParcelUser2.size()){
            maxListSizeAmongUsers=listUpdateParcelUser1.size();
        }else{
            maxListSizeAmongUsers=listUpdateParcelUser2.size();
        }

        for(int i=maxListSizeAmongUsers-1; i>=0; i--){
            if(i<listUpdateParcelUser1.size()) {
                updateParcel = listUpdateParcelUser1.get(i);
                if (updateParcel.iscomp) {
                    updateParcel.userId = user1.id;
                    listUpdateParcelFull.add(updateParcel);
                }
            }

            if(i<listUpdateParcelUser2.size()) {
                updateParcel = listUpdateParcelUser2.get(i);
                if (updateParcel.iscomp) {
                    updateParcel.userId = user2.id;   //TODO: figure out a better way to put username, dp with the parcel
                    listUpdateParcelFull.add(updateParcel);
                }
            }
        }

        return listUpdateParcelFull;
    }

    public List<UpdateParcel> getUserUpdatesParcelList(int userId){

        HashMap<String, String> updatesHash;
        if(userId==user1.id){
            updatesHash=user1UpdatesHash;
        }else{
            updatesHash=user2UpdatesHash;
        }

        List<UpdateParcel> updateParcelList=new ArrayList<UpdateParcel>();

        for(int i=0; i<completeDatesList.size(); i++)
        {
            UpdateParcel updateParcel;
            String s=completeDatesList.get(i);
            if(updatesHash.containsKey(s)){
                updateParcel=createUpdateParcel(s, 1,updatesHash.get(s));
            }else{
                updateParcel=createUpdateParcel(s, 0,updatesHash.get(s));
            }

            if(i==0){
                updateParcel.ishead=true;
            }

            updateParcelList.add(updateParcel);
        }
        return updateParcelList;
    }

    public UpdateParcel createUpdateParcel(String dateString, int iscomplete, String message) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = null;
        DateTime datetime=null;

        try {
            date = format.parse(dateString);
            datetime = new DateTime(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        UpdateParcel updateParcel;

        updateParcel=new UpdateParcel();
        updateParcel.date=datetime;
        updateParcel.message=message;

        if(iscomplete==1) {
            updateParcel.iscomp = true;
        }
        else {
            updateParcel.iscomp = false;
        }

        if(datetime.getMonthOfYear()!=datetime.minusDays(1).getMonthOfYear())
            updateParcel.ishead=true;

        return updateParcel;
    }

    private class UpdateChallengeTask extends AsyncTask<String, String, String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            //we will develop this method in version 2
            int success;

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                int userId = userSession.getUserId();

                params.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
                params.add(new BasicNameValuePair("challenge_id", Integer.toString(ID)));
                params.add(new BasicNameValuePair("update_message", args[0]));

                JSONParser jsonParser = new JSONParser();

                JSONObject json = jsonParser.makeHttpRequest(UPDATE_URL, "POST", params);

                success = json.getInt(PHP_TAG_SUCCESS);

                if (success == 1) {
                    return "Updated";
                } else {
                    return "Update Unsuccessful";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "Error Accessing Database";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }

    }

    private class DeleteChallengeTask extends AsyncTask<BasicNameValuePair, Void, Boolean> {

        private Context mContext;

        public DeleteChallengeTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            URL_DELETECHALLENGE=mContext.getResources().getString(R.string.ipaddress)+"deletechallenge.php";
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(BasicNameValuePair... paramSet) {
            int success;
            try {
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();

                for(BasicNameValuePair valuePair: paramSet)
                {
                    paramList.add(valuePair);
                }

                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(URL_DELETECHALLENGE, "POST", paramList);

                success = json.getInt(PHP_TAG_SUCCESS);

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
                Toast.makeText(mContext, "Challenge Deleted", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ((Activity) mContext).finish();

                //Intent friendScreen = new Intent((Activity)mContext, ActivityProfileHome.class);
                //((Activity)mContext).startActivity(friendScreen);
            }else{
                Toast.makeText(mContext, "Error Accessing Server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
