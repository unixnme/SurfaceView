package com.blogspot.unixnme.takeiteasy;

public class FrequencyCounter {

    public interface CallBack {
        void onFrequencyCalculated(double freq);
    }

    private FrequencyCounter instance;

    private CallBack callBack;
    private int period;

    private CountDownTimer countDownTimer;

    private int counter;

    public FrequencyCounter() {
        instance = this;
    }

    public void setFrequencyCallback(FrequencyCounter.CallBack callBack) {
        this.callBack = callBack;
    }

    public synchronized void start(int period) throws IllegalArgumentException {
        if (period <= 0)
            throw new IllegalArgumentException("period must be positive");

        counter = 0;
        this.period = period;
        countDownTimer = new CountDownTimer(period*1000, period*1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // do nothing
            }

            @Override
            public void onFinish() {

                synchronized (instance) {
                    double freq = (double) counter / (double) instance.period;
                    if (callBack != null)
                        callBack.onFrequencyCalculated(freq);

                    counter = 0;
                }

                if (countDownTimer != null)
                    countDownTimer.start();
            }
        };

        countDownTimer.start();
    }

    public synchronized void increment() {
        counter++;
    }

    public synchronized void stop() {
        if (countDownTimer != null)
            countDownTimer.cancel();
    }
}
