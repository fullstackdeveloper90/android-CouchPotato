package com.thorangs.couchpotato.ui.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.thorangs.couchpotato.R;
import com.thorangs.couchpotato.utils.Logger;

import java.util.ArrayList;

/**
 * Created by balman on 11/15/17.
 */

public abstract class AProgressViewFragment extends Fragment implements View.OnClickListener {

    protected BarChart mChart;
    protected Activity mActivity;
    protected TextView mTvDailyAverageSteps;
    protected TextView mTvPreviousNextDate;
    protected TextView mTvTotalSteps, totalStepsInThatColumn;
    protected String mBarGraphLabel = "Weekly";
    protected ImageView mIvPreviousRangeGraph, mIvNextRangeGraph;
    protected ChartRange mChartRange;

    // used while jumping in time for graph rendering
    protected static final int JUMP_NEXT = 1;
    protected static final int JUMP_PREV = 2;
    private String[] weeklyLabels = {"S", "M", "T", "W", "T", "F", "S"};
    private String[] yearlyLabels = {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_view, container, false);
        mChart = view.findViewById(R.id.progress_view_barchart);
        totalStepsInThatColumn = view.findViewById(R.id.totalStepsInThatColumn);
        mTvDailyAverageSteps = view.findViewById(R.id.tv_daily_average_steps);
        mTvTotalSteps = view.findViewById(R.id.tv_total_steps);
        mTvPreviousNextDate = view.findViewById(R.id.tv_next_previous_date);
        mIvPreviousRangeGraph = view.findViewById(R.id.iv_previous_range_graph);
        mIvNextRangeGraph = view.findViewById(R.id.iv_next_range_graph);
        mIvPreviousRangeGraph.setOnClickListener(this);
        mIvNextRangeGraph.setOnClickListener(this);

        configureBarChart();
        return view;
    }

    private void configureBarChart() {

        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setFitBars(true); // make the x-axis fit exactly all bars
        mChart.animateY(700);
        mChart.setTouchEnabled(true);

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Logger.INSTANCE.log("item touched "+ mChartRange.toString()+e.getY());
                String columnCount = "";
                if (mChartRange == ChartRange.YEARLY) {
                    columnCount += "Selected month : ";
                } else {
                    columnCount += "Selected day : ";
                }
                columnCount += (int) e.getY() + " steps";
                totalStepsInThatColumn.setText(columnCount);
                totalStepsInThatColumn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected() {
                Logger.INSTANCE.log("nothing selected ");

            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

    }

    protected void setTheBarChart(ArrayList<BarEntry> entries) {

        //Configuring the X-axis
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGradientStart));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        if ( mChartRange == ChartRange.WEEKLY) {
            xAxis.setValueFormatter((value, axis) -> {
                return weeklyLabels[(int) (value) % 7]; // xVal is a string array
            });
        } else if (mChartRange == ChartRange.YEARLY) {
            xAxis.setValueFormatter((value, axis) -> {
                return yearlyLabels[(int) (value) % 12]; // xVal is a string array
            });
        }


        //Configuring the Y-axis right
        YAxis right = mChart.getAxisRight();
        right.setTextSize(12f); // set the text size
        right.setDrawLabels(false); // no axis labels
        right.setDrawAxisLine(false); // no axis line
        right.setDrawGridLines(false); // no grid lines
        right.setDrawZeroLine(false); // draw a zero line

        //Configuring the Y-axis left
        YAxis left = mChart.getAxisLeft();
        left.setTextSize(12f); // set the text size
        left.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGradientStart));
        left.setDrawLabels(true); // no axis labels
        left.setDrawAxisLine(true); // no axis line
        left.setDrawGridLines(false); // no grid lines
        left.setDrawZeroLine(false); // draw a zero line

        BarDataSet barDataSet = new BarDataSet(entries, mBarGraphLabel);
        barDataSet.setColors(ContextCompat.getColor(mActivity, R.color.colorGradientStart));
        barDataSet.setDrawValues(false);

        BarData data = new BarData(barDataSet);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);

        mChart.setData(data);
        mChart.invalidate();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.iv_next_range_graph:
                jumpGraph(JUMP_NEXT);
                break;

            case R.id.iv_previous_range_graph:
                jumpGraph(JUMP_PREV);
                break;
        }
    }

    protected abstract void jumpGraph(int jumpFlag);

    public enum ChartRange {
        YEARLY, MONTHLY, WEEKLY
    }
}

