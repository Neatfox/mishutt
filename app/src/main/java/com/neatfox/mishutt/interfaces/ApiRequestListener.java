package com.neatfox.mishutt.interfaces;

import org.json.JSONException;

public interface ApiRequestListener {
    void onSuccess(String result, String type) throws JSONException;
    void onFailure(int responseCode, String responseMessage);

}
