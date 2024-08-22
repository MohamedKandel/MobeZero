package com.correct.mobezero.engine;

import android.util.Log;

import com.correct.mobezero.helper.CallStatusListener;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

public class MyCall extends Call
{
    public VideoWindow vidWin;
    public VideoPreview vidPrev;
    private CallStatusListener listener;

    public void setCallListener(CallStatusListener listener) {
        this.listener = listener;
    }

    MyCall(MyAccount acc, int call_id)
    {
        super(acc, call_id);
        vidWin = null;
    }

    @Override
    public void onCallState(OnCallStateParam prm)
    {
        try {
            CallInfo ci = getInfo();
            Log.i("calling mohamed",ci.getStateText()+" 7");
            listener.onCallInfoChanged(ci.getStateText());
            if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                MyApp.ep.utilLogWrite(3, "MyCall", this.dump(true, ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Should not delete this call instance (self) in this context,
        // so the observer should manage this call instance deletion
        // out of this callback context.
        MyApp.observer.notifyCallState(this);
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm)
    {
        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    (cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                            cmi.getStatus() ==
                                    pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD))
            {
                // connect ports
                try {
                    AudioMedia am = getAudioMedia(i);
                    MyApp.ep.audDevManager().getCaptureDevMedia().
                            startTransmit(am);
                    am.startTransmit(MyApp.ep.audDevManager().
                            getPlaybackDevMedia());
                } catch (Exception e) {
                    System.out.println("Failed connecting media ports" +
                            e.getMessage());
                    continue;
                }
            } else if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                    cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID)
            {
                vidWin = new VideoWindow(cmi.getVideoIncomingWindowId());
                vidPrev = new VideoPreview(cmi.getVideoCapDev());
            }
        }

        MyApp.observer.notifyCallMediaState(this);
    }
}