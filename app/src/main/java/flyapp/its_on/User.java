package flyapp.its_on;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HLiu on 18/08/2014.
 */
public class User {


    private static final String PHPTAG_SUCCESS = "success";

    private static String URL_DELETEFRIEND;

    public String username;
    public String emailAddress;
    public int id;
    public Bitmap dp;
    public String dpUrl;
    public Bitmap cover;

    public User() {
    }

    public void denyFriendRequest(BasicNameValuePair[] params, Context context){

        DeleteFriend deleteFriendTask=new DeleteFriend(context);
        deleteFriendTask.execute(params);
    }

    public void defriend(BasicNameValuePair[] params, Context context){

        DeleteFriend deleteFriendTask=new DeleteFriend(context);
        deleteFriendTask.execute(params);
    }


    private class DeleteFriend extends AsyncTask<BasicNameValuePair, Void, Boolean> {
        private Context mContext;

        public DeleteFriend(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            URL_DELETEFRIEND=mContext.getResources().getString(R.string.ipaddress)+"deletefriend.php";
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
                JSONObject json = jsonParser.makeHttpRequest(URL_DELETEFRIEND, "POST", paramList);

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
                Toast.makeText(mContext, "Friend Deleted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mContext, "Unable to Deny Friend Request", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
