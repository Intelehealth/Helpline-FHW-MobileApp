package org.intelehealth.helpline.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.intelehealth.helpline.R;
import org.intelehealth.helpline.activities.loginActivity.LoginActivityNew;
import org.intelehealth.helpline.app.IntelehealthApplication;
import org.intelehealth.helpline.networkApiCalls.interceptors.LogoutException;
import org.intelehealth.helpline.syncModule.SyncUtils;

public class NavigationUtils {

    /**
     * logout operation
     * @param context
     * @param t
     */
    public void logoutOperation(Context context, Throwable t){
        //checking LogoutException or not
        //if yes then log out operations are executing
        if(t instanceof LogoutException){
            NavigationUtils navigationUtils = this;
            navigationUtils.triggerSignOutOn401Response(context);
        }
    }

    public void triggerSignOutOn401Response(Context context) {
        //(Activity(context))
        Toast.makeText(context, context.getString(R.string.your_session_has_expired_please_log_in_again), Toast.LENGTH_SHORT).show();

        SessionManager sessionManager = new SessionManager(context);
        sessionManager.setReturningUser(false);
        sessionManager.setLogout(true);

        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);
        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();

        IntelehealthApplication.getInstance().disconnectSocket();
        Intent intent = new Intent(context, LoginActivityNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (context instanceof Activity activity) {
            activity.startActivity(intent);
            activity.finish();
        }
    }

}