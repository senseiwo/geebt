package com.ees.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * The Class Channel.
 */
public class Channel extends Thread {
	
	/** The Constant TAG. */
	private static final String TAG = Channel.class.toString();
	
	/** The mm socket. */
	private final BluetoothSocket mmSocket;
    
    /** The mm in stream. */
    private final InputStream mmInStream;
    
    /** The mm out stream. */
    private final OutputStream mmOutStream;
    
    /** The manager. */
    private ConnectionManager mManager;
    
    /**
     * Instantiates a new channel.
     *
     * @param socket the socket
     * @param manager the manager
     */
    public Channel(BluetoothSocket socket, ConnectionManager manager) {
    	mmSocket = socket;
    	this.mManager  = manager;
    	InputStream tmpIn = null;
        OutputStream tmpOut = null;

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
    
    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                Log.i(TAG, "Received data with size = " + bytes + "bytes.");

                // Send the obtained bytes to the UI Activity
                //TODO: mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                Log.i(TAG, "Restart connection manager");
                mManager.start();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    /**
     * Cancel.
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
