package org.intelehealth.helpline.activities.callflow.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.intelehealth.helpline.R
import org.intelehealth.helpline.activities.callflow.fragments.IncomingCallRecordingsFragment
import org.intelehealth.helpline.activities.callflow.fragments.IncomingMissedCallsFragment
import org.intelehealth.helpline.activities.callflow.fragments.OutgoingCallRecordingsFragment
import org.intelehealth.helpline.activities.callflow.fragments.OutgoingMissedCallsFragment

class RecordingsCallTypeTabsPagerAdapter (
        private val context: Context,

        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val tabs = context.resources.getStringArray(R.array.call_types)
    private val fragments = arrayListOf<Fragment>(
            IncomingCallRecordingsFragment.newInstance(),
            OutgoingCallRecordingsFragment.newInstance(),
    )

    override fun createFragment(position: Int): Fragment {
        return fragments[position] as Fragment
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun getTitle(position: Int): String {
        return tabs[position]
    }
}