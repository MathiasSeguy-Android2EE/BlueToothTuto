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
package com.android2ee.android.tuto.communication.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.android2ee.android.tuto.communication.bluetooth.gui.CommunicationActivity;
import com.android2ee.android.tuto.communication.bluetooth.gui.DiscoverDevicesActivity;
import com.android2ee.android.tuto.communication.bluetooth.service.CommunicationService;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to the global state of the application
 *        Here it focused on blueToothSocket and on start/stoip the communication service
 */
public class MyApplication extends Application {
	/******************************************************************************************/
	/** Attributes **************************************************************************/
	/******************************************************************************************/
	/**
	 * The unique UUID of the application to etablish a connection
	 */
	public static final UUID MY_UUID = UUID.fromString("fa87c0e0-dfef-11de-8a39-0800200c9a66");
	/**
	 * The unique bluetooth server name use for connection
	 */
	public static final String MY_BLUETOOTH_SERVER_NAME = "blueToothServerName11021974";
	/**
	 * The BlueTooth socket
	 */
	private BluetoothSocket bluetoothSocket;
	/**
	 * The input stream from the socket
	 */
	private InputStream inputSocketStream = null;
	/**
	 * The output stream from the socket
	 */
	private OutputStream outputSocketStream = null;
	/**
	 * The selected device to connect with
	 */
	private BluetoothDevice remoteDevice = null;
	/**
	 * The name of this device
	 */
	private String thisDeviceName = null;
	/**
	 * The intent that launches and stops the commmunication service
	 */
	private Intent communicationServiceIntent;
	/**
	 * The communication activity
	 * We keep a pointer on it when it alives to be able to kill it if the bluetooth connection is
	 * reset
	 */
	private CommunicationActivity comActivity = null;
	/******************************************************************************************/
	/** Access Every Where **************************************************************************/
	/******************************************************************************************/
	/**
	 * instance of this
	 */
	private static MyApplication instance;

	/**
	 * @return The instance of the application
	 */
	public static MyApplication getInstance() {
		return instance;
	}

