package org.intelehealth.helpline.activities.callflow.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.helpline.R
import org.intelehealth.helpline.activities.callflow.adapter.CallsTypeAdapter
import org.intelehealth.helpline.activities.callflow.models.CallTypeViewModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel
import org.intelehealth.helpline.activities.callflow.utils.setupLinearView
import org.intelehealth.helpline.databinding.FragmentLayoutBinding
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import java.util.LinkedList

abstract class CallTypesFragment : Fragment(R.layout.fragment_layout),
        BaseViewHolder.ViewHolderClickListener {
    protected lateinit var binding: FragmentLayoutBinding
    protected lateinit var adapter: CallsTypeAdapter
    private var isLastPage = false
    private var isLoading = false

    protected val viewMode: CallTypeViewModel by lazy {
        ViewModelProvider(
                this, ViewModelProvider.Factory.from(CallTypeViewModel.initializer)
        )[CallTypeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLayoutBinding.bind(view)
        binding.emptyMessage = getEmptyDataMessage()
        binding.emptyDataIcon = getEmptyDataIcon()
        initListView()
    }

    private fun initListView() {
        adapter = CallsTypeAdapter(requireContext(), LinkedList())
        //adapter.viewHolderClickListener = this
        val layoutManager = binding.rvPrescription.setupLinearView(adapter)
    }

    open fun bindData(result: MissedCallsResponseModel) {

        /*//success

        viewMode.missedCallResult.observe(viewLifecycleOwner) { data ->
            if (data != null && data.data.isNotEmpty()) {
                Log.d("TAG", "bindData: dataa : " + Gson().toJson(data))
                //adapter.updateItems(data.data.toMutableList())

            }
        }*/
    }

    abstract fun getEmptyDataMessage(): String
    abstract fun getEmptyDataIcon(): Int

   /* private fun loadMoreItems() {
        isLoading = true // Set loading to true before making API call
        adapter.addLoading() // Add loading item to show progress bar
        apiCallForGetListOfMissedCalls(this@NewMissedCallActivity, pageNumber)
    }*/

}