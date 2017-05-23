package flyapp.its_on;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class CommonUtilities {

    // give your server registration url here
    static final String SERVER_URL="http://team-fly.com/teamflyc_webserver/registerGCMUser.php";

    // Google project id
    static final String SENDER_ID = "61787331614";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "itsOn GCM";

    static final String DISPLAY_MESSAGE_ACTION =
            "flyapp.its_on.DISPLAY_MESSAGE";

    static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    static int getAppVersion(Context context) {
        int appVersion=0;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersion=packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
        return appVersion;
    }



}