	/******************************************************************************************/
	/** Managing LifeCycle **************************************************************************/
	/******************************************************************************************/

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("MApplication:onCreate", "Application is create");
		instance = this;
		communicationServiceIntent = new Intent(getApplicationContext(), CommunicationService.class);
	}

	/******************************************************************************************/
	/** Try managing socket in/out put here **************************************************************************/
	/******************************************************************************************/
	/**
	 * @param bluetoothSocket
	 *            the bluetoothSocket to set
	 */
	public final void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
		Log.e("MyaApplication",
				"BlueToothSocket set :" + bluetoothSocket + " is connected " + bluetoothSocket.isConnected());
		// First store it
		syncBluetoothSocketModifier(true, bluetoothSocket);
		// Then get the input and output streams
		try {
			syncInputSocketStream(true, syncBluetoothSocketModifier(false, null).getInputStream());
			outputSocketStream = this.bluetoothSocket.getOutputStream();
		} catch (IOException e) {
			Log.e("MApplication:onCreate", "setBluetoothSocket", e);
		}
		// then launch the communication service
		startService();
	}

	/**
	 * Ensure that modifications of the Socket is ThreadSafe
	 * 
	 * @param set
	 *            if true then this function act like a setter
	 * @param bluetoothSocket
	 *            the socket to set
	 * @return bluetoothSocket
	 */
	private synchronized BluetoothSocket syncBluetoothSocketModifier(boolean set, BluetoothSocket bluetoothSocket) {
		if (set) {
			this.bluetoothSocket = bluetoothSocket;
		}
		return this.bluetoothSocket;
	}

	/**
	 * Ensure that modification of the inputSocketStream is ThreadSafe
	 * 
	 * @param set
	 *            if true then this function act like a setter
	 * @param inputSocketStream
	 *            the inputSocketStream to set
	 * @return inputSocketStream
	 */
	private synchronized InputStream syncInputSocketStream(boolean set, InputStream inputSocketStream) {
		if (set) {
			this.inputSocketStream = inputSocketStream;
		}
		return this.inputSocketStream;
	}

	/**
	 * Ensure that modification of the outputSocketStream is ThreadSafe
	 * 
	 * @param set
	 *            if true then this function act like a setter
	 * @param outputSocketStream
	 *            the outputSocketStream to set
	 * @return outputSocketStream
	 */
	private synchronized OutputStream syncOutputSocketStream(boolean set, OutputStream outputSocketStream) {
		if (set) {
			this.outputSocketStream = outputSocketStream;
		}
		return this.outputSocketStream;
	}

	/**
	 * @return the inputSocketStream
	 */
	public final InputStream getInputSocketStream() {
		return syncInputSocketStream(false, null);
	}

	/**
	 * @return the outputSocketStream
	 */
	public final OutputStream getOutputSocketStream() {
		return syncOutputSocketStream(false, null);
	}

	/**
	 * @return the bluetoothSocket
	 */
	public final BluetoothSocket getBluetoothSocket() {
		return syncBluetoothSocketModifier(false, null);
	}

	
	/**
	 * ReleaseBlueToothSocket:
	 * Stop CommunicationService
	 * Close socket
	 * Finish communication Activity
	 * Set Socket and its io to null
	 */
	public final void resetBluetoothSocket() {
		Log.e("MyaApplication", "BlueToothSocket resetBluetoothSocket : was " + bluetoothSocket);
		stopService();
		if (getBluetoothSocket() != null) {
			try {
				// then close
				getBluetoothSocket().close();
			} catch (IOException e) {
				Log.e("CommunicationActivity", "resetBluetoothSocket ", e);
			}
		}
		if (comActivity != null) {
			// kill the communication activity
			comActivity.finish();
			comActivity = null;
		}
		syncBluetoothSocketModifier(true, null);
		syncInputSocketStream(true, null);
		syncOutputSocketStream(true, null);
	}

	/******************************************************************************************/
	/** Manage service **************************************************************************/
	/******************************************************************************************/
	private boolean serviceOn=false;
	public boolean isServiceOn() {
		return serviceOn;
	}
	/**
	 * Start the service
	 */
	public void startService() {
		startService(communicationServiceIntent);
		serviceOn=true;
	}

	/**
	 * Stop the service
	 */
	public void stopService() {
		stopService(communicationServiceIntent);
		serviceOn=false;
	}

	/**
	 * Kill the service and the socket
	 * same as calling resetBluetoothSocket
	 */
	public void killService() {
		resetBluetoothSocket();
	}
	
	/******************************************************************************************/
	/** Managing the activity flow **************************************************************************/
	/******************************************************************************************/

	/**
	 * To know if we just leave the communicationActivity
	 * or if we launch the application to fall in the discoveryActivity directly
	 */
	private boolean justQuitingComActivity=false;
	
	/**
	 * To know if we just leave the communicationActivity
	 * @return true if just quitting com activity
	 */
	public boolean isJustQuitingComActivity() {
		return justQuitingComActivity;
	}
	/**
	 * To know if we just leave the communicationActivity
	 * @return true if just quitting com activity
	 */
	public void isJustQuitingComActivity(boolean isJustQuitingComActivity) {
		justQuitingComActivity=isJustQuitingComActivity;
	}
	/******************************************************************************************/
	/** Get/set **************************************************************************/
	/******************************************************************************************/
	/**
	 * @return the remoteDevice
	 */
	public final BluetoothDevice getRemoteDevice() {

		return remoteDevice;
	}

	/**
	 * @param remoteDevice
	 *            the remoteDevice to set
	 */
	public final void setRemoteDevice(BluetoothDevice selectedDevice) {
		this.remoteDevice = selectedDevice;
	}

	/**
	 * @return the thisDeviceName
	 */
	public final String getThisDeviceName() {
		return thisDeviceName;
	}

	/**
	 * @param thisDeviceName
	 *            the thisDeviceName to set
	 */
	public final void setThisDeviceName(String thisDeviceName) {
		this.thisDeviceName = thisDeviceName;
	}
	
	/**
	 * @param comActivity
	 *            the comActivity to set
	 */
	public final void setComActivity(CommunicationActivity comActivity) {
		this.comActivity = comActivity;
		if(comActivity==null) {
			justQuitingComActivity=true;
		}
	}

}
