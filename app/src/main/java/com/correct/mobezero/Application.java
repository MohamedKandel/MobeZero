package com.correct.mobezero;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.RequestQueue;
import com.correct.mobezero.engine.SIPService;
import com.correct.mobezero.utils.AutoLogout;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class Application extends android.app.Application {

    private final ExecutorService backgroundExecutor;
    private final ExecutorService backgroundExecutorForUserActions;
    private final Handler handler;
    public SIPService service;
    private static Application instance;
    public String ssps = "Ts(Trjslas";
    public String customer = "symlexcall";
    private RequestQueue requestQueue;


    public Application() {
        instance = this;

        handler = new Handler();
        backgroundExecutor = createSingleThreadExecutor("Background executor service");
        backgroundExecutorForUserActions = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
    }

    public static Application getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void onCreate() {
        super.onCreate();

        //startService(SIPService.createIntent(this));
        Intent intent = new Intent(this, SIPService.class);
        intent.setAction(SIPService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        scheduleAutoLogout();
    }



    private void scheduleAutoLogout() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(AutoLogout.class)
                .setInitialDelay(12, TimeUnit.HOURS)
                .setConstraints(Constraints.NONE)
                .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .addTag("AutoLogout")
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork("AutoLogout", ExistingWorkPolicy.KEEP, request);
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            SIPService.LocalBinder binder = (SIPService.LocalBinder) serviceBinder;
            service = binder.getService();
            Log.e("onServiceConnected", "connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
            Log.e("onServiceDisconnected", "Disconnected");
        }
    };

    @NonNull
    private ExecutorService createSingleThreadExecutor(final String threadName) {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                Thread thread = new Thread(runnable, threadName);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });
    }


    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}
