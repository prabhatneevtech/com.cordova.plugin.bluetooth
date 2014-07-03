package com.neev.cordova; 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


class BluetoothService{
	
	private static final String TAG = "log";
	private static final String NAME = "BluetoothChatSecure";
	private static final UUID MY_UUID =
	        UUID.fromString("23F18142-B389-4772-93BD-52BDBB2C03E9");
	
	//fa87c0d0-afac-11de-8a39-0800200c9a66
	private ConnectThread mConnectThread;
	private AcceptThread mAcceptThread;
	private ConnectedThread mConnectedThread;
	private BluetoothAdapter mBluetoothAdapter;
	private Context mContext;
	private  Handler mHandler;
	private  BluetoothSocket mmSocket;
	private BluetoothServerSocket mmServerSocket;
	private OutputStream mmOutStream;
	//private BluetoothServerSocket mmServerSocket;
	
	public BluetoothService(BluetoothAdapter bluetoothAdapter, Handler handler){
		mBluetoothAdapter=bluetoothAdapter;
		mHandler=handler;
		//mContext=context;
	}
	
	public void connect(BluetoothDevice device,CountDownLatch latch){
		mConnectThread = new ConnectThread(device,latch);
        mConnectThread.start();
	}
	
	public void startSocketServer(){
		mAcceptThread=new AcceptThread();
		mAcceptThread.start();
	}
	
	public void connected(){
		Log.e(TAG, "About to connected thread-------");
		mConnectedThread=new ConnectedThread(mmSocket);
		mConnectedThread.start();
		
	}
	
	public void write(byte[] buffer){
		try {
			Log.e(TAG, "Write karne ko hain--------");
			mmOutStream=mmSocket.getOutputStream();
            mmOutStream.write(buffer);
            Log.e(TAG, "Write kar DIYE-----");
            
            // Share the sent message back to the UI Activity
            //mHandler.obtainMessage(BluetoothSerial.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();

        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
	}
	
	
	
	
	
	private class AcceptThread extends Thread {
	    //private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        Log.e("LOG", "--------------------Server Started");
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	                mmSocket=socket;
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	            	mHandler.obtainMessage(Bluetooth.MESSAGE_ISCONNECTED, "yes").sendToTarget();
	            	//Toast.makeText(mContext, "Request Accepted", Toast.LENGTH_SHORT).show();
	            	Log.e("LOG", "------------------------------------Request Accepted");
	                try {
						mmServerSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private class ConnectThread extends Thread {
	    //private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    private CountDownLatch mlatch;
	    public ConnectThread(BluetoothDevice device,CountDownLatch latch) {

	        BluetoothSocket tmp = null;
	        mmDevice = device;
	        mlatch=latch;
	        try {
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            mmSocket.connect();
	            if(mmServerSocket!=null){
	            	mmServerSocket.close();
	            }
	            mlatch.countDown();
	        } catch (IOException connectException) {
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	    }
	}
	
	
	
	
	
	
	
	private class ConnectedThread extends Thread {
        //private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread:========================= ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            if(socket==null){
            	Log.d(TAG, "scoket null hai:========================= ");
            }
            else{
            	Log.d(TAG, "scoket null nai---hai:========================= ");
            }
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                	 Log.e(TAG, "waiting for message=======");
                    bytes = mmInStream.read(buffer);
                    Log.e(TAG, "Read kar liye bhai=======");
                    String data = new String(buffer, 0, bytes);

                    // Send the new data String to the UI Activity
                    mHandler.obtainMessage(Bluetooth.MESSAGE_READ, data).sendToTarget();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    //connectionLost();
                    // Start the service over to restart listening mode
                    //BluetoothSerialService.this.start();
                    break;
                }
            }
        }
	}
	
	
	
	
}