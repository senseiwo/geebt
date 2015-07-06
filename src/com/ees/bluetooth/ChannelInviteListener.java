package com.ees.bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * 
 */
public class ChannelInviteListener extends Thread{
	
	/** The Constant TAG. */
	private static final String TAG = ChannelInviteListener.class.toString();
	
	/** The mm server socket. */
	private final BluetoothServerSocket mmServerSocket;
	
	/** The manager. */
	private ConnectionManager mManager;
	
	/**
	 * Instantiates a new channel invite listener.
	 *
	 * @param manager the manager
	 */
	public ChannelInviteListener(ConnectionManager manager) {
		this.mManager = manager;
        BluetoothServerSocket tmp = null;

        try {
             tmp = manager.getAdapter().listenUsingInsecureRfcommWithServiceRecord("BTAPPUnity3d", manager.getUUID());
        } catch (IOException e) {
            Log.e(TAG, "Socket listen() failed", e);
        }
        mmServerSocket = tmp;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        Log.d(TAG, "Start scanning BT devices, " + this);

        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected
        while (mManager.getState() != ConnectionState.CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket accept() failed", e);
                break;
            }

            // If a connection was accepted
            if (socket != null) {
            	// TODO: synch is not necessary here, atomic is used
                synchronized (mManager) { 
                    switch (mManager.getState()) {
                    case ConnectionState.LISTENING:
                    case ConnectionState.CONNECTING:
                        // Situation normal. Start the connected thread.
                    	mManager.establishConnection(socket, socket.getRemoteDevice());
                    	Log.v(TAG, "Scanned result: " + socket.getRemoteDevice().getName());
                        break;
                    case ConnectionState.NONE:
                    	Log.e(TAG, "Not initialized ConnectionManager.");
                    case ConnectionState.CONNECTED:
                        // Either not ready or already connected. Terminate new socket.
                    	Log.e(TAG, "Already connected. Socket termination.");
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Could not close unwanted socket", e);
                        }
                        break;
                    }
                }
            }
        }
        
        Log.i(TAG, "END Scanning");

    }

    /**
     * Cancel.
     */
    public void cancel() {
        Log.d(TAG, "Socket " + mmServerSocket.toString() + "cancel " + this);
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Socket Type" + mmServerSocket.toString() + "close() of server failed", e);
        }
    }
}
