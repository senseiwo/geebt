
package com.ees.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * The Class BTDevicesReceiver which is able to detect new Bluetooth devices in its own BT range.
 */
public class BTDevicesReceiver extends BroadcastReceiver {

    /** The Constant TAG. */
    private static final String TAG = BTDevicesReceiver.class.getSimpleName();

    /** The extra device address. */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    /** The m bt adapter. */
    private BluetoothAdapter mBtAdapter;
    
    /** The m paired devices list. */
    private List<BluetoothDevice> mPairedDevicesList = new ArrayList<BluetoothDevice>();
    
    /** The m new devices list. */
    private List<BluetoothDevice> mNewDevicesList = new ArrayList<BluetoothDevice>();
    

    /**
     * Instantiates a new bT devices receiver.
     */
    public BTDevicesReceiver() {
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    	Set<BluetoothDevice> boundedDevs = mBtAdapter.getBondedDevices();
    	for(BluetoothDevice item : boundedDevs) {
    		mPairedDevicesList.add(item);
    	}
    }
    
    /**
     * Gets the new devices.
     *
     * @return the new devices
     */
    public List<BluetoothDevice> getNewDevices() {
    	return mNewDevicesList;
    }
    
    /**
     * Gets the paired devices.
     *
     * @return the paired devices
     */
    public List<BluetoothDevice> getPairedDevices() {
    	return mPairedDevicesList;
    }

    /**
     * Do discovery.
     *
     * @return true, if successful
     */
    public boolean doDiscovery() {
    	Log.v(TAG, "Discovering neer area to find BT devices.");
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        return mBtAdapter.startDiscovery();
    }

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
	public void onReceive(Context arg0, Intent intent) {
		String action = intent.getAction();
		
		Log.v(TAG, "Broadcast received: " + action);
		
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Get the BluetoothDevice object from the Intent
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// If it's already paired, skip it, because it's been listed already
			Log.i(TAG, "Found device: " + device.getName() + ", state: " + device.getBondState());
			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				mNewDevicesList.add(device);
			}
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			Log.v(TAG, "No new devices has been found.");

		}
		
	}

}
