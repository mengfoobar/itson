package flyapp.its_on;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.NameValuePair;

import java.util.List;


public class ActivityChallengeSettings extends Activity {

    private static final Challenge selectedChallenge=ActivityProfileHome.selectedChallenge;
    private EditText etName, etDescription, etCategory, etStartDate, etEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_settings);

        etName = (EditText) findViewById(R.id.et_newChlgName);
        etDescription = (EditText) findViewById(R.id.et_newChlgDesc);
        etCategory = (EditText) findViewById(R.id.et_newChlgCgry);
        etStartDate = (EditText) findViewById(R.id.et_newChlgStartDate);
        etEndDate= (EditText) findViewById(R.id.et_newChlgEndDate);

        etName.setText(selectedChallenge.getName());
        etDescription.setText(selectedChallenge.getDescription());
        etCategory.setText(selectedChallenge.getCategory());
        etStartDate.setText(selectedChallenge.getStartDate());
        etEndDate.setText(selectedChallenge.getEndDate());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_challenge_settings, menu);
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

    public void updateGoal(View v){
        //List<NameValuePair> params=ActivityNewChallenge.getChallengeParams();
    }
}
