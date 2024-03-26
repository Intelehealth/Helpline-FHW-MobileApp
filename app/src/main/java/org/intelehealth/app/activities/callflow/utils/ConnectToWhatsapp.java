package org.intelehealth.app.activities.callflow.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

public class ConnectToWhatsapp {
    public static void connectToWhatsappOnUserProfile(Context context, String phoneNumber){
        SessionManager sessionManager = new SessionManager(context);
        try {
            String nurseName = new ProviderDAO().getProviderName(sessionManager.getProviderID(), ProviderDTO.Columns.PROVIDER_UUID.value);
            if (phoneNumber != null && !phoneNumber.equalsIgnoreCase("")) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(
                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                        phoneNumber, context.getResources().getString(R.string.nurse_whatsapp_message, nurseName)))));
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.mobile_no_not_registered), Toast.LENGTH_LONG).show();
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }
}
