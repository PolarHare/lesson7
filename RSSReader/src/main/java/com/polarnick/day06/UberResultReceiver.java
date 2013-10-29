package com.polarnick.day06;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Date: 23.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class UberResultReceiver extends ResultReceiver {

    private Receiver receiver;

    public UberResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }

    public interface Receiver {

        public static final String IS_FRESHER_KEY = "fresh";
        public static final String FEED_KEY = "feed";

        public void onReceiveResult(int resultCode, Bundle resultData);

    }

}
