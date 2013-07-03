/**<ul>
 * <li>PremierTPInterParisMai</li>
 * <li>com.android2ee.formation.mai.mmxiii.premiertp</li>
 * <li>13 mai 2013</li>
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
package com.android2ee.android.tuto.communication.bluetooth.gui.model;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to:
 *        <ul>
 *        <li></li>
 *        </ul>
 */
public class BluetoothMessage {
	/**
	 * 
	 */
	private String name = "ME";
	/**
	 * 
	 */
	private String message;
	/**
	 * To know if the message comes from this device
	 */
	private boolean localDevice = true;

	/**
	 * 
	 */
	public BluetoothMessage(String name, String message, boolean localDevice) {
		this.message = message;
		this.name = name;
		this.localDevice = localDevice;
	}
	/**
	 * @return the name
	 */
	public final String getName() {
		if (null == name) {
			name = new String();
		}
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the message
	 */
	public final String getMessage() {
		if (null == message) {
			message = new String();
		}
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public final void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the localDevice
	 */
	public final boolean isLocalDevice() {

		return localDevice;
	}

	/**
	 * @param localDevice
	 *            the localDevice to set
	 */
	public final void setLocalDevice(boolean localDevice) {
		this.localDevice = localDevice;
	}

}
