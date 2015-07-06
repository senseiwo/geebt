package com.ees.bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * The Class ChannelConnector.
 */
public class ChannelConnector extends Thread {
	
	/** The Constant TAG. */
	private static final String TAG = ChannelConnector.class.getSimpleName();
	
	/** The m manager. */
	private ConnectionManager mManager;
    
    /** The mm socket. */
    private final BluetoothSocket mmSocket;
    
    /** The mm device. */
    private final BluetoothDevice mmDevice;

    /**
     * Instantiates a new channel connector.
     *
     * @param device the device
     * @param manager the manager
     */
	public ChannelConnector(BluetoothDevice device, ConnectionManager manager) {
        mmDevice = device;
        mManager = manager;
        BluetoothSocket tmp = null;

        try {
        	tmp = device.createInsecureRfcommSocketToServiceRecord(mManager.getUUID());
        } catch (Exception e) {
            Log.e(TAG, "Socket create() failed", e);
        }
        mmSocket = tmp;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        Log.i(TAG, "Start connection to BT channel by given BT device. ");

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
        	Log.i(TAG, "Connect via given socket to dev: " + mmSocket.getRemoteDevice().getName());
            mmSocket.connect();
        } catch (IOException e) {
        	Log.e(TAG, "Cannot connect to device.", e);
        	mManager.setState(ConnectionState.NONE);
            // Close the socket
            try {
                mmSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }
            Log.i(TAG, "Restart connection manager");
            mManager.start();
            return;
        }

        // Reset the ConnectThread because we're done
        // TODO: move it to establishChannel...
        //synchronized (BluetoothChatService.this) {
        //    mConnectThread = null;
        //}

        // Establish BT connection, and make possible data exchanging.
        Log.i(TAG, "Establish connection.");
        mManager.establishConnection(mmSocket, mmDevice);
    }

    /**
     * Cancel.
     * Method cancels this thread by closing socket. 
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
