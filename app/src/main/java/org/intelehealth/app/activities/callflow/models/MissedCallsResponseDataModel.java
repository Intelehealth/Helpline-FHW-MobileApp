package org.intelehealth.app.activities.callflow.models;

import com.google.gson.annotations.SerializedName;

public class MissedCallsResponseDataModel {
    @SerializedName("CallTime")
    private String CallTime;

    public String getCallTime() {
        return CallTime;
    }

    public void setCallTime(String callTime) {
        CallTime = callTime;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNoanswer() {
        return Noanswer;
    }

    public void setNoanswer(String noanswer) {
        Noanswer = noanswer;
    }

    @SerializedName("Name")
    private String Name;

    @SerializedName("Noanswer")
    private String Noanswer;
}
