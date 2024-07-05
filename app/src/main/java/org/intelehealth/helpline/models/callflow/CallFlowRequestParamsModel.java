package org.intelehealth.helpline.models.callflow;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallFlowRequestParamsModel {

    @SerializedName("callerMobileNo")
    @Expose
    private String callerMobileNo;

    public String getCallerMobileNo() {
        return callerMobileNo;
    }

    public void setCallerMobileNo(String callerMobileNo) {
        this.callerMobileNo = callerMobileNo;
    }

    public String getPatientMobileNo() {
        return patientMobileNo;
    }

    public void setPatientMobileNo(String patientMobileNo) {
        this.patientMobileNo = patientMobileNo;
    }

    @SerializedName("patientMobileNo")
    @Expose
    private String patientMobileNo;
}
