package com.neev.cordova;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

public class Bluetooth extends CordovaPlugin {
	private static final String TAG = "Bluetooth";

	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_ISCONNECTED = 3;
	
	// actions
	private static final String CONNECT = "connect";
	private static final String START_SERVER = "startServer";
	private static final String GET_PAIRED_DEVICE = "getPairedDevice";
	private static final String READ = "read";
	private static final String WRITE = "write";
	private static final String IS_CONNECTED = "isconnected";

	private CallbackContext connectCallback;
	private CallbackContext dataAvailableCallback;
	private CallbackContext isConnectedCallback;
	
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothService bluetoothSerialService;
	
	StringBuffer buffer = new StringBuffer();
	private String delimiter;

	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		LOG.d(TAG, "action = " + action);

		if (bluetoothAdapter == null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		if (bluetoothSerialService == null) {
			bluetoothSerialService = new BluetoothService(bluetoothAdapter,mHandler);
		}

		if (action.equals(GET_PAIRED_DEVICE)) {
			getPairedDevice(callbackContext);
		}
		else if(action.equals(START_SERVER)){
			startSocketServer(callbackContext);
		}
		else if(action.equals(CONNECT)){
			connect(args,callbackContext);
		}
		else if(action.equals(READ)){
			read(callbackContext);
		}
		else if(action.equals(WRITE)){
			write(args,callbackContext);
		}
		else if(action.equals(IS_CONNECTED)){
			isConnected(callbackContext);
		}
		return true;
	}

	private void getPairedDevice(CallbackContext callbackContext)
			throws JSONException {
		JSONArray deviceList = new JSONArray();
		Set<BluetoothDevice> bondedDevices = bluetoothAdapter
				.getBondedDevices();

		for (BluetoothDevice device : bondedDevices) {
			JSONObject json = new JSONObject();
			json.put("name", device.getName());
			json.put("address", device.getAddress());
			json.put("id", device.getAddress());
			if (device.getBluetoothClass() != null) {
				json.put("class", device.getBluetoothClass().getDeviceClass());
			}
			deviceList.put(json);
		}
		callbackContext.success(deviceList);
	}

	private void startSocketServer(CallbackContext callbackContext)
			throws JSONException {
		bluetoothSerialService.startSocketServer();
		callbackContext.success();
	}

	private void connect(CordovaArgs args, CallbackContext callbackContext)
			throws JSONException {
		String macAddress = args.getString(0);
		LOG.d(TAG, "MAC ADDRESS ====  " + macAddress);
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
		CountDownLatch latch = new CountDownLatch(1);
		if (device != null) {
			connectCallback = callbackContext;
			bluetoothSerialService.connect(device, latch);
			try {
				latch.await();
				JSONObject json = new JSONObject();
				json.put("status", "connected");
				callbackContext.success(json);
			} catch (InterruptedException e) {
				callbackContext.error("Could not connect to " + macAddress);
				e.printStackTrace();
			}

		} else {
			callbackContext.error("Could not connect to " + macAddress);
		}
	}
	
	private void read(CallbackContext callbackContext)throws JSONException{
		dataAvailableCallback=callbackContext;
		bluetoothSerialService.connected();
		sendDataToSubscriber();
	}
	
	private void write(CordovaArgs args,CallbackContext callbackContext)throws JSONException{
		
		LOG.d(TAG, "write me aaa gyeeeeeeeeee------------------ ");
		String message = args.getString(0);
		bluetoothSerialService.write(message.getBytes());
		callbackContext.success();
	}
	
	private void isConnected(CallbackContext callbackContext)throws JSONException{
		isConnectedCallback=callbackContext;
	}
	
	private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                   buffer.append((String)msg.obj);

                   if (dataAvailableCallback != null) {
                       sendDataToSubscriber();
                   }
                   break;
                case MESSAGE_ISCONNECTED:
                	if(isConnectedCallback!=null){
                		connectedStatus((String)msg.obj);
                	}
            }
        }
   };
   
   private void connectedStatus(String data){
	   PluginResult result = new PluginResult(PluginResult.Status.OK, data);
       result.setKeepCallback(true);
       isConnectedCallback.sendPluginResult(result);
       LOG.d(TAG, "YES U R NOW CONNECTED------------------ ");

   }
   private void sendDataToSubscriber() {
       String data = readUntil();
       if (data != null && data.length() > 0) {
           PluginResult result = new PluginResult(PluginResult.Status.OK, data);
           result.setKeepCallback(true);
           dataAvailableCallback.sendPluginResult(result);

           sendDataToSubscriber();
       }
   }
   
   private String readUntil() {
       String data = "";
       int index = buffer.length();
       if (index > 0) {
           data = buffer.substring(0, index-1);
           buffer.delete(0, index-1);
       }
       return data;
   }
	
}