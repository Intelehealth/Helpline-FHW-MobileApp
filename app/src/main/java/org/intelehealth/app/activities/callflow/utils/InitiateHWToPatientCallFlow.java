package org.intelehealth.app.activities.callflow.utils;

import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.github.ajalt.timberkt.Timber;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.callflow.models.CallFlowResponse;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.models.callflow.CallFlowRequestParamsModel;
import org.intelehealth.app.models.hwprofile.PersonAttributes;
import org.intelehealth.app.models.hwprofile.Profile;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class InitiateHWToPatientCallFlow {
    private static final String TAG = "InitiateHWToPatientCall";

    public void initiateCallFlowFromHwToPatient(String patientPhoneNumber, Context context, String patientUuid) {
        fetchHWMobileNumber(context, patientPhoneNumber, patientUuid);
    }

    private void callToInitiate(Context context, String patientUuid, String HWMobileNo, String patientPhone) {
        if (patientUuid != null && !patientUuid.isEmpty()) {
            try {
                patientPhone = StringUtils.mobileNumberEmpty(phoneNumber(patientUuid));
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
        }
        if (HWMobileNo == null || HWMobileNo.isEmpty() || HWMobileNo.equalsIgnoreCase("N/A")) {
            Toast.makeText(context, context.getResources().getString(R.string.mobile_no_not_registered), Toast.LENGTH_LONG).show();
        } else if (patientPhone == null || patientPhone.isEmpty() || patientPhone.equalsIgnoreCase("N/A")) {
            Toast.makeText(context, context.getResources().getString(R.string.mobile_no_not_registered), Toast.LENGTH_LONG).show();
        } else {
            CustomProgressDialog customProgressDialog = new CustomProgressDialog(context);
            customProgressDialog.show(context.getResources().getString(R.string.please_wait_for_call));
            String serverUrl = BuildConfig.SERVER_URL + ":" + AppConstants.PORT_NUMBER;
            ApiClient.changeApiBaseUrl(serverUrl);
            ApiInterface apiService = ApiClient.createService(ApiInterface.class);
            try {
                CallFlowRequestParamsModel callFlowRequestParamsModel = new CallFlowRequestParamsModel();
                callFlowRequestParamsModel.setCallerMobileNo(HWMobileNo);
                callFlowRequestParamsModel.setPatientMobileNo(patientPhone);
                Observable<CallFlowResponse> resultsObservable = apiService.initiateCallFlow(callFlowRequestParamsModel);
                resultsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<CallFlowResponse>() {
                            @Override
                            public void onNext(CallFlowResponse callFlowResponse) {
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    customProgressDialog.dismiss();
                                    if (callFlowResponse.isSuccess())
                                        Toast.makeText(context, context.getResources().getString(R.string.call_initiated), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(context, context.getResources().getString(R.string.try_later), Toast.LENGTH_SHORT).show();

                                }, 2000);

                            }

                            @Override
                            public void onError(Throwable e) {
                                customProgressDialog.dismiss();
                                Toast.makeText(context, context.getResources().getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void fetchHWMobileNumber(Context context, String patientPhoneNumber, String patientUuid) {
        String uuid = new SessionManager(context).getCreatorID();
        ProviderDAO providerDAO = new ProviderDAO();
        String url = new UrlModifiers().getHWProfileDetails(uuid);

        Observable<Profile> profileDetailsDownload = AppConstants.apiInterface.PROVIDER_PROFILE_DETAILS_DOWNLOAD(url, "Basic " + new SessionManager(context).getEncoded());
        profileDetailsDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<Profile>() {
            @Override
            public void onNext(Profile profile) {
                if (profile != null) {
                    Timber.tag(TAG).d("Profile =>%s", new Gson().toJson(profile));
                    List<PersonAttributes> personAttributes = new ArrayList<>();
                    personAttributes = profile.getResults().get(0).getAttributes();
                    if (personAttributes != null && personAttributes.size() > 0) {
                        for (int i = 0; i < personAttributes.size(); i++) {
                            String attributeName = personAttributes.get(i).getAttributeTpe().getDisplay();
                            if (attributeName.equalsIgnoreCase("phoneNumber") && !personAttributes.get(i).isVoided()) {
                                String mobileNo = personAttributes.get(i).getValue().toString();
                                callToInitiate(context, patientUuid, mobileNo, patientPhoneNumber);
                            }

                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
                Toast.makeText(context, context.getResources().getString(R.string.try_later), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
            }
        });
    }

}
