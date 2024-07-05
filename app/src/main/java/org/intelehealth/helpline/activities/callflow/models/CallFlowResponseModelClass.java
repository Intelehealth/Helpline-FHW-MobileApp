package org.intelehealth.helpline.activities.callflow.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CallFlowResponseModelClass<T> {
    @SerializedName("data")
    private List<CallFlowResponseData> data;
    @SerializedName("status")
    private String status;

    public List<CallFlowResponseData> getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }
}
