package org.intelehealth.helpline.activities.callflow

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.intelehealth.helpline.R
import org.intelehealth.helpline.activities.callflow.adapter.CallTypeTabsPagerAdapter
import org.intelehealth.helpline.activities.callflow.adapter.RecordingsCallTypeTabsPagerAdapter
import org.intelehealth.helpline.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.helpline.databinding.ActivityMissedCallsDetailsBinding
import org.intelehealth.helpline.shared.BaseActivity
import org.intelehealth.helpline.syncModule.SyncUtils
import org.intelehealth.helpline.utilities.NetworkUtils

class CallRecordingsNewActivity : BaseActivity(), NetworkUtils.InternetCheckUpdateInterface {
    private lateinit var binding: ActivityMissedCallsDetailsBinding
    private val syncAnimator: ObjectAnimator? = null
    private lateinit var networkUtils: NetworkUtils
    private lateinit var ivIsInternet: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissedCallsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // setupActionBar()
        networkUtils = NetworkUtils(this@CallRecordingsNewActivity, this)

        initUI()
        setupTabs()
    }

    private fun initUI() {
        val toolbar = findViewById<RelativeLayout>(R.id.toolbar_missed_call)
        val tvTitle = toolbar.findViewById<TextView>(R.id.tv_screen_title_common)
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common)
        val ivBackArrow = toolbar.findViewById<ImageView>(R.id.iv_back_arrow_common)

        tvTitle.text = getString(R.string.call_recordings)
        ivIsInternet.visibility = View.VISIBLE

        ivIsInternet.setOnClickListener {
            SyncUtils.syncNow(this@CallRecordingsNewActivity, ivIsInternet, syncAnimator)
        }

        ivBackArrow.setOnClickListener {
            val intent = Intent(this@CallRecordingsNewActivity, HomeScreenActivity_New::class.java)
            startActivity(intent)
        }
    }


    /*private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = resources.getString(R.string.title_visit_status)
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { _ -> finish() }

    }*/

    private fun setupTabs() {
        if (::binding.isInitialized) {
            val adapter = RecordingsCallTypeTabsPagerAdapter(this, supportFragmentManager, lifecycle)
            binding.viewPagerCallTypes.adapter = adapter
            TabLayoutMediator(
                    binding.tabsCallType,
                    binding.viewPagerCallTypes
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = adapter.getTitle(position)
            }.attach()
            binding.viewPagerCallTypes.isUserInputEnabled = false
        }
    }

    override fun updateUIForInternetAvailability(isInternetAvailable: Boolean) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_internet_available))
        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_no_internet))
        }
    }
    override fun onStop() {
        super.onStop()
        try {
            // Unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
    override fun onStart() {
        super.onStart()

        // Register receiver for internet check
        networkUtils.callBroadcastReceiver()
    }
}