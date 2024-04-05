package org.intelehealth.app.activities.visit.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.app.appointment.model.BookAppointmentRequest;

import java.util.List;

public class SendSMSRequest {

    public SendSMSRequestModel getData() {
        return data;
    }

    public void setData(SendSMSRequestModel data) {
        this.data = data;
    }

    @SerializedName("data")
    @Expose
    private SendSMSRequestModel data = null;
}
