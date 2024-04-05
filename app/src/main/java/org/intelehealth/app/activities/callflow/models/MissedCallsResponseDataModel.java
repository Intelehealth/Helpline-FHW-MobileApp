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

    public String getCallType() {
        return CallType;
    }

    public void setCallType(String callType) {
        CallType = callType;
    }

    public String getPatientNumber() {
        return PatientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        PatientNumber = patientNumber;
    }

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    @SerializedName("CallType")
    private String CallType;

    @SerializedName("PatientNumber")
    private String PatientNumber;
    @SerializedName("record_id")
    private String record_id;

}
