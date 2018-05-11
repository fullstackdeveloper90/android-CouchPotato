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
import com.thorangs.couchpotato.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by balman on 11/15/17.
 */

public class WeeklyProgressView extends AProgressViewFragment {

    private static final String LABEL = "Weekly";
    private Calendar mCalendar;
    private static final int WEEK_DAYS_COUNT = 7;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarGraphLabel = LABEL;
        mChartRange = ChartRange.WEEKLY;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCalendar = Calendar.getInstance();

        new UpdateTheBarGraph().execute(DateUtils.getThisSundayTimeMills(Calendar.getInstance()));
        updateJumpText();
    }

    private void updateJumpText() {

        //SET CALENDAR TO SATURDAY
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(DateUtils.getThisSundayTimeMills(mCalendar));
        cal.add(Calendar.DAY_OF_WEEK, 6);

        String start_month = new SimpleDateFormat("MMM").format(mCalendar.getTime());
        int start_day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int start_year = mCalendar.get(Calendar.YEAR);

        int end_day = cal.get(Calendar.DAY_OF_MONTH);

        String rangeString = start_month + " " + DateUtils.getStringFromTwoDigitNumber(start_day) + "-" + DateUtils.getStringFromTwoDigitNumber(end_day) + ", " + start_year;
        mTvPreviousNextDate.setText(rangeString);

    }

    @Override
    protected void jumpGraph(int jumpFlag) {

        int JUMP_COUNT = 0;
        switch (jumpFlag) {
            case JUMP_NEXT:
                JUMP_COUNT = WEEK_DAYS_COUNT;
                break;

            case JUMP_PREV:
                JUMP_COUNT = -WEEK_DAYS_COUNT;
                break;
        }

        Calendar cal = Calendar.getInstance();
        if (jumpFlag == JUMP_NEXT &&
                (cal.get(Calendar.YEAR) == mCalendar.get(Calendar.YEAR) && cal.get(Calendar.WEEK_OF_YEAR) == mCalendar.get(Calendar.WEEK_OF_YEAR)
                        || cal.get(Calendar.YEAR) > mCalendar.get(Calendar.YEAR))) {
            return;

        }

        // set calendar to the sunday of this week
        mCalendar.setTimeInMillis(DateUtils.getThisSundayTimeMills(mCalendar));
        // jump calendar to previous or next sunday
        mCalendar.add(Calendar.DAY_OF_WEEK, JUMP_COUNT);

        updateJumpText();
        new UpdateTheBarGraph().execute(mCalendar.getTimeInMillis());


    }


    private class UpdateTheBarGraph extends AsyncTask<Long, Void, List<StepsLog>> {

        @Override
        protected List<StepsLog> doInBackground(Long... range) {
            long sunMills = range[0];
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(sunMills);
            cal.add(Calendar.DAY_OF_WEEK, 6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long satMills = cal.getTimeInMillis();

            // getting from
            StepLogFactory stepLogFactory = PedometerApp.Companion.stepLogFactory();
            Log.i("sun millis", "weekly record: sun millis =" + sunMills + " satmillis =" + satMills);
            List<StepsLog> stepsLogInWeek = stepLogFactory.getAllStepsNonReactive(sunMills, satMills);
            Logger.INSTANCE.log("weekly record: " + stepsLogInWeek.size());
            return stepsLogInWeek;
        }

        @Override
        protected void onPostExecute(List<StepsLog> stepsLogs) {
            super.onPostExecute(stepsLogs);

            //filling data with zeros
            HashMap<Integer, Integer> dataPoints = new HashMap<>();

            for (int i = 0; i < WEEK_DAYS_COUNT; i++) {
                dataPoints.put(i, 0);
            }

            int total_steps = 0;
            // filling the datapoints from the database
            for (StepsLog stepsLog : stepsLogs) {

                int dayOfWeek = DateUtils.getDayOfWeek(stepsLog.getDate());
                dataPoints.put(dayOfWeek, stepsLog.getSteps());
                total_steps += stepsLog.getSteps();
            }

            // creating the entries for chart
            ArrayList<BarEntry> entries = new ArrayList<>();
            for (Integer key : dataPoints.keySet()) {
                entries.add(new BarEntry(key, dataPoints.get(key)));
                Log.i("Weekly graph", "Weekly Graph x=" + key + " value=" + dataPoints.get(key));
            }
            setTheBarChart(entries);
            mTvTotalSteps.setText(mBarGraphLabel + " total: " + total_steps);
            mTvDailyAverageSteps.setText(total_steps / WEEK_DAYS_COUNT + "");
        }
    }
}

