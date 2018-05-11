package com.thorangs.couchpotato.ui.dashboard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.thorangs.couchpotato.PedometerApp;
import com.thorangs.couchpotato.database.StepLogFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by balman on 11/15/17.
 */

public class YearlyProgressView extends AProgressViewFragment {

    private static final String LABEL = "Yearly";
    private static final int YEAR_INCR_DCR_VAL = 1;
    private static final int MONTH_COUNT = 12;
    private static final int TOTAL_DAYS_IN_YEAR = 365;
    Calendar mCalendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarGraphLabel = LABEL;
        mChartRange = ChartRange.YEARLY;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCalendar = Calendar.getInstance();
        new UpdateTheGraph().execute();
        updateJumpText();
    }

    private void updateJumpText() {
        mTvPreviousNextDate.setText(mCalendar.get(Calendar.YEAR) + "");
    }

    @Override
    protected void jumpGraph(int jumpFlag) {

        int JUMP_COUNT = 0;
        switch (jumpFlag) {
            case JUMP_NEXT:
                JUMP_COUNT = YEAR_INCR_DCR_VAL;
                break;

            case JUMP_PREV:
                JUMP_COUNT = -YEAR_INCR_DCR_VAL;
                break;
        }
        Calendar cal = Calendar.getInstance();
        if (jumpFlag == JUMP_NEXT &&cal.get(Calendar.YEAR) <= mCalendar.get(Calendar.YEAR)) {
            return;
        }

        mCalendar.add(Calendar.YEAR, JUMP_COUNT);
        updateJumpText();
        new UpdateTheGraph().execute();
    }


    private class UpdateTheGraph extends AsyncTask<Void, Void, Map<Integer, Integer>> {

        @Override
        protected Map<Integer, Integer> doInBackground(Void... voids) {

            StepLogFactory stepLogFactory = PedometerApp.Companion.stepLogFactory();
            return stepLogFactory.stepsInYearByMonth(mCalendar.get(Calendar.YEAR));
        }

        @Override
        protected void onPostExecute(Map<Integer, Integer> yearlyStepsHash) {
            super.onPostExecute(yearlyStepsHash);

            // Initializing Entires with empty set
            ArrayList<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < MONTH_COUNT; i++) {
                int stepsInMonth = 0;
                try {
                    stepsInMonth = yearlyStepsHash.get(i);
                } catch (Exception ignored) {
                }
                entries.add(new BarEntry(i, stepsInMonth));
                Log.i("Yearly graph", "Yearly Graph x="+i+" value="+stepsInMonth);
            }

            int total_steps = 0;
            for (int key : yearlyStepsHash.keySet()) {

                total_steps += yearlyStepsHash.get(key);
                //entries.add(new BarEntry(key+1, yearlyStepsHash.get(key)));
            }

            mTvTotalSteps.setText(mBarGraphLabel + " total: " + total_steps);
            mTvDailyAverageSteps.setText(total_steps / TOTAL_DAYS_IN_YEAR + "");
            setTheBarChart(entries);
        }
    }
}
