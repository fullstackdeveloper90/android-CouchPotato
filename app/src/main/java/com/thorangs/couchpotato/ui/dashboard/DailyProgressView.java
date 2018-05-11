package com.thorangs.couchpotato.ui.dashboard;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thorangs.couchpotato.PedometerApp;
import com.thorangs.couchpotato.R;
import com.thorangs.couchpotato.backend.RestClient;
import com.thorangs.couchpotato.backend.syncing.UserDataApiService;
import com.thorangs.couchpotato.database.StepLogFactory;
import com.thorangs.couchpotato.ui.common.ReactiveFragment;
import com.thorangs.couchpotato.utils.UtilKt;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class DailyProgressView extends ReactiveFragment {

    private TextView tvDailySteps;
    private TextView dailyStepsTarget;
    private ProgressBar progressBar;
    private TextView progressPercetage;
    private TextView dailyTimeLeft;
    private LinearLayout liveStepHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_progress, container, false);
        tvDailySteps = view.findViewById(R.id.tv_daily_steps);
        dailyStepsTarget = view.findViewById(R.id.tv_daily_steps_target);
        progressBar = view.findViewById(R.id.daily_progress_bar);
        progressPercetage = view.findViewById(R.id.progress_perecentage);
        dailyTimeLeft = view.findViewById(R.id.daily_time_left);
        liveStepHistory = view.findViewById(R.id.liveStepHistory);
        Observable.interval(1000L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(longTimed -> {
                    String printingString = "";
                    long remaining = UtilKt.getTomorrow() - System.currentTimeMillis();
                    int hour = (int) remaining / (1000 * 60 * 60);
                    int minute = ((int) remaining % (1000 * 60 * 60)) / (1000 * 60);
                    int second = ((int) remaining % (1000 * 60)) / (1000);
                    printingString += hour + " hr " + minute + " min " + second + " sec";
                    dailyTimeLeft.setText(printingString);
                });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        StepLogFactory stepLogFactory = PedometerApp.Companion.stepLogFactory();
        if (DashBoardActivity.Companion.subscriptionHasNotExpired()) {
            stepLogFactory.getSteps(UtilKt.getToday())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stepsLog -> {
                        int currentSteps = stepsLog.getSteps();
                        int targetSteps = stepsLog.getTargetSteps();
                        tvDailySteps.setText(currentSteps + "");
                        dailyStepsTarget.setText(targetSteps + "");

                        int progress_percentage;
                        if (targetSteps > 0)
                            progress_percentage = currentSteps * 100 / (targetSteps);
                        else progress_percentage = currentSteps * 100 / (targetSteps + 1);
                        progressBar.setProgress(progress_percentage);
                        progressPercetage.setText(progress_percentage + " %");
                    });
        } else {
            liveStepHistory.setVisibility(View.INVISIBLE);

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
