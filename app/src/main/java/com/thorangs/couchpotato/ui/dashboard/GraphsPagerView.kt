package com.thorangs.couchpotato.ui.dashboard

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thorangs.couchpotato.R

class GraphsPagerView : Fragment() {

    private lateinit var tabPager: TabLayout
    private lateinit var graphsViewPager: ViewPager
    private lateinit var bottomTab: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_graphs_pager, container, false)
        rootView.apply {
            tabPager = findViewById(R.id.tab_pager)
            graphsViewPager = findViewById(R.id.pager_performance_graph)
            bottomTab = findViewById(R.id.swipe_tab_bottom)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tabPager.setupWithViewPager(graphsViewPager)
        graphsViewPager.adapter = GraphsPagerAdapter(this)
        graphsViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomTab.visibility = View.VISIBLE
                    else -> bottomTab.visibility = View.GONE
                }
            }
        })
    }

    class GraphsPagerAdapter(fragment: Fragment?) : FragmentStatePagerAdapter(fragment?.fragmentManager) {

        private val titles: Array<String> = fragment?.context?.resources?.getStringArray(R.array.titles)!!

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> DailyProgressView()
                1 -> WeeklyProgressView()
                2 -> MonthlyProgressView()
                else -> YearlyProgressView()
            }
        }

        override fun getCount(): Int = 4

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}
