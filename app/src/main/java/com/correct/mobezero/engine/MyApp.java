package com.correct.mobezero.engine;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.ContainerNode;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.IpChangeParam;
import org.pjsip.pjsua2.JsonDocument;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pjmedia_srtp_use;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.io.File;
import java.util.ArrayList;

public class MyApp {
    static {
        try{
            System.loadLibrary("openh264");
            // Ticket #1937: libyuv is now included as static lib
            //System.loadLibrary("yuv");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("UnsatisfiedLinkError: " + e.getMessage());
            System.out.println("This could be safely ignored if you " +
                    "don't need video.");
        }
        System.loadLibrary("pjsua2");
        System.out.println("Library loaded");
    }

    public static Endpoint ep = new Endpoint();
    public static MyAppObserver observer;
    public ArrayList<MyAccount> accList = new ArrayList<MyAccount>();

    private ArrayList<MyAccountConfig> accCfgs =
            new ArrayList<MyAccountConfig>();
    private EpConfig epConfig = new EpConfig();
    private TransportConfig sipTpConfig = new TransportConfig();
    private String appDir;

    /* Maintain reference to log writer to avoid premature cleanup by GC */
    private MyLogWriter logWriter;

    private final String configName = "pjsua2.json";
    private final int SIP_PORT  = 6000;
    private final int LOG_LEVEL = 4;
    int and_listenport = 0;
    int min = 34000;
    int max = 50000;
    public void init(MyAppObserver obs, String app_dir)
    {
        init(obs, app_dir, false);
    }

