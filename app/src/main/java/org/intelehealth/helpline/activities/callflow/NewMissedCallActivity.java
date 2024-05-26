package org.intelehealth.helpline.activities.callflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.google.gson.Gson;

import org.intelehealth.helpline.BuildConfig;
import org.intelehealth.helpline.R;
import org.intelehealth.helpline.activities.callflow.adapter.MissedCallsAdapter;
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseDataModel;
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModelOld;
import org.intelehealth.helpline.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.helpline.app.AppConstants;
import org.intelehealth.helpline.databinding.ActivityMissedCallsBinding;
import org.intelehealth.helpline.networkApiCalls.ApiClient;
import org.intelehealth.helpline.networkApiCalls.ApiInterface;
import org.intelehealth.helpline.syncModule.SyncUtils;
import org.intelehealth.helpline.utilities.NetworkConnection;
import org.intelehealth.helpline.utilities.NetworkUtils;
import org.intelehealth.helpline.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class NewMissedCallActivity extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "NewMissedCallActivity";
    private MissedCallsAdapter adapter;
    private List<MissedCallsResponseDataModel> dataList;
    private int pageNumber = 0; // Initial page number
    private ActivityMissedCallsBinding binding;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private SessionManager sessionManager;
    private ObjectAnimator syncAnimator;
    private ImageView ivIsInternet;
    private NetworkUtils networkUtils;
    private boolean isFirstTimeLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMissedCallsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        networkUtils = new NetworkUtils(NewMissedCallActivity.this, this);

        initUI();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvMissedCalls.setLayoutManager(layoutManager);

        dataList = new ArrayList<>();
        adapter = new MissedCallsAdapter(NewMissedCallActivity.this, dataList);
        // Set up RecyclerView with adapter
        binding.rvMissedCalls.setAdapter(adapter);

        // Pagination listener
        binding.rvMissedCalls.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 10) {
                        loadMoreItems();
                    }
                }
            }
        });


        // Initial API call
        Log.d(TAG, "onCreate: checkkk");
        isFirstTimeLoading = true;
        apiCallForGetListOfMissedCalls(NewMissedCallActivity.this, pageNumber);
    }


    /* private void loadMoreItems() {
         isLoading = true; // Set loading to true before making API call
         apiCallForGetListOfMissedCalls(NewMissedCallActivity.this, pageNumber);
     }*/
    private void loadMoreItems() {
        isLoading = true; // Set loading to true before making API call
        adapter.addLoading(); // Add loading item to show progress bar
        apiCallForGetListOfMissedCalls(NewMissedCallActivity.this, pageNumber);
    }

    private boolean isLoading() {
        // Implement logic to check if data is currently being loaded
        return false;
    }

    private boolean isLastPage() {
        // Implement logic to check if current page is the last page
        return false;
    }

    private void apiCallForGetListOfMissedCalls(Context context, int page) {
        Log.d(TAG, "apiCallForGetListOfMissedCalls: page: " + page);
        if (NetworkConnection.isOnline(context)) {
            String finalURL = BuildConfig.SERVER_URL + "/noanswer/" + page;

            if (new SessionManager(context).getLoginHWMobileNumber().isEmpty()) {
                return;
            }
            // CustomProgressDialog customProgressDialog = new CustomProgressDialog(context);
            // customProgressDialog.show(context.getResources().getString(R.string.please_wait));
            showProgressbarForInitialLoading(false);
            ApiInterface apiService = ApiClient.createService(ApiInterface.class);
            try {
                Observable<MissedCallsResponseModelOld> resultsObservable = apiService.getMissedCalls(finalURL, AppConstants.AUTH_HEADER_CALL_FLOW);
                resultsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<>() {
                            @Override
                            public void onNext(MissedCallsResponseModelOld res) {
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
                                adapter.removeLoading(); // Remove loading item after data is loaded
                            }

                            @Override
                            public void onError(Throwable e) {
                                isLoading = false;
                                isFirstTimeLoading = false;
                                showProgressbarForInitialLoading(true);
                                //customProgressDialog.dismiss();
                                Toast.makeText(context, context.getResources().getString(R.string.try_later), Toast.LENGTH_LONG).show();
                                adapter.removeLoading(); // Remove loading item in case of error
                            }

                            @Override
                            public void onComplete() {
                                isFirstTimeLoading = false;
                                // customProgressDialog.dismiss();
                                showProgressbarForInitialLoading(true);

                            }
                        });
            } catch (IllegalArgumentException e) {
                isLoading = false; // Reset loading flag on exception\
                isFirstTimeLoading = false;
                showProgressbarForInitialLoading(true);

            }
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }

    }

    private void manageUIVisibility(boolean isDataAvailable) {
        if (dataList != null && dataList.size() > 0) {
            binding.rvMissedCalls.setVisibility(View.VISIBLE);
            binding.nodataMissedCall.setVisibility(View.GONE);
        } else {
            binding.nodataMissedCall.setVisibility(View.VISIBLE);
            binding.rvMissedCalls.setVisibility(View.GONE);

        }

    }

    private void updateDataInView(List<MissedCallsResponseDataModel> data) {
        Log.d(TAG, "updateDataInView: data : " + data.size());
        dataList.addAll(data);
        adapter.notifyDataSetChanged();
        pageNumber++; // Increment page number for next call
        manageUIVisibility(true);
    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_missed_call);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ImageView ivBackArrow = toolbar.findViewById(R.id.iv_back_arrow_common);
        tvTitle.setText(getResources().getString(R.string.missed_calls));
        ivIsInternet.setVisibility(View.VISIBLE);
        ivIsInternet.setOnClickListener(v -> {
            SyncUtils.syncNow(NewMissedCallActivity.this, ivIsInternet, syncAnimator);
        });

        ivBackArrow.setOnClickListener(v -> {
            Intent intent = new Intent(NewMissedCallActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
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

    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /* private void showProgressbarForInitialLoading(boolean wantToDismiss) {
         binding.progressBarMissed.setVisibility(isFirstTimeLoading ? View.VISIBLE : View.GONE);
         if (wantToDismiss) {
             int visibility = binding.progressBarMissed.getVisibility();
             boolean isProgressBarVisible = (visibility == View.VISIBLE);
             if (isProgressBarVisible)
                 binding.progressBarMissed.setVisibility(View.GONE);
         }
     }*/
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
}
