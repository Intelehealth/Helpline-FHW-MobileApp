package org.intelehealth.helpline.activities.callflow;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.helpline.BuildConfig;
import org.intelehealth.helpline.R;
import org.intelehealth.helpline.activities.callflow.adapter.CallRecordingsAdapter;
import org.intelehealth.helpline.activities.callflow.models.CallFlowResponseData;
import org.intelehealth.helpline.activities.callflow.models.CallFlowResponseModelClass;
import org.intelehealth.helpline.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.helpline.app.AppConstants;
import org.intelehealth.helpline.databinding.ActivityCallRecordingBinding;
import org.intelehealth.helpline.networkApiCalls.ApiClient;
import org.intelehealth.helpline.networkApiCalls.ApiInterface;
import org.intelehealth.helpline.syncModule.SyncUtils;
import org.intelehealth.helpline.utilities.NetworkUtils;
import org.intelehealth.helpline.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class CallRecordingsActivity extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "CallRecordingsActivity";
    private ObjectAnimator syncAnimator;
    private SessionManager sessionManager;
    private ActivityCallRecordingBinding binding;
    private List<CallFlowResponseData> dataList;
    private int pageNumber = 0; // Initial page number
    CallRecordingsAdapter recordedCallsAdapter;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private ImageView ivIsInternet;
    private NetworkUtils networkUtils;
    private boolean isFirstTimeLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallRecordingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        networkUtils = new NetworkUtils(CallRecordingsActivity.this, this);

        initUI();
    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_recordings);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ImageView ivBackArrow = toolbar.findViewById(R.id.iv_back_arrow_common);
        tvTitle.setText(getResources().getString(R.string.call_recordings));
        ivIsInternet.setVisibility(View.VISIBLE);

        ivIsInternet.setOnClickListener(v -> {
            SyncUtils.syncNow(CallRecordingsActivity.this, ivIsInternet, syncAnimator);
        });

        ivBackArrow.setOnClickListener(v -> {
            onBackPressed();
        });

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvRecordedCalls.setLayoutManager(layoutManager);

        dataList = new ArrayList<>();
        recordedCallsAdapter = new CallRecordingsAdapter(CallRecordingsActivity.this, dataList);
        binding.rvRecordedCalls.setAdapter(recordedCallsAdapter);
        // Set up RecyclerView with adapter
       /* binding.rvRecordedCalls.setAdapter(new NewRecordedCallsAdapter(CallRecordingsActivity.this, dataList, binding.rvRecordedCalls, item -> {

        }));*/


        binding.rvRecordedCalls.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= 10) {
                        loadMoreItems();
                    }
                }
            }
        });

        // Initial API call
        isFirstTimeLoading = true;
        apiCallForGetListOfRecordedCalls(CallRecordingsActivity.this, pageNumber);
    }

    private void apiCallForGetListOfRecordedCalls(Context context, int pageNumber) {
        String finalURL = BuildConfig.SERVER_URL + "/recordings/" + sessionManager.getLoginHWMobileNumber() + "/" + pageNumber;
        if (sessionManager.getLoginHWMobileNumber().isEmpty()) return;
        //CustomProgressDialog customProgressDialog = new CustomProgressDialog(context);
        //customProgressDialog.show(context.getResources().getString(R.string.please_wait));
        showProgressbarForInitialLoading(false);

        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<CallFlowResponseModelClass> resultsObservable = apiService.getCallRecordings(finalURL, AppConstants.AUTH_HEADER_CALL_FLOW);
            resultsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<CallFlowResponseModelClass>() {
                @Override
                public void onNext(CallFlowResponseModelClass res) {
                    //customProgressDialog.dismiss();
                    showProgressbarForInitialLoading(true);
                    isFirstTimeLoading = false;
                    Log.d(TAG, "onNext: missed data 1 : " + new Gson().toJson(res));
                    if (!res.getStatus().isEmpty() && res.getStatus().equalsIgnoreCase("ok") && res.getData() != null && res.getData().size() != 0) {
                        updateDataInView(res.getData());
                    } else {
                        isLastPage = true;
                        //no data
                        manageUIVisibility(false);
                    }
                    isLoading = false; // Reset loading flag
                    recordedCallsAdapter.removeLoading(); // Remove loading item after data is loaded
                }

                @Override
                public void onError(Throwable e) {
                    isLoading = false;
                    isFirstTimeLoading = false;
                    showProgressbarForInitialLoading(true);
                    //customProgressDialog.dismiss();
                    Toast.makeText(context, context.getResources().getString(R.string.try_later), Toast.LENGTH_LONG).show();
                    recordedCallsAdapter.removeLoading(); // Remove loading item in case of error
                }

                @Override
                public void onComplete() {
                    isFirstTimeLoading = false;
                    // customProgressDialog.dismiss();
                    showProgressbarForInitialLoading(true);

                }
            });
        } catch (IllegalArgumentException e) {
        }

    }

    private void updateDataInView(List<CallFlowResponseData> data) {
        dataList.addAll(data);
        recordedCallsAdapter.notifyDataSetChanged();
        pageNumber++; // Increment page number for next call
        manageUIVisibility(true);
    }

    /*  private void loadMoreItems() {
          isLoading = true; // Set loading to true before making API call
          apiCallForGetListOfRecordedCalls(CallRecordingsActivity.this, pageNumber);
      }*/
    private void loadMoreItems() {
        isLoading = true; // Set loading to true before making API call
        recordedCallsAdapter.addLoading(); // Add loading item to show progress bar
        apiCallForGetListOfRecordedCalls(CallRecordingsActivity.this, pageNumber);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordedCallsAdapter != null) {
            recordedCallsAdapter.releaseMediaPlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recordedCallsAdapter != null) {
            recordedCallsAdapter.releaseMediaPlayer();
        }
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CallRecordingsActivity.this, HomeScreenActivity_New.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }

    private void manageUIVisibility(boolean isDataAvailable) {
        if (dataList != null && dataList.size() > 0) {
            binding.rvRecordedCalls.setVisibility(View.VISIBLE);
            binding.nodataRecordings.setVisibility(View.GONE);
        } else {
            binding.nodataRecordings.setVisibility(View.VISIBLE);
            binding.rvRecordedCalls.setVisibility(View.GONE);

        }

    }

    private void showProgressbarForInitialLoading(boolean wantToDismiss) {
        if (isFirstTimeLoading) {
            binding.layoutLoader.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.layoutLoader.getRoot().setVisibility(View.GONE);
        }
        if (wantToDismiss) {
            int visibility = binding.layoutLoader.getRoot().getVisibility();
            boolean isProgressBarVisible = (visibility == View.VISIBLE);
            if (isProgressBarVisible) {
                binding.layoutLoader.getRoot().setVisibility(View.GONE);
                binding.layoutLoader.getRoot().setVisibility(View.GONE);
            }
        }
    }
  /*  private void showProgressbarForInitialLoading(boolean wantToDismiss) {
        binding.progressBarRecording.setVisibility(isFirstTimeLoading ? View.VISIBLE : View.GONE);
        if (wantToDismiss) {
            int visibility = binding.progressBarRecording.getVisibility();
            boolean isProgressBarVisible = (visibility == View.VISIBLE);
            if (isProgressBarVisible){
                binding.progressBarRecording.setVisibility(View.GONE);
                binding.textViewLoading.setVisibility(View.GONE);
            }
        }
    }*/
}
