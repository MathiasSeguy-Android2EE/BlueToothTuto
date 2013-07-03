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
import android.util.Log;
import android.widget.Toast;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to:
 *        <ul>
 *        <li></li>
 *        </ul>
 */
public class MyApplication extends Application {
	/******************************************************************************************/
	/** Attributes **************************************************************************/
	/******************************************************************************************/
	/** * The unique UUID of the application to etablish a connection */
	public static final UUID MY_UUID = UUID.fromString("fa87c0e0-dfef-11de-8a39-0800200c9a66");
	/** * The unique bluetooth server name use for connection */
	public static final String MY_BLUETOOTH_SERVER_NAME = "blueToothServerName11021974";
	/** * The BlueTooth socket */
	private  BluetoothSocket bluetoothSocket;
	/**
	 * The selected device to connect with
	 */
	private BluetoothDevice remoteDevice = null;
	/**
	 * The selected device to connect with
	 */
	private String thisDeviceName = null;
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
	}

	/******************************************************************************************/
	/** Get/set list of found devices **************************************************************************/
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
	 * @return the bluetoothSocket
	 */
	public final BluetoothSocket getBluetoothSocket() {

		return bluetoothSocket;
	}

	/**
	 * @param bluetoothSocket
	 *            the bluetoothSocket to set
	 */
	public final void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
		Log.e("MyaApplication","BlueToothSocket set :" + bluetoothSocket + " is connected " + bluetoothSocket.isConnected());
		this.bluetoothSocket = bluetoothSocket;
		try {
			inputSocketStream = this.bluetoothSocket.getInputStream();
			outputSocketStream = this.bluetoothSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param bluetoothSocket
	 *            the bluetoothSocket to set
	 */
	public final void resetBluetoothSocket() {
		Log.e("MyaApplication","BlueToothSocket resetBluetoothSocket : was " + bluetoothSocket );
		this.bluetoothSocket = null;
			inputSocketStream = null;
			outputSocketStream = null;
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

	
	/******************************************************************************************/
	/** Try managing socket in/out put here **************************************************************************/
	/******************************************************************************************/
	/** * The input stream from the socket */
	private  InputStream inputSocketStream=null;
	/** * The output stream from the socket */
	private  OutputStream outputSocketStream=null;

	/**
	 * @return the inputSocketStream
	 */
	public final InputStream getInputSocketStream() {
		Log.e("MyaApplication","getInputSocketStream BlueToothSocket set :" + bluetoothSocket + " is connected " + bluetoothSocket.isConnected());
	return inputSocketStream;}
	

	/**
	 * @return the outputSocketStream
	 */
	public final OutputStream getOutputSocketStream() {
		Log.e("MyaApplication","getOutputSocketStream BlueToothSocket set :" + bluetoothSocket + " is connected " + bluetoothSocket.isConnected());
	return outputSocketStream;}
	
	
	
}
