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
import com.google.gson.Gson
import org.intelehealth.helpline.BuildConfig
import org.intelehealth.helpline.R
import org.intelehealth.helpline.activities.callflow.adapter.CallRecordingsAdapter
import org.intelehealth.helpline.activities.callflow.adapter.MissedCallsAdapter
import org.intelehealth.helpline.activities.callflow.models.CallRequestModel
import org.intelehealth.helpline.activities.callflow.models.CallTypeViewModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseDataModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel
import org.intelehealth.helpline.activities.callflow.utils.setupLinearView
import org.intelehealth.helpline.app.AppConstants
import org.intelehealth.helpline.databinding.FragmentLayoutBinding
import org.intelehealth.helpline.utilities.NetworkConnection
import java.util.LinkedList


class OutgoingMissedCallsFragment : Fragment(R.layout.fragment_layout) {
    private val TAG = "OutgoingMissedCallsFrag"
    protected lateinit var binding: FragmentLayoutBinding
    protected lateinit var adapter: MissedCallsAdapter
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

        // showProgressbarForInitialLoading(false, "onViewCreated")

        initListView()
        getMissedCalls()
        observeData()
    }

    fun getEmptyDataMessage(): String = getString(
            R.string.no_data_message,
            getString(R.string.outgoing)
    )

    fun getEmptyDataIcon(): Int = R.drawable.no_data_icon

    private fun getMissedCalls() {
        val finalURL = BuildConfig.SERVER_URL + "/noanswer/" + pageNo

        if (NetworkConnection.isOnline(context)) {
            viewModel = ViewModelProvider(this, ViewModelProvider.Factory.from(CallTypeViewModel.initializer)).get(CallTypeViewModel::class.java)
            val callRequestModel = CallRequestModel(finalURL, AppConstants.AUTH_HEADER_CALL_FLOW)
            viewModel.getMissedCall(callRequestModel)
        } else {
            Toast.makeText(context, resources.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): OutgoingMissedCallsFragment {
            return OutgoingMissedCallsFragment()
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
        viewModel.failDataResult.observe(viewLifecycleOwner) { failureResultData ->
            isLoading = false;
            isFirstTimeLoading = false
        }

        // api failure
        viewModel.errorDataResult.observe(viewLifecycleOwner)
        { errorResult ->
            Log.d(TAG, "observeData: errorDataResult")
            isLoading = false;
            isFirstTimeLoading = false
            Toast.makeText(context, resources.getString(R.string.failed_to_connect), Toast.LENGTH_SHORT).show()
        }

    }

    private fun initListView() {
        dataList = ArrayList()
        adapter = MissedCallsAdapter(requireContext(), LinkedList())
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
        //success
        if (result.outgoingCalls.isNotEmpty()) {
            isLoading = false
            adapter.updateItems(result.outgoingCalls)
            pageNo++
            adapter.notifyDataSetChanged()
        } else {
            isLastPage = true
            if (pageNo == 0) {
                binding.tvCallLogEmptyMessage.visibility = View.VISIBLE
            }

        }

    }

    private fun loadMoreItems() {
        isLoading = true
        adapter.addLoading()
        getMissedCalls();
    }

}
