package com.correct.mobezero.engine;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.correct.mobezero.helper.CallStatusListener;
import com.correct.mobezero.helper.KtHelper;

import com.correct.mobezero.R;
import com.correct.mobezero.helper.RegisterListener;
import com.correct.mobezero.room.CallLog;
import com.correct.mobezero.room.CallsDB;
import com.correct.mobezero.ui.CallActivity;
import com.correct.mobezero.utils.ContactUtils;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.pjsip_status_code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SipProfile {

    private static SipProfile instance = null;
    private MyCall call = null;
    private MyAccount account = null;
    private String lastOnlineStatus = "";
    private int lastStatusCode = 0;
    private boolean isInComingCall = false;
    private String balance = "";
    private int coin = 0;
    private String callingNumber = "";
    private String CallInitiatedTime = "";
    private boolean isCallOngoing = false;
    private boolean userNotDisconnectedYet = true;

    private String minInSec = "";
    private CallsDB callsDB;
    private RegisterListener listener;
    private CallStatusListener callListener;

    public void setCallListener(CallStatusListener callListener) {
        this.callListener = callListener;
    }

    public void removeCallListener() {
        this.callListener = null;
    }

    public void setRegisterListener(RegisterListener listener) {
        this.listener = listener;
    }

    public void removeRegisterListener() {
        this.listener = null;
    }

    public String getMinInSec() {
        return minInSec;
    }

    public void setMinInSec(String minInSec) {
        this.minInSec = minInSec;
    }

    public boolean isCallOngoing() {
        return isCallOngoing;
    }

    public void setCallOngoing(boolean callOngoing) {
        isCallOngoing = callOngoing;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public MyAccount getAccount() {
        return this.account;
    }

    public void setAccount(MyAccount account) {
        this.account = account;
    }

    public void setCall(MyCall call) {
        this.call = call;
    }

    public MyCall getCall() {
        return call;
    }

    public int getCoin() {
        return coin;
    }

    public static SipProfile getInstance() {
        if (instance == null) {
            instance = new SipProfile();
        }

        return instance;
    }

    public String getLastOnlineStatus() {
        return lastOnlineStatus;
    }

    public void setLastOnlineStatus(String lastOnlineStatus) {
        this.lastOnlineStatus = lastOnlineStatus;
    }

    public void makeCall(final String number, final Activity activity) {

        callsDB = CallsDB.Companion.getDBInstance(activity);
        try {

            if (call != null) {
                Toast.makeText(activity, "Call exist", Toast.LENGTH_LONG).show();
                CallOpParam prm = new CallOpParam();
                prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
                try {
                    if (isMyServiceRunning(activity, CallService.class)) {
                        activity.stopService(CallService.Companion.createIntent(activity));
                    }
                    call.hangup(prm);
                } catch (Exception e) {
                    System.out.println(e);
                }
                call = null;
                return;
            }
            if (account == null) {
                Toast.makeText(activity, "Invalid account", Toast.LENGTH_LONG).show();
                return;
            }

            if (account.getInfo().getRegStatus().swigValue() == SipCallSession.StatusCode.OK) {


                isInComingCall = false;
                setCallingNumber(number);
                makeCallWithOptions(number);
                if (!isMyServiceRunning(activity, CallService.class)) {
                    activity.startService(CallService.Companion.createIntent(activity));
                }
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
        /*Permissions.check(activity, new String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECORD_AUDIO},null, null,
                new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        //do your task.
                        try {

                            if (call != null){
                                Toast.makeText(activity, "Call exist",Toast.LENGTH_LONG).show();
                                CallOpParam prm = new CallOpParam();
                                prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
                                try {
                                    if (isMyServiceRunning(activity,CallService.class)) {
                                        activity.stopService(CallService.Companion.createIntent(activity));
                                    }
                                    call.hangup(prm);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                call = null;
                                return;
                            }
                            if(account == null){
                                Toast.makeText(activity, "Invalid account",Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (account.getInfo().getRegStatus().swigValue() == SipCallSession.StatusCode.OK) {


                                isInComingCall = false;
                                setCallingNumber(number);
                                makeCallWithOptions(number);
                                if (!isMyServiceRunning(activity,CallService.class)){
                                    activity.startService(CallService.Companion.createIntent(activity));
                                }
                            }
                        }catch(Exception e){

                            e.printStackTrace();

                        }
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                    }
                });
*/


    }

    public boolean isMyServiceRunning(Context activity, Class<?> serviceClass) {
        android.app.ActivityManager manager = (android.app.ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void makeCallWithOptions(String number) {

        if (account == null) return;
        call = null;
        MyCall mcall = new MyCall(account, -1);
        mcall.setCallListener(callListener);
        CallOpParam prm = new CallOpParam(true);

        if (timerStarted) {
            timerHandler.removeCallbacks(runnable);
            timerStarted = false;
        }
        time_counter = 0;
        CallDuration = "00:00:00";

        try {
            mcall.makeCall(number, prm);
            CallInitiatedTime = getTimeDate();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        call = mcall;

    }

    public void showIncomingCall(MyCall call, Service activity) {

        try {
            if (this.call != null) return;
            this.call = call;
            this.isInComingCall = true;
            CallInitiatedTime = getTimeDate();
            setCallingNumber(call.getInfo().getRemoteUri());
            Intent intent = new Intent(activity, CallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCallActivity(Activity activity) {
        Intent intent = new Intent(activity, CallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public boolean isInComingCall() {
        return isInComingCall;
    }

    public void setInComingCall(boolean inComingCall) {
        isInComingCall = inComingCall;
    }

    public String getStatusText(Activity activity) {

        String msg_str = "";
        Account account = SipProfile.getInstance().getAccount();
        try {
            int status = account.getInfo().getRegStatus().swigValue();

            if (status == SipCallSession.StatusCode.OK) {
                msg_str += activity.getResources().getString(R.string.acct_registered);
            } else if (status == SipCallSession.StatusCode.TRYING) {
                msg_str += activity.getResources().getString(R.string.acct_registering);
            } else if (status == SipCallSession.StatusCode.FORBIDDEN) {
                msg_str += activity.getResources().getString(R.string.user_not_found);
            } else if (status == SipCallSession.StatusCode.UNAUTHORIZED) {
                msg_str += activity.getResources().getString(R.string.wrong_password);
            } else if (status == SipCallSession.StatusCode.REQUEST_TIME_OUT) {
                msg_str += activity.getResources().getString(R.string.request_time_out);
            } else if (status == SipCallSession.StatusCode.NOT_FOUND) {
                msg_str += activity.getResources().getString(R.string.user_not_found);
            } else if (status == SipCallSession.StatusCode.NETWORK_UNRECHABLE) {
                msg_str += activity.getResources().getString(R.string.check_internet);
            } else {
                msg_str += status + " " + account.getInfo().getRegStatusText();
            }

            listener.onAccountRegisterListener(msg_str);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg_str;
    }

    public int getStatusColor(Activity activity) {

        int color = activity.getResources().getColor(R.color.red_400);
        Account account = SipProfile.getInstance().getAccount();


        try {

            if (account == null) return color;

            if (account.getInfo() == null) return color;

            if (account.getInfo().getRegStatus() == null) return color;

            if (account.getInfo().getRegStatus().swigValue() == 0) return color;


            int status = account.getInfo().getRegStatus().swigValue();
            if (status == SipCallSession.StatusCode.OK) {
                color = activity.getResources().getColor(R.color.green_400);
            } else if (status == SipCallSession.StatusCode.FORBIDDEN) {
                color = activity.getResources().getColor(R.color.red_400);
            } else if (status == SipCallSession.StatusCode.REQUEST_TIME_OUT) {
                color = activity.getResources().getColor(R.color.red_400);
            } else if (status == SipCallSession.StatusCode.NOT_FOUND) {
                color = activity.getResources().getColor(R.color.red_400);
            } else if (status == SipCallSession.StatusCode.NETWORK_UNRECHABLE) {
                color = activity.getResources().getColor(R.color.red_400);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return color;
    }

    public void setBalance(String bal) {
        this.balance = bal;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }


    public String getBalance() {
        return balance;
    }

    public void holdCall() {
        CallOpParam prm = new CallOpParam(true);
        if (call == null) return;
        try {
            call.setHold(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unHoldCall() {
        CallOpParam prm = new CallOpParam(true);
        if (call == null) return;
        prm.getOpt().setFlag(1);
        try {
            call.reinvite(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDTMF(String key) {

        if (this.call == null) return;

        try {
            this.call.dialDtmf(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    callDurationListener durationListener;
    private int time_counter = 0;
    private String CallDuration = "00:00:00";
    boolean timerStarted = false;
    private Handler timerHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            timerStarted = true;
            time_counter++;
            CallDuration = ConvertSecondToHHMMString(time_counter);
            if (durationListener != null) {
                durationListener.UpdateDuration(CallDuration);
            }
            if (timerHandler != null) {

                String timeinsec = minInSec.trim();
                int interval = 1000;
                if (!TextUtils.isEmpty(timeinsec)) {
                    try {
                        interval = Math.round((Float.parseFloat(timeinsec) / 60) * 1000);
                        Log.e("register inverval", "" + interval);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                timerHandler.postDelayed(runnable, interval);
            } else {
                Log.v("register", "timerHandler is null");
            }
        }
    };

    public void setDurationListener(callDurationListener listener) {
        this.durationListener = listener;
    }

    public void cancelListener() {
        durationListener = null;
    }

    public interface callDurationListener {
        void UpdateDuration(String duration);
    }

    void startTimer() {
        timerHandler.postDelayed(runnable, 1000);
    }

    void stopTimer(Context context) {
        if (timerStarted) {
            timerHandler.removeCallbacks(runnable);
            timerStarted = false;
        }
        Log.e("Sipprofile", "stopTimer");
        time_counter = 0;
        setCallOngoing(false);
        updateCallLog(context);
        CallDuration = "00:00:00";
        setCallingNumber("");
    }

    private String ConvertSecondToHHMMString(int secondtTime) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(secondtTime * 1000L));
        return time;

    }


    private String formatPhoneNumber(String phonenumber) {

        Pattern sipUriSpliter = Pattern
                .compile("^(?:\")?([^<\"]*)(?:\")?[ ]*(?:<)?sip(?:s)?:([^@]*)@[^>]*(?:>)?");
        Matcher m = sipUriSpliter.matcher(phonenumber);
        if (m.matches()) {
            if (!TextUtils.isEmpty(m.group(2))) {
                phonenumber = m.group(2);

            } else if (!TextUtils.isEmpty(m.group(1))) {

                phonenumber = m.group(1);// change by zem
            }
        }

        return phonenumber;

    }

    private void updateCallLog(Context context) {

        String phonenumber = formatPhoneNumber(getCallingNumber());
        String Duration = CallDuration;
        CallDuration = "00:00:00";

        Date date = new Date();
        SimpleDateFormat sdf_date = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
        String current_date = sdf_date.format(date);
        String current_time = sdf_time.format(date);


        int id = KtHelper.INSTANCE.getNextIDBlocking(callsDB.dao());
        Log.e("Next ID mohamed", id + "");
        String name = ContactUtils.Companion.getName(context, phonenumber);//"Mohamed Hossam";//ContactUtils.Companion.getName(phonenumber);
        CallLog call = new CallLog(id, name, phonenumber, current_date, current_time, "", "");


        if (phonenumber.startsWith("000")) {
            phonenumber = phonenumber.substring(3);
        }
        call.setNumber(phonenumber);
        call.setDate(current_date);
        call.setTime(current_time);
        call.setDuration(Duration);

        if (SipProfile.getInstance().isInComingCall()) {
//
            if (Duration.equalsIgnoreCase("00:00:00")) {
//                newLog.setCallType("Missed call");
                call.setCall_type("Missed call");
            } else {
                call.setCall_type("Incoming call");
//                newLog.setCallType("Incoming call");
            }
        } else {
            call.setCall_type("Dialed call");
//            newLog.setCallType("Dialed call");
        }
        KtHelper.INSTANCE.insert(callsDB.dao(), call);
//
//        db.add(newLog);
    }

    public String getTimeDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        Calendar cal = Calendar.getInstance();
        String date_time = dateFormat.format(cal.getTime());
        return date_time;
    }

    public boolean isUserNotDisconnectedYet() {
        return userNotDisconnectedYet;
    }

    public void setUserNotDisconnectedYet(boolean userNotDisconnectedYet) {
        this.userNotDisconnectedYet = userNotDisconnectedYet;
    }

    public int convertToCoin(String balance) {

        try {
            float bal = Float.parseFloat(balance);
            float coin = bal * 1000;

            return Math.round(coin);

        } catch (Exception e) {

            return 0;
        }
    }

    public String convertTobalance(String amount) {
        try {
            Log.e("amount--", amount);
            double coin = Double.parseDouble(amount);
            Log.e("coin--", "" + coin);
            double balance = coin / 1000;
            Log.e("balance--", "" + balance);
            return String.valueOf(balance);

        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
    }
}
