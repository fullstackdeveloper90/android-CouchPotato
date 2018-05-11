package com.thorangs.couchpotato.ui.dashboard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.thorangs.couchpotato.PedometerApp;
import com.thorangs.couchpotato.database.StepLogFactory;
import com.thorangs.couchpotato.database.StepsLog;
import com.thorangs.couchpotato.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by balman on 11/15/17.
 */

public class MonthlyProgressView extends AProgressViewFragment {

    private static final String LABEL = "Monthly";
    private static final int MONTH_INCR_DCR_VAL = 1;
    private Calendar mCalendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarGraphLabel = LABEL;
        mChartRange = ChartRange.MONTHLY;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCalendar = Calendar.getInstance();
        new UpdateTheBarGraph().execute(DateUtils.getStartOfTheMonthMillis());
        updateJumpText();
    }

    @Override
    protected void jumpGraph(int jumpFlag) {

        int JUMP_COUNT = 0;
        switch (jumpFlag) {
            case JUMP_NEXT:
                JUMP_COUNT = MONTH_INCR_DCR_VAL;
                break;

            case JUMP_PREV:
                JUMP_COUNT = -MONTH_INCR_DCR_VAL;
                break;
        }

        Calendar cal = Calendar.getInstance();
        if (jumpFlag == JUMP_NEXT &&
                (cal.get(Calendar.YEAR) == mCalendar.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == mCalendar.get(Calendar.MONTH)
                        || cal.get(Calendar.YEAR) > mCalendar.get(Calendar.YEAR))) {
            return;

        }

        mCalendar.add(Calendar.MONTH, JUMP_COUNT);
        mCalendar.set(Calendar.DATE, mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);


        long startMills = mCalendar.getTimeInMillis();
        new UpdateTheBarGraph().execute(startMills);

        updateJumpText();

    }

    private void updateJumpText() {

        String month = new SimpleDateFormat("MMM").format(mCalendar.getTime());
        int year = mCalendar.get(Calendar.YEAR);

        String rangeString = month + ", " + year;
        mTvPreviousNextDate.setText(rangeString);
    }

    private class UpdateTheBarGraph extends AsyncTask<Long, Void, List<StepsLog>> {

        @Override
        protected List<StepsLog> doInBackground(Long... range) {

            long startMillis = range[0];

            //get endMillis
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startMillis);
            cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long endMills = cal.getTimeInMillis();

            // getting from
            StepLogFactory stepLogFactory = PedometerApp.Companion.stepLogFactory();
            return stepLogFactory.getAllStepsNonReactive(startMillis, endMills);
        }

        @Override
        protected void onPostExecute(List<StepsLog> stepsLogs) {
            super.onPostExecute(stepsLogs);

            //filling data with zeros
            HashMap<Integer, Integer> dataPoints = new HashMap<>();

            byte totalDayInMonth = (byte) mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < totalDayInMonth; i++) {
                dataPoints.put(i, 0);
            }

            // filling the datapoints from the database
            int total_steps = 0;
            for (StepsLog stepsLog : stepsLogs) {

                int dayOfMonth = DateUtils.getDayOfMonth(stepsLog.getDate());
                dataPoints.put(dayOfMonth, stepsLog.getSteps());

                total_steps += total_steps + stepsLog.getSteps();
            }

            // creating the entries for chart
            ArrayList<BarEntry> entries = new ArrayList<>();
            for (Integer key : dataPoints.keySet()) {
                entries.add(new BarEntry(key, dataPoints.get(key)));
                Log.i("Monthly graph", "Monthly Graph x="+key+" value="+dataPoints.get(key));
            }

            mTvTotalSteps.setText(mBarGraphLabel + " total: " + total_steps);
            mTvDailyAverageSteps.setText(total_steps / totalDayInMonth + "");
            setTheBarChart(entries);
        }

    }
}
