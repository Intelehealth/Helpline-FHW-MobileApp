package org.intelehealth.helpline.activities.callflow.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import org.intelehealth.helpline.BuildConfig;
import org.intelehealth.helpline.activities.callflow.datasource.CallTypesDataSource;
import org.intelehealth.helpline.activities.callflow.repository.CallTypesRepository;
import org.intelehealth.helpline.app.AppConstants;
import org.intelehealth.helpline.networkApiCalls.ApiClient;
import org.intelehealth.helpline.networkApiCalls.ApiInterface;
import org.intelehealth.klivekit.call.utils.CallType;

import java.util.List;

public class CallTypeViewModel extends BaseViewModel {
    private static final String TAG = "ForgotPasswordViewModel";
    private CallTypesRepository repository;

    private final MutableLiveData<MissedCallsResponseModel> missedCallResultData = new MutableLiveData<MissedCallsResponseModel>();
    public LiveData<MissedCallsResponseModel> missedCallResult = missedCallResultData;

    public CallTypeViewModel(CallTypesRepository repository) {
        this.repository = repository;
    }


    public static final ViewModelInitializer<CallTypeViewModel> initializer = new ViewModelInitializer<>(
            CallTypeViewModel.class,
            creationExtras -> {
                String BASE_URL = BuildConfig.SERVER_URL + ":" + AppConstants.PORT_NUMBER;
                ApiClient.changeApiBaseUrl(BASE_URL);
                ApiInterface apiService = ApiClient.createService(ApiInterface.class);
                CallTypesDataSource dataSource = new CallTypesDataSource(apiService);
                CallTypesRepository requestOtpRepository = new CallTypesRepository(dataSource);
                return new CallTypeViewModel(requestOtpRepository);
            }
    );


    public void clearPreviousResult() {
        missedCallResultData.postValue(null);
    }

    /*public void getMissedCall(CallRequestModel callRequestModel) {
        repository.getMissedCalls(new ExecutionListener<MissedCallsResponseModel>() {
            @Override
            public void onSuccess(@NonNull List<? extends MissedCallsResponseDataModel> result) {
                missedCallResultData.postValue(result);
            }
        }, callRequestModel);
    }*/
    public void getMissedCall(CallRequestModel callRequestModel) {
        repository.getMissedCalls(new ExecutionListener<MissedCallsResponseModel>() {
            @Override
            public void onSuccess(MissedCallsResponseModel result) {
                missedCallResultData.postValue(result);
            }
        }, callRequestModel);
    }

}
