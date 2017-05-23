package flyapp.its_on;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class ActivityStartScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        ActionBar bar = getActionBar();
        bar.hide();
    }

    public void SwitchToSignIn(View v)
    {
        Intent nextScreen = new Intent(getApplicationContext(), ActivitySignInScreen.class);
        startActivity(nextScreen);

    }

    public void SwitchToRegister(View v)

    {
        Intent nextScreen = new Intent(getApplicationContext(), ActivityRegistration.class);
        startActivity(nextScreen);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }


}
