package org.intelehealth.helpline.activities.visit.model;

public class DataForSMSPrescription {
    String prescription1;

    public DataForSMSPrescription(String prescription1, String prescription2) {
        this.prescription1 = prescription1;
        this.prescription2 = prescription2;
    }

    public String getPrescription1() {
        return prescription1;
    }

    public void setPrescription1(String prescription1) {
        this.prescription1 = prescription1;
    }

    public String getPrescription2() {
        return prescription2;
    }

    public void setPrescription2(String prescription2) {
        this.prescription2 = prescription2;
    }

    String prescription2;
}
