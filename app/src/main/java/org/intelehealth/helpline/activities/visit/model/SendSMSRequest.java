package org.intelehealth.helpline.activities.visit.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
