package org.intelehealth.helpline.utilities;

import android.util.Log;

import org.intelehealth.helpline.appointment.api.ApiClientAppointment;
import org.intelehealth.helpline.appointment.dao.AppointmentDAO;
import org.intelehealth.helpline.appointment.model.CancelRequest;
import org.intelehealth.helpline.appointment.model.CancelResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentUtils {
    public void cancelAppointmentRequestOnVisitEnd(String visitID, int appointment_id, String reason, String providerID, String baseurl) {
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        CancelRequest request = new CancelRequest();

        request.setVisitUuid(visitID);
        request.setId(appointment_id);
        request.setReason(reason);
        request.setHwUUID(providerID);

        ApiClientAppointment.getInstance(baseurl).getApi().cancelAppointment(request).enqueue(new Callback<CancelResponse>() {
            @Override
            public void onResponse(Call<CancelResponse> call, Response<CancelResponse> response) {
                if (response.body() == null) return;
                CancelResponse cancelResponse = response.body();
                if (cancelResponse.isStatus()) {
                    appointmentDAO.deleteAppointmentByVisitId(visitID);
                }
            }

            @Override
            public void onFailure(Call<CancelResponse> call, Throwable t) {
                Log.v("onFailure", t.getMessage());
            }
        });
    }
}