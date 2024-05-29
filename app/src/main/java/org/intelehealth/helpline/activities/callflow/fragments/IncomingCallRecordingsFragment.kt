package org.intelehealth.helpline.activities.callflow.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.helpline.BuildConfig
import org.intelehealth.helpline.R
import org.intelehealth.helpline.activities.callflow.adapter.CallRecordingsAdapter
import org.intelehealth.helpline.activities.callflow.models.CallRequestModel
import org.intelehealth.helpline.activities.callflow.models.CallTypeViewModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseDataModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel
import org.intelehealth.helpline.activities.callflow.utils.setupLinearView
import org.intelehealth.helpline.app.AppConstants
import org.intelehealth.helpline.databinding.FragmentLayoutBinding
import org.intelehealth.helpline.utilities.NetworkConnection
import org.intelehealth.helpline.utilities.SessionManager
import java.util.LinkedList

class IncomingCallRecordingsFragment : Fragment(R.layout.fragment_layout) {
    private val TAG = "IncomingCallRecordingsF"
    protected lateinit var binding: FragmentLayoutBinding
    protected lateinit var adapter: CallRecordingsAdapter
    private var isLastPage = false
    private var isLoading = false
    private var pageNo = 0
    private var dataList: List<MissedCallsResponseDataModel>? = null
    private var isFirstTimeLoading = false
    private lateinit var viewModel: CallTypeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLayoutBinding.bind(view)
        binding.emptyMessage = getEmptyDataMessage()
        binding.emptyDataIcon = getEmptyDataIcon()
        isFirstTimeLoading = true


        initListView()
        getMissedCalls()
        observeData()
    }

    private fun getEmptyDataMessage(): String = getString(
            R.string.no_data_message,
            getString(R.string.incoming)
    )

    private fun getEmptyDataIcon(): Int = R.drawable.no_data_icon

    private fun getMissedCalls() {
        val finalURL = BuildConfig.SERVER_URL + "/recordings/" + SessionManager(context).loginHWMobileNumber + "/" + pageNo
        if (NetworkConnection.isOnline(context)) {
            viewModel = ViewModelProvider(this, ViewModelProvider.Factory.from(CallTypeViewModel.initializer)).get(CallTypeViewModel::class.java)
            val callRequestModel = CallRequestModel(finalURL, AppConstants.AUTH_HEADER_CALL_FLOW)
            viewModel.getMissedCall(callRequestModel)
        } else {
            Toast.makeText(context, resources.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): IncomingCallRecordingsFragment {
            return IncomingCallRecordingsFragment()
        }
    }

    private fun observeData() {
        viewModel.missedCallResult.observe(viewLifecycleOwner, Observer { missedCallResult ->
            missedCallResult?.let {
                isFirstTimeLoading = false
                isFirstTimeLoading = false
                bindData(it)
            }
        })

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) adapter.addLoading() else adapter.removeLoading()

        }
        viewModel.failDataResult.observe(viewLifecycleOwner) {
            isLoading = false;
            isFirstTimeLoading = false
        }

        viewModel.errorDataResult.observe(viewLifecycleOwner)
        {
            isLoading = false;
            isFirstTimeLoading = false
            Toast.makeText(context, resources.getString(R.string.failed_to_connect), Toast.LENGTH_SHORT).show()
        }

    }

    private fun initListView() {
        dataList = ArrayList()
        adapter = CallRecordingsAdapter(requireContext(), LinkedList())
        val layoutManager = binding.rvPrescription.setupLinearView(adapter)

        binding.rvPrescription.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Handler(Looper.getMainLooper()).post(Runnable {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= 10
                        ) {
                            loadMoreItems()
                        }
                    }
                })

            }
        })

    }


    private fun bindData(result: MissedCallsResponseModel) {
        if (result.incomingCalls.isNotEmpty()) {
            isLoading = false
            adapter.updateItems(result.incomingCalls)
            pageNo++ // Increment page number for next call
            adapter.notifyDataSetChanged()
        } else {
            isLastPage = true
            if (pageNo == 0) {
                binding.tvCallLogEmptyMessage.visibility = View.VISIBLE
            }

        }

    }

    private fun loadMoreItems() {
        isLoading = true // Set loading to true before making API call
        adapter.addLoading() // Add loading item to show progress bar
        getMissedCalls();
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "kaverionPause: ")
        adapter.resetMediaPlayer()
    }

}