    public void init(MyAppObserver obs, String app_dir,
                     boolean own_worker_thread)
    {
        observer = obs;
        appDir = app_dir;

        /* Create endpoint */
        try {
            ep.libCreate();
        } catch (Exception e) {
            return;
        }


        /* Load config */
        String configPath = appDir + "/" + configName;
        File f = new File(configPath);
        if (f.exists()) {
            loadConfig(configPath);
        } else {
            /* Set 'default' values */
            sipTpConfig.setPort(SIP_PORT);
        }

        /* Override log level setting */
        epConfig.getLogConfig().setLevel(LOG_LEVEL);
        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

        /* Set log config. */
        LogConfig log_cfg = epConfig.getLogConfig();
        logWriter = new MyLogWriter();
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor() &
                ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));

        /* Write log to file (just uncomment whenever needed) */
        //String log_path = android.os.Environment.getExternalStorageDirectory().toString();
        //log_cfg.setFilename(log_path + "/pjsip.log");

        /* Set ua config. */
        UaConfig ua_cfg = epConfig.getUaConfig();
        ua_cfg.setUserAgent("Pjsua2 Android " + ep.libVersion().getFull());


        /*UserDefaults zem = new UserDefaults(Application.getInstance());
        if(zem.getPTime().length() != 0){
            if (Build.VERSION.SDK_INT>19){
                epConfig.getMedConfig().setPtime(Integer.parseInt(zem.getPTime()));
                //epConfig.getMedConfig().setAudioFramePtime(Integer.parseInt(zem.getPTime()));
            }else{
                epConfig.getMedConfig().setPtime(Integer.parseInt(zem.getPTime()));
            }
        }*/


        /* No worker thread */
        if (own_worker_thread) {
            ua_cfg.setThreadCnt(0);
            ua_cfg.setMainThreadOnly(true);
        }

        /* Init endpoint */
        try {
            ep.libInit(epConfig);
        } catch (Exception e) {
            return;
        }

        try {
            and_listenport = (int) (Math.random() * (max - min + 1) ) + min;
            sipTpConfig.setPort(and_listenport);

            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                    sipTpConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            and_listenport = (int) (Math.random() * (max - min + 1) ) + min;
            sipTpConfig.setPort(and_listenport);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
                    sipTpConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            and_listenport = (int) (Math.random() * (max - min + 1) ) + min;
            sipTpConfig.setPort(and_listenport);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,
                    sipTpConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ep.codecSetPriority("G729/8000", (short) 255);
            ep.codecSetPriority("iLBC/8000", (short) 0);
            ep.codecSetPriority("GSM/8000", (short) 0);
            ep.codecSetPriority("PCMU/8000", (short) 0);
            ep.codecSetPriority("PCMA/8000", (short) 0);
            ep.codecSetPriority("speex/16000", (short) 0);
            ep.codecSetPriority("speex/8000", (short) 0);
            ep.codecSetPriority("speex/32000", (short) 0);
            //ep.codecSetPriority("telephone-event/8000", (short) 0);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        /* Create accounts. */
        for (int i = 0; i < accCfgs.size(); i++) {
            MyAccountConfig my_cfg = accCfgs.get(i);

            /* Customize account config */
            my_cfg.accCfg.getNatConfig().setIceEnabled(true);
            my_cfg.accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            my_cfg.accCfg.getVideoConfig().setAutoShowIncoming(true);

            /* Enable SRTP optional mode and without requiring SIP TLS transport */
            my_cfg.accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_DISABLED);
            my_cfg.accCfg.getMediaConfig().setSrtpSecureSignaling(0);

            MyAccount acc = addAcc(my_cfg.accCfg);
            if (acc == null)
                continue;

            /* Add Buddies */
            for (int j = 0; j < my_cfg.buddyCfgs.size(); j++) {
                BuddyConfig bud_cfg = my_cfg.buddyCfgs.get(j);
                acc.addBuddy(bud_cfg);
            }
        }

        /* Start. */
        try {
            ep.libStart();
        } catch (Exception e) {
            return;
        }
    }

    public MyAccount addAcc(AccountConfig cfg)
    {
        MyAccount acc = new MyAccount(cfg);
        try {
            acc.create(cfg);
            System.out.println("Account created");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Account not created");
            acc = null;
            return null;
        }

        accList.add(acc);
        return acc;
    }

    public void delAcc(MyAccount acc)
    {
        accList.remove(acc);
    }

    private void loadConfig(String filename)
    {
        JsonDocument json = new JsonDocument();

        try {
            /* Load file */
            json.loadFile(filename);
            ContainerNode root = json.getRootContainer();

            /* Read endpoint config */
            epConfig.readObject(root);

            /* Read transport config */
            ContainerNode tp_node = root.readContainer("SipTransport");
            sipTpConfig.readObject(tp_node);

            /* Read account configs */
            accCfgs.clear();
            ContainerNode accs_node = root.readArray("accounts");
            while (accs_node.hasUnread()) {
                MyAccountConfig acc_cfg = new MyAccountConfig();
                acc_cfg.readObject(accs_node);
                accCfgs.add(acc_cfg);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        /* Force delete json now, as I found that Java somehow destroys it
         * after lib has been destroyed and from non-registered thread.
         */
        json.delete();
    }

    private void buildAccConfigs()
    {
        /* Sync accCfgs from accList */
        accCfgs.clear();
        for (int i = 0; i < accList.size(); i++) {
            MyAccount acc = accList.get(i);
            MyAccountConfig my_acc_cfg = new MyAccountConfig();
            my_acc_cfg.accCfg = acc.getCfg();

            my_acc_cfg.buddyCfgs.clear();
            for (int j = 0; j < acc.getBuddyList().size(); j++) {
                MyBuddy bud = acc.getBuddyList().get(j);
                my_acc_cfg.buddyCfgs.add(bud.cfg);
            }

            accCfgs.add(my_acc_cfg);
        }
    }

    private void saveConfig(String filename)
    {
        JsonDocument json = new JsonDocument();

        try {
            /* Write endpoint config */
            json.writeObject(epConfig);

            /* Write transport config */
            ContainerNode tp_node = json.writeNewContainer("SipTransport");
            sipTpConfig.writeObject(tp_node);

            /* Write account configs */
            buildAccConfigs();
            ContainerNode accs_node = json.writeNewArray("accounts");
            for (int i = 0; i < accCfgs.size(); i++) {
                accCfgs.get(i).writeObject(accs_node);
            }

            /* Save file */
            json.saveFile(filename);
        } catch (Exception e) {}

        /* Force delete json now, as I found that Java somehow destroys it
         * after lib has been destroyed and from non-registered thread.
         */
        json.delete();
    }

    public void handleNetworkChange()
    {
        try{
            System.out.println("Network change detected");
            IpChangeParam changeParam = new IpChangeParam();
            ep.handleIpChange(changeParam);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void deinit()
    {
        String configPath = appDir + "/" + configName;
        saveConfig(configPath);

        /* Try force GC to avoid late destroy of PJ objects as they should be
         * deleted before lib is destroyed.
         */
        Runtime.getRuntime().gc();

        /* Shutdown pjsua. Note that Endpoint destructor will also invoke
         * libDestroy(), so this will be a test of double libDestroy().
         */
        try {
            ep.libDestroy();
        } catch (Exception e) {}

        /* Force delete Endpoint here, to avoid deletion from a non-
         * registered thread (by GC?).
         */
        ep.delete();
        ep = null;
    }
}
