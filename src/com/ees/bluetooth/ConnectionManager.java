package com.ees.bluetooth;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * The Class ConnectionManager.
 */
public class ConnectionManager {

	/** The Constant TAG. */
	private static final String TAG = ConnectionManager.class.getSimpleName();
	
	/** The Bluetooth adapter reference */
	private final BluetoothAdapter mAdapter;
	
	/** The connection state. */
	private AtomicInteger mState;

	/** The connected device name. */
	private String deviceName;
	
	/** The channel Bluetooth listener. */
	private ChannelInviteListener chnlBTListener;
	
	/** The opened Bluetooth channel. */
	private Channel chnlBT;
	
	/** The Bluetooth channel connector */
	private ChannelConnector chnlBTConnect;

	/** The Constant APP_UUID. */
	private static final UUID APP_UUID =
	        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	
	/**
	 * Instantiates a new connection manager.
	 */
	public ConnectionManager() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.i(TAG, "State: NONE");
		mState = new AtomicInteger(ConnectionState.NONE);
	}
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public int getState() {
		return mState.get();
	}
	
	public void setState(int state) {
		mState.set(state);
	}
	
	/**
	 * Gets the adapter.
	 *
	 * @return the adapter
	 */
	public BluetoothAdapter getAdapter() {
		return mAdapter;
	}
	
	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	public UUID getUUID() {
		return APP_UUID;
	}
	
	/**
	 * Starts channel listener. In this case the app will waits until request from client become.
	 */
	public synchronized void start() {
		Log.i(TAG, "Starting BT channel listener.");
		Log.i(TAG, "State: LISTENING");
		mState.set(ConnectionState.LISTENING);
		chnlBTListener = new ChannelInviteListener(this);
		chnlBTListener.start();
		Log.d(TAG, "Channel listener thread started.");
	}
	
	/**
	 * Establish connection. The method is called in case when application
	 * received request from client or successfully connected to other device.
	 * The method simply opens Bluetooth channel to operate, what means data
	 * exchanging.
	 * 
	 * @param socket
	 *            the socket
	 * @param device
	 *            the device
	 */
	public void establishConnection(BluetoothSocket socket, BluetoothDevice device) {
		Log.d(TAG, "Establishing connection to " + device.getName());
		deviceName = device.getName();
		
		/*
		if(chnlBTConnect!=null) {
			Log.i(TAG, "Stop running connection thread.");
			chnlBTConnect.cancel();
			chnlBTConnect=null;
		}
		
		if(chnlBT!=null) {
			Log.i(TAG, "Stop running BT channel thread.");
			chnlBT.cancel();
			chnlBT=null;
		}
		*/
		
		if(chnlBTListener != null) {
			Log.i(TAG, "Stop running BT channel listener thread.");
			chnlBTListener.cancel();
			chnlBTListener = null;
		}
		
		Log.i(TAG, "Create BT channel thread.");
		chnlBT = new Channel(socket, this);
		chnlBT.start();
		Log.i(TAG, "Channel main thread started.");
		
		Log.i(TAG, "State: CONNECTED");
		mState.set(ConnectionState.CONNECTED);		
	}
	
	/**
	 * Connect with given device.
	 *
	 * @param device the device
	 */
	public void connect(BluetoothDevice device) {
		Log.v(TAG, "Connect to: " + device.getName());
		if(getState() == ConnectionState.CONNECTING) {
			if(chnlBTConnect!=null) {
				Log.d(TAG, "Cancel connecting process.");
				chnlBTConnect.cancel();
				chnlBTConnect=null;
			}
		}
		
		if(chnlBT!=null) {
			Log.d(TAG, "Cancel Bluetooth channel");
			chnlBT.cancel();
			chnlBT=null;
		}
		
		Log.i(TAG, "Create connection to remote device.");
		chnlBTConnect = new ChannelConnector(device, this);
		chnlBTConnect.start();
		Log.i(TAG, "State: CONNECTING");
		mState.set(ConnectionState.CONNECTING);
	}
	
	/**
	 * Terminate all.
	 * Method terminates all running threads.
	 */
	public void terminateAll() {
		if(chnlBTListener != null) {
			chnlBTListener.cancel();
			chnlBTListener = null;
		}
		
		if(chnlBT != null) {
			chnlBT.cancel();
			chnlBT = null;
		}
		
		if(chnlBTConnect != null) {
			chnlBTConnect.cancel();
			chnlBTConnect=null;
		}
		Log.i(TAG, "State: NONE");
		mState.set(ConnectionState.NONE);
		deviceName=null;
	}
	
	/**
	 * Gets the connected device name.
	 *
	 * @return the conn dev name
	 */
	public String getConnDevName() {
		if(deviceName == null || (mState.get() != ConnectionState.CONNECTED)) {
			return "No connection";
		}
		return deviceName;
	}
	
	/**
	 * Write.
	 * 
	 *
	 * @param data the data
	 */
	public void write(byte[] data) {
		if(mState.get() != ConnectionState.CONNECTED) {
			Log.i(TAG, "Try to send data, but device is not connected.");
			return;
		}
		if(chnlBT!=null && chnlBT.isAlive()) {
			Log.i(TAG, "Bluetooth channel is alive, send data to paired and connected device.");
			chnlBT.write(data);
		} else {
			Log.e(TAG, "Could not send data, BT channel not established.");
		}
	}
	
}
