package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visit.model.DataForSMSPrescription;
import org.intelehealth.app.activities.visit.model.SendSMSRequestModel;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SMSApiCallManager {
    private static final String TAG = "SMSApiCallManager";
     String rxReturned ="";

    public static void checkInternetAndCallApi(String phoneNumber, Context context, String smsMsgBody) {
        if (NetworkConnection.isOnline(context)) {
            apiCallForSendSmsRequest(phoneNumber, context, smsMsgBody);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }
    }

    private static void apiCallForSendSmsRequest(String phoneNumber, Context context, String smsMsgBody) {
        Log.d(TAG, "apiCallForSendSmsRequest: smsMsgBody :" + smsMsgBody);
        Log.d(TAG, "apiCallForSendSmsRequest: phoneNumber :" + phoneNumber);
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(context);
        customProgressDialog.show(context.getResources().getString(R.string.please_wait));

        //String serverUrl = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/provider/" + sessionManager.getProviderID() + "/"; //${target_provider_uuid}/attribute/${target_provider_attribute_uuid}
        SendSMSRequestModel sendSMSRequestModel = new SendSMSRequestModel();
        sendSMSRequestModel.setModule(AppConstants.MODULE);
        sendSMSRequestModel.setApikey(AppConstants.APIKEY);
        sendSMSRequestModel.setFrom(AppConstants.FROM);
        sendSMSRequestModel.setTo(phoneNumber);
        sendSMSRequestModel.setCtid(AppConstants.CTID);
        sendSMSRequestModel.setMsg(Html.fromHtml(smsMsgBody).toString());

        Log.d(TAG, "apiCallForSendSmsRequest: reqmodel : " + new Gson().toJson(sendSMSRequestModel));

        String serverUrl = AppConstants.SMS_API_REQUEST_URL;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> smsRequest = apiService.sendSMS(serverUrl, sendSMSRequestModel.getModule(),
                sendSMSRequestModel.getApikey(), sendSMSRequestModel.getTo(), sendSMSRequestModel.getFrom(),
                sendSMSRequestModel.getMsg(), sendSMSRequestModel.getCtid());
        smsRequest.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody responseBody) {
                Log.d(TAG, "onNext: response sms : " + new Gson().toJson(responseBody));
                customProgressDialog.dismiss();
                try {
                    if (responseBody != null) {
                        String jsonData = responseBody.string();
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // Accessing values from the JSON object
                        String status = jsonObject.getString("Status");
                        System.out.println("Status: " + status);
                        if (!status.isEmpty() && status.equalsIgnoreCase("success")) {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.sms_sent), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.request_failed), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        System.out.println("Response body is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                customProgressDialog.dismiss();

                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                customProgressDialog.dismiss();

                Logger.logD(TAG, "completed");
            }
        });
    }

    public  DataForSMSPrescription jsonBasedPrescriptionTitle(Context context) {
        boolean hasLicense = false;
        String mFileName = "config.json";

        //Check for license key and load the correct config file
        if (!new SessionManager(context).getLicenseKey().isEmpty()) hasLicense = true;
        DataForSMSPrescription dataForSMSPrescription = null;
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context), String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(context, mFileName)));
            }
            String prescription1 = obj.getString("presciptionHeader1");

            String prescription2 = obj.getString("presciptionHeader2");
            dataForSMSPrescription = new DataForSMSPrescription(prescription1, prescription2);
          /*  //For AFI we are not using Respiratory Value
            if (obj.getBoolean("mResp")) {
                isRespiratory = true;
            } else {
                isRespiratory = false;
            }*/

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return dataForSMSPrescription;
    }

    public  String getMedicationDataNew(String patientUuid, String visitUuid, Context context) {
        Log.d(TAG, "getMedicationDataNew: rxReturned : " + rxReturned);
        if (rxReturned == null || rxReturned.isEmpty()) {
            return "";
        }


        String finalMedicationDataString = "";
        String titleStart = "<font color=" + Color.GRAY + ">";
        String titleEnd = "</font>";

        StringBuilder medicationData = new StringBuilder();
        String[] medicationDataArray = rxReturned.split("\n");
        for (String s : medicationDataArray) {
            if (!s.contains(":")) {
                medicationData.append(titleStart);
                medicationData.append(context.getString(R.string.additional_instruction));
                medicationData.append(titleEnd);
                //medicationData.append(",");
                medicationData.append(s);
            } else {
                medicationData.append(s);
                medicationData.append(", ");
                ///medicationData.append("<br>");
            }
        }
        if (medicationData.length() == 0) return "";

        finalMedicationDataString = medicationData.toString();
        Log.d(TAG, "getMedicationDataNew: finalMedicationDataString : " + finalMedicationDataString);

        return finalMedicationDataString;
    }

    private  void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.JSV_MEDICATIONS: {
                Log.i("TAG", "kkparse_va: " + value);
                Log.i("TAG", "kkparseData: rx" + rxReturned);
                if (rxReturned!=null && !rxReturned.trim().isEmpty() && !rxReturned.contains(value)) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
                Log.i("TAG", "kkparseData: rxfin" + rxReturned);
                //medication_txt.setText(Html.fromHtml(getMedicationData()));
                //checkForDoctor();
                break;
            }

            default:
                Log.i("TAG", "parseData: " + value);
                break;
        }
    }

