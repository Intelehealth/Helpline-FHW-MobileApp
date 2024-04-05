package org.intelehealth.app.activities.visit.model;


import com.google.gson.annotations.Expose;

import org.intelehealth.app.activities.visit.model.prescribedmedication.En;
import org.intelehealth.app.activities.visit.model.prescribedmedication.Hi;

public class PrescribedMedication {
    @Expose
    private En en;
    @Expose
    private Hi hi;

    public En getEn() {
        return en;
    }
    public void setEn(En en) {
        this.en = en;
    }
    public Hi getHi() {
        return hi;
    }
    public void setHi(Hi hi) {
        this.hi = hi;
    }
}
