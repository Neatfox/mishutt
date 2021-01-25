package com.neatfox.mishutt.interfaces;

public interface OTPReceivedInterface {

    void onOtpReceived(String otp);
    void onOtpTimeout();
}
