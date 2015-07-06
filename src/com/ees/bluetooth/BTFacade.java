package com.ees.bluetooth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * The Class BTFacade. The class provides interface to manage BT device.
 */
public class BTFacade {

	/** The Constant TAG. */
	private static final String TAG = BTFacade.class.getSimpleName();

	/** The conn mgr. */
	private ConnectionManager connMgr = new ConnectionManager();
	
	/** The m activity. */
	private Activity mActivity;
	
	/** The m bluetooth adapter. */
	private BluetoothAdapter mBluetoothAdapter;
	
	/** The m receiver. */
	private BTDevicesReceiver mReceiver;
	
	/** The m new dev map. */
	private Map<String, BluetoothDevice> mNewDevMap = new HashMap<String,BluetoothDevice>();
	
	/** The m paired dev map. */
	private Map<String, BluetoothDevice> mPairedDevMap = new HashMap<String,BluetoothDevice>();
	
	/**
	 * Instantiates a new bT facade.
	 *
	 * @param activity the activity
	 */
	public BTFacade(Activity activity) {
		mActivity = activity;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mReceiver = new BTDevicesReceiver();
	}
	
	/**
	 * Update paired devices. Method update internal map of paired devices.
	 * Paired device mans that the device had been previously connected successfully
	 * with this device.
	 */
	private void updatePairedDevices() {
		mPairedDevMap.clear();
		List<BluetoothDevice> devList = mReceiver.getPairedDevices();
		Log.i(TAG, "Paired devices count: " + devList.size());
		for(BluetoothDevice item : devList) {
			mPairedDevMap.put(item.getName(), item);
			Log.i(TAG, "DevName: " + item.getName());
		}
	}
	
	/**
	 * Enable discoverable.
	 * Method initialize discoverable state, what means the device can detected by other devices
	 * which are scanning near BT range. Method operates on activity given from top tier. 
	 *
	 * @param timeout the timeout
	 */
	public void enableDiscoverable(int timeout) {
		Log.d(TAG, "Enable discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeout);
            mActivity.startActivity(discoverableIntent);
        }
	}

	
	/**
	 * Enable bluetooth.
	 * Method initializes turning on BT device.
	 *
	 * @return true, if successful
	 */
	public boolean enableBluetooth() {
		
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Bluetooth adapter not obtained. Null pointer.");
			return false;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Log.d(TAG, "BT adapter not enabled.");
			Intent enableBTIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mActivity.startActivityForResult(enableBTIntent, 1);
		}
		
		return true;
		
	}
	
	
	/**
	 * Gets the bT adapter state.
	 * 
	 * @return true if BT is enabled, false otherwise
	 */
	public boolean isBTAdapterEnabled() {
		if(mBluetoothAdapter == null) {
			Log.e(TAG, "Bluetooth adapter not obtained. Null pointer.");
			return false;
		}
		return mBluetoothAdapter.isEnabled();		
	}
		
	/**
	 * Start discovery.
	 * Method initializes scanning process. It looking for all BT devices in its own BT range.
	 *
	 * @return true, if successful
	 */
	public synchronized boolean startDiscovery() {
		// Clear current map devices
		mNewDevMap.clear();
		
		Context ctx = mActivity.getApplicationContext();
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ctx.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ctx.registerReceiver(mReceiver, filter);
        
		return mReceiver.doDiscovery();
	}
	
	/**
	 * Stop discovery.
	 * Method deinitialize scanning process.
	 */
	public void stopDiscovery() {
		if(isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
	}
	
	/**
	 * Gets the new devices.
	 * Method returns all new found devices, but not attached yes.
	 * These devices are not paired yet. 
	 *
	 * @return the new devices
	 */
	public synchronized Set<String> getNewDevices() {
		if(!isDiscovering()) {
			mNewDevMap.clear();
			for(BluetoothDevice item : mReceiver.getNewDevices()) {
				mNewDevMap.put(item.getName(), item);
			}
			return mNewDevMap.keySet();
		}
		// BT is discovering, try again later.
		return null;
	}
	
	/**
	 * Checks if is discovering.
	 * 	 *
	 * @return true, if is discovering otherwise false
	 */
	public boolean isDiscovering() {
		return mBluetoothAdapter.isDiscovering();
	}
	
	/**
	 * Gets the paired devices.
	 * Method returns all paired devices. Some of them could not be in range,
	 * so trying to make a connection would failed.
	 *
	 * @return the paired devices
	 */
	public Set<String> getPairedDevices() {
		updatePairedDevices();
		return mPairedDevMap.keySet();
	}
	
	/**
	 * Connect.
	 * Method connects to the device by given name. 
	 *
	 * @param name the name
	 * @return true, if successfully device has been found 
	 */
	public boolean connect(String name) {
		stopDiscovery();
		updatePairedDevices();
		BluetoothDevice device = mPairedDevMap.get(name);
		if(device != null) {
			Log.i(TAG, "Found in paired, lets connect.");
			connMgr.connect(device);
			return true;
		}
		
		device = mNewDevMap.get(name);
		if(device != null) {
			Log.i(TAG, "Found in new, lets connect.");
			connMgr.connect(device);
			return true;
		}
		
		Log.e(TAG, "Device not found.");
		
		return false;
	}
	
	/**
	 * Disconnect.
	 * Method drops all connections.
	 */
	public void disconnect() {
		stopDiscovery();
		connMgr.terminateAll();
	}
	
	/**
	 * Start server. Method initializes server mode. It is thread which wait on
	 * client request to connect. This is like a TCP client-server connection.
	 * Server waits on accept method, when request becomes it go further.
	 */
	public void startServer() {
		connMgr.start();
	}
	
	/**
	 * Send data.
	 * Method send data to paired and connected device.
	 * @param data the data
	 */
	public void sendData(String data) {
		if(connMgr!=null) {
			Log.d(TAG, "Sending data.");
			connMgr.write(data.getBytes());
		}
	}
	
	/**
	 * Gets the state.
	 * Method returns current state of BT connection.
	 * @return the state
	 */
	public int getState() {
		return connMgr.getState();
	}
	
	/**
	 * Gets the connected device name.
	 * 
	 * @return the connected dev name
	 */
	public String getConnectedDevName() {
		return connMgr.getConnDevName();
	}
}
