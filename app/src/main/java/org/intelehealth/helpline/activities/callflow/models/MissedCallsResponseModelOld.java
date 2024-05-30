package org.intelehealth.helpline.activities.callflow.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MissedCallsResponseModelOld {
    @SerializedName("data")
    private List<MissedCallsResponseDataModel> data;
    @SerializedName("status")
    private String status;

    public List<MissedCallsResponseDataModel> getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }
}
