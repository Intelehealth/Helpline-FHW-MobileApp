package org.intelehealth.app.activities.callflow.models;

import com.google.gson.annotations.SerializedName;

public class CallFlowResponse {
   boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    @SerializedName("data")
    private String data;
}
