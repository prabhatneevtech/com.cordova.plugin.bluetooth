package com.neev.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONException;

class Bluetooth extends CordovaPlugin{
	private static final String TAG = "Bluetooth";
	
	//actions
	private static final String CONNECT = "connect";
	
	
	private CallbackContext connectCallback;
	
	@Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		LOG.d(TAG, "action = " + action);
		return true;
	}
}