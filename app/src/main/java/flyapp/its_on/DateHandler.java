package flyapp.its_on;

import android.widget.EditText;
import android.widget.RadioButton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by HLiu on 14/09/2014.
 */
public class DateHandler {

    private DateTime startDate, endDate, todayDate;
    private final static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");


    HashSet<Integer> weekdaysHash=new HashSet<Integer>();


    public DateHandler(String startDateString, String endDateString, String weekdays) {
        // Parsing the date
        todayDate=DateTime.now();
        startDate = dtf.parseDateTime(startDateString);
        endDate = dtf.parseDateTime(endDateString);

        char[] weekdaysArray=weekdays.toCharArray();


        for(int i=0; i<7; i++) {
            if (weekdaysArray[i] == '1') {
                weekdaysHash.add(i + 1);
            }
        }

        //convertJsonToUpdateParcel(updates);
    }

    public List<String> getCompleteDatesList(){
        List<String> completeDatesList=new ArrayList<String>();
        DateTime node=startDate;
        while(!node.isAfter(endDate)){
            if(weekdaysHash.contains(node.getDayOfWeek())){
                completeDatesList.add(dtf.print(node));
            }
            node=node.plusDays(1);
        }

        return completeDatesList;
    }

    public HashSet<String>  getCompleteDatesHash(){
        HashSet<String> completeDatesHash=new HashSet<String>();
        DateTime node=startDate;
        while(!node.isAfter(endDate)){
            if(weekdaysHash.contains(node.getDayOfWeek())){
                completeDatesHash.add(dtf.print(node)); //for checking later on if its completed
            }
            node=node.plusDays(1);
        }

        return completeDatesHash;
    }
}
