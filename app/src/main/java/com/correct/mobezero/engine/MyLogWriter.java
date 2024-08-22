package com.correct.mobezero.engine;

import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;

/**
 * Created by USER on 7/5/17.
 */

public class MyLogWriter extends LogWriter
{
    @Override
    public void write(LogEntry entry)
    {
        try {
            //System.out.println(entry.getMsg());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}