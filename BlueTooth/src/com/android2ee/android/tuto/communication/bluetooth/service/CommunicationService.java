/**<ul>
 * <li>BlueTooth</li>
 * <li>com.android2ee.android.tuto.communication.bluetooth</li>
 * <li>2 juil. 2013</li>
 * 
 * <li>======================================================</li>
 *
 * <li>Projet : Mathias Seguy Project</li>
 * <li>Produit par MSE.</li>
 *
 /**
 * <ul>
 * Android Tutorial, An <strong>Android2EE</strong>'s project.</br> 
 * Produced by <strong>Dr. Mathias SEGUY</strong>.</br>
 * Delivered by <strong>http://android2ee.com/</strong></br>
 *  Belongs to <strong>Mathias Seguy</strong></br>
 ****************************************************************************************************************</br>
 * This code is free for any usage except training and can't be distribute.</br>
 * The distribution is reserved to the site <strong>http://android2ee.com</strong>.</br>
 * The intelectual property belongs to <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * 
 * *****************************************************************************************************************</br>
 *  Ce code est libre de toute utilisation mais n'est pas distribuable.</br>
 *  Sa distribution est reservée au site <strong>http://android2ee.com</strong>.</br> 
 *  Sa propriété intellectuelle appartient à <strong>Mathias Seguy</strong>.</br>
 *  <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * *****************************************************************************************************************</br>
 */
package com.android2ee.android.tuto.communication.bluetooth.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android2ee.android.tuto.communication.bluetooth.MyApplication;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to be a service that handles the bluetooth communication exchanges between
 *        the devices.
 */
public class CommunicationService extends Service {
	/**
	 * Boolean to create the communication thread only once
	 */
	boolean alreadyManagedCommunication = false;

	/**
	 * The thread that manage the data exchange between the both device
	 */
	ConnectedThread connectedThread;
	/******************************************************************************************/
	/** Connection with the activity **************************************************************************/
	/******************************************************************************************/
	/**
	 * The action of the intent sent from this service to the bound activity
	 */
	public static final String BLUETOOTH_COMMUNICATION_INTENT_ACTION = "com.android2ee.android.tuto.communication.bluetooth.service.intent.communication.thread";
	/**
	 * The Intent sent from this service to the bound activity
	 */
	private final Intent bluetoothCommunicationIntent = new Intent(BLUETOOTH_COMMUNICATION_INTENT_ACTION);
	/**
	 * The name of the property of the intent that carry the string message
	 */
	public static final String BLUETOOTH_MESSAGE = "bluetoothMessage";

	/******************************************************************************************/
	/** Constructors & LifeCycle **************************************************************************/
	/******************************************************************************************/
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.e("CommunicationService", "onDestroy");
		// close the socket
		connectedThread.cancel();
		// Kill the thread
			connectedThread.interrupt();
		
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("CommunicationService", "onStartCommand");
		initialize();
		return super.onStartCommand(intent, flags, startId);
	}

	/******************************************************************************************/
	/** Initialization **************************************************************************/
	/******************************************************************************************/
	/**
	 * Before the service runs really
	 */
	private void initialize() {
		// Launch the Thread and all the communication stuff
		manageConnectedSocket();
	}

	/******************************************************************************************/
	/** Binder **************************************************************************/
	/******************************************************************************************/
	/**
	 * The binder use to bind this and the activity
	 */
	private final Binder binder = new LocalBinder();

	/**
	 * @author Mathias Seguy (Android2EE)
	 * @goals
	 *        This class aims to define a local binder
	 */
	public class LocalBinder extends Binder {
		/**
		 * @return the instance of the service
		 */
		public CommunicationService getService() {
			return CommunicationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		initialize();
		return binder;
	}

	/******************************************************************************************/
	/** Public methods **************************************************************************/
	/******************************************************************************************/
	/**
	 * Send a Message through Bluetooth
	 * 
	 * @param Message
	 */
	public void sendMessage(String Message) {
		connectedThread.write(Message.getBytes());
	}

	/*************************************************************************************/
	/** Managing data exchange **************************************************************/
	/*************************************************************************************/

	/**
	 * * This method is called when a Bluetooth socket is created and the attribute bluetoothSocket
	 * is instanciated
	 */
	private void manageConnectedSocket() {
		if (!alreadyManagedCommunication) {
			// launch the connected thread
			connectedThread = new ConnectedThread();
			connectedThread.start();
			alreadyManagedCommunication = true;
		}

	}

	/**
	 * The thread that manages communication
	 */
	private class ConnectedThread extends Thread {
		/* (non-Javadoc) * @see java.lang.Thread#run() */
		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()
			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = MyApplication.getInstance().getInputSocketStream().read(buffer);
					byte[] readBuf = (byte[]) buffer;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(readBuf, 0, bytes);
					// Add that string to the intent that will be launched
					bluetoothCommunicationIntent.putExtra(BLUETOOTH_MESSAGE, readMessage);
					// launch the intent it will be catched by the bound activity
					sendBroadcast(bluetoothCommunicationIntent);
				} catch (IOException e) {
					Log.e("CommunicationActivity", "ConnectedThread run", e);
					break;
				}
			}
		}

		/* Call this from the main Activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				// Write bytes in the output socket
				MyApplication.getInstance().getOutputSocketStream().write(bytes);
			} catch (IOException e) {
				Log.e("CommunicationActivity", "ConnectedThread write", e);
			}
		}

		/* Call this from the main Activity to shutdown the connection */
		public void cancel() {
			try {
				// Close the socket
				if (MyApplication.getInstance().getBluetoothSocket() != null) {
					MyApplication.getInstance().getBluetoothSocket().close();
				}
			} catch (IOException e) {
				Log.e("CommunicationActivity", "ConnectedThread cancel", e);
			}
		}
	}

}