/*
    private static String queryData(String dataString, String visitUuid, Context context) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Patient patient = new Patient();
        String medHistory;
        ObsDTO patHistory = new ObsDTO();
        String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitUuid);
        String vitalsUUID = fetchEncounterUuidForEncounterVitals(visitUuid);

        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};

        String table = "tbl_patient";
        String[] columnsToReturn = {"openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "gender"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndex("openmrs_id")));
                patient.setFirst_name(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddle_name(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLast_name(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDate_of_birth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

      */
/*  PatientsDAO patientsDAO = new PatientsDAO();
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("NationalID")) {
                    patient.setNationalID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();*//*

        String[] columns = {"value", " conceptuuid"};

      */
/*  try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ?";
            String[] famHistArgs = {adultInitialUUID, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
            Cursor famHistCursor = db.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            String famHistText = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistory.setValue(famHistText);
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            famHistory.setValue(""); // if family history does not exist
        }*//*


        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ?";

            String[] medHistArgs = {adultInitialUUID, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};

            Cursor medHistCursor = db.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            Log.d(TAG, "kkqueryData: medHistText :: "+medHistText);
            patHistory.setValue(medHistText);

            if (medHistText != null && !medHistText.isEmpty()) {

                medHistory = patHistory.getValue();


                medHistory = medHistory.replace("\"", "");
                medHistory = medHistory.replace("\n", "");
                do {
                    medHistory = medHistory.replace("  ", "");
                } while (medHistory.contains("  "));
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            patHistory.setValue(""); // if medical history does not exist
        }

       //vitals display code
        String visitSelection = "encounteruuid = ? AND voided!='1'";
        String[] visitArgs = {vitalsUUID};
        if (vitalsUUID != null) {
            try {
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
                if (visitCursor != null && visitCursor.moveToFirst()) {
                    do {
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        parseData(dbConceptID, dbValue);
                    } while (visitCursor.moveToNext());
                }
                if (visitCursor != null) {
                    visitCursor.close();
                }
            } catch (SQLException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        //adult intails display code
        String encounterselection = "encounteruuid = ? AND conceptuuid != ? AND conceptuuid != ? AND voided!='1'";
        String[] encounterargs = {adultInitialUUID, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE};
        Cursor encountercursor = db.query("tbl_obs", columns, encounterselection, encounterargs, null, null, null);
        try {
            if (encountercursor != null && encountercursor.moveToFirst()) {
                do {
                    String dbConceptID = encountercursor.getString(encountercursor.getColumnIndex("conceptuuid"));
                    String dbValue = encountercursor.getString(encountercursor.getColumnIndex("value"));
                    parseData(dbConceptID, dbValue);
                } while (encountercursor.moveToNext());
            }
            if (encountercursor != null) {
                encountercursor.close();
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
        }
        return getMedicationDataNew(patientSelection, visitUuid, context);
    }
*/

    public String stringToWeb_sms(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<b style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "";
            formatted = para_open + "- " + input.replaceAll("\n", para_close + para_open + "- ") + para_close;
        }
        return formatted;
    }

    public  String sms_prescription(PrescriptionModel model, Context context) {
        rxReturned = "";
        String mPatientName1 = model.getFirst_name() + " " + ((!TextUtils.isEmpty(model.getMiddle_name())) ? model.getMiddle_name() : "") + " " + model.getLast_name();
        String mPatientName = mPatientName1.substring(0, Math.min(mPatientName1.length(), 30));
        String mPatientDob = model.getDob();
        String mGender = model.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println(context.getString(R.string.current_time) + c.getTime());

        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(mPatientDob);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        // Log.d(TAG, "sms_prescription: medication_txt : " + medication_txt);
        // medication_txt
        String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
        String prescription_link = new VisitAttributeListDAO().getVisitAttributesList_specificVisit(model.getVisitUuid(), PRESCRIPTION_LINK);
        String finalPrescUrl = partial_whatsapp_presc_url + Uri.encode("#") + prescription_link;
        Log.d("TAG", "sms_prescription: prescription_link : " + prescription_link);
        DataForSMSPrescription dataForSMSPrescription = jsonBasedPrescriptionTitle(context);
        String heading = dataForSMSPrescription.getPrescription1();
        String heading2 = dataForSMSPrescription.getPrescription2();
        String heading3 = "<br/>";

        String medicationData1 = Html.fromHtml(queryData(model.getPatientUuid(), model.getVisitUuid(), context)).toString();
        Log.d(TAG, "sms_prescription:medicationData1  :  " + medicationData1);
        // String medicationData1 = Html.fromHtml(getMedicationDataNew(model.getPatientUuid(), model.getVisitUuid(), context)).toString();
        String medicationData = medicationData1.substring(0, Math.min(medicationData1.length(), 60));
        //String rx_web = stringToWeb(medicationData).replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">●", "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">").replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\"></p>", "</p>");
        String rx_webFinal = medicationData + "...";
        String htmlDocument = String.format("<b id=\"heading_1\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b><br>" + "<b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b>" + "<br><br>" +

                        "<b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Patient %s</b><br>" + "<b id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </b>" + "<br><br>" +

                        "<b id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication<br>" + "%s </b>"

                /*"</div>"*/, heading, heading2, /*heading3,*/ mPatientName, age, mGender, /*mSdw*/
//                            address, mPatientOpenMRSID, mDate,
                !medicationData.isEmpty() ? rx_webFinal : stringToWeb_sms("Not Provided...")) + "<br><br>Download full prescription at " + finalPrescUrl + " --- Powered by Intelehealth";

        Log.d("html", "html:ppp " + Html.fromHtml(htmlDocument));

        return htmlDocument;
    }

    public String queryData(String dataString, String visitUuid, Context context) {
        ObsDTO famHistory = new ObsDTO();

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Patient patient = new Patient();

        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};

        String table = "tbl_patient";
        String[] columnsToReturn = {"openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "country", "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndex("openmrs_id")));
                patient.setFirst_name(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddle_name(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLast_name(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDate_of_birth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();
        downloadPrescriptionDefault(visitUuid);

        return getMedicationDataNew(patientSelection, visitUuid, context);

        //downloadDoctorDetails();
    }

    public void downloadPrescriptionDefault(String visitUuid) {
        boolean hasPrescription = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();

        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitUuid, "0"}; // so that the deleted values dont come in the presc.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                hasPrescription = true; //if any kind of prescription data is present...
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

}
