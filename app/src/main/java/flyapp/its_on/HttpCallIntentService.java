package flyapp.its_on;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HLiu on 16/08/2014.
 */


public class HttpCallIntentService extends IntentService {

    public static final String TAG_SUCCESS = "success";    //string to get integer value of success
    private static final String TAG_MESSAGE = "message";    //key string used to retrieve JSON msgs from PHP

    public static final String TAG_URL="url";

    private static String TAG_POST="posts";

    private static String requestUrl;
    private static ArrayList<String> postKeys;
    private static ArrayList<String> keys;

    public static final String ACTION_MyIntentService = "flyapp.its_on.RESPONSE";
    public static final String TAG_PARCELS_IN = "PARCELS_IN";
    public static final String TAG_JSON_OUT = "JSON_OUT";


    public HttpCallIntentService() {
        super("flyapp.its_on.HttpCallIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int success;
        requestUrl=intent.getStringExtra(TAG_URL);
        postKeys=intent.getStringArrayListExtra(TAG_POST);

        ArrayList<ParcelHttpParams> parcels=intent.getParcelableArrayListExtra(TAG_PARCELS_IN);
        List<NameValuePair> params=ConvertParcelToBNVP(parcels);


        try{
            Log.d("request!", "starting");
            JSONParser jsonParser = new JSONParser();

            JSONObject json = jsonParser.makeHttpRequest(requestUrl, "POST", params);

            // check your log for json response
            Log.d("Register attempt", json.toString());

            // json success tag
            success = json.getInt(TAG_SUCCESS);


            Intent intentResponse = new Intent();
            if (success == 1) {
                Log.d("Register Successful!", json.toString());
                intentResponse.putExtra(TAG_SUCCESS, true);

            } else {
                Log.d("Register Failed!", json.toString());
                intentResponse.putExtra(TAG_SUCCESS, false);
            }



            intentResponse.setAction(ACTION_MyIntentService);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
            intentResponse.putExtra(TAG_JSON_OUT,json.toString());
            sendBroadcast(intentResponse);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<NameValuePair> ConvertParcelToBNVP(List<ParcelHttpParams> parcels)
    {
        List<NameValuePair> params=new ArrayList<NameValuePair>();
        for(int i=0; i<parcels.size();i++)
        {
            params.add(new BasicNameValuePair(parcels.get(i).key,parcels.get(i).value));
        }
        return params;

    }
}
