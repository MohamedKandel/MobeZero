package com.correct.mobezero.engine;

/**
 * Created by USER on 7/5/17.
 */

public class SipCallSession {

    public static class StatusCode {
        public static final int TRYING = 100;
        public static final int RINGING = 180;
        public static final int CALL_BEING_FORWARDED = 181;
        public static final int QUEUED = 182;
        public static final int PROGRESS = 183;
        public static final int OK = 200;
        public static final int ACCEPTED = 202;
        public static final int MULTIPLE_CHOICES = 300;
        public static final int MOVED_PERMANENTLY = 301;
        public static final int MOVED_TEMPORARILY = 302;
        public static final int USE_PROXY = 305;
        public static final int ALTERNATIVE_SERVICE = 380;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int PAYMENT_REQUIRED = 402;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int METHOD_NOT_ALLOWED = 405;
        public static final int NOT_ACCEPTABLE = 406;
        public static final int INTERVAL_TOO_BRIEF = 423;
        public static final int BUSY_HERE = 486;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int DECLINE = 603;
        public static final int NETWORK_UNRECHABLE = 503;
        public static final int PROXY_AUTHENTICATION_REQURE = 407;
        //added by riaz for request time out
        public static final int REQUEST_TIME_OUT = 408;

    }
}
