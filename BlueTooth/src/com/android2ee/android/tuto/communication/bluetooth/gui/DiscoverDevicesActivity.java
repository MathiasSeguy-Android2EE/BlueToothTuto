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
package com.android2ee.android.tuto.communication.bluetooth.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android2ee.android.tuto.communication.bluetooth.MyApplication;
import com.android2ee.android.tuto.communication.bluetooth.R;
import com.android2ee.android.tuto.communication.bluetooth.gui.arrayadapter.BluetoothAdapterCallBack;
import com.android2ee.android.tuto.communication.bluetooth.gui.arrayadapter.BluetoothDevicesAdapter;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to display a view to the user that let him:
 *        <ul>
 *        <li>=>Discover other devices around him</li>
 *        <li>=>Be discoverable for others devices</li>
 *        <li>=>List the device already paired</li>
 *        <li>=>List the device discovered</li>
 *        <li>And then choose the device to connect with</li>
 *        </ul>
 */
public class DiscoverDevicesActivity extends Activity implements BluetoothAdapterCallBack {
	/**
	 * The Bluetooth Adapter
	 */
	private BluetoothAdapter bluetoothAdapter;
	/**
	 * The blueToothServer Socket
	 */
	private BluetoothServerSocket bluetoothServerSocket = null;
	/**
	 * The unique Request code send with the Intent BluetoothAdapter.ACTION_DISCOVERABLE when
	 * starting the activty to discover bluetooth
	 */
	private final int REQUEST_DISCOVERABLE = 13121974;
	/**
	 * The boolean to know is the device is discoverable
	 */
	boolean isDiscoverable = false;
	/**
	 * The duration of the discoverable mode
	 */
	int discoverableTimeRemaining = 0;
	/**
	 * To log what happens (simple GUI)
	 */
	TextView txvLog;
	/**
	 * The string to log
	 */
	StringBuilder strLog = new StringBuilder("No device found yet...\r\n");
	/**
	 * TextView Discoverable state
	 */
	TextView txvDiscoverableState;
	/**
	 * TextView Discovery state
	 */
	TextView txvDiscoveryState;
	/**
	 * The button to set the Connectable mode on
	 * BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
	 */
	Button btnBeDiscoverable;
	/**
	 * The button to launch a discovery action
	 * Just call bluetoothAdapter.startDiscovery();
	 */
	Button btnStartDiscovery;
	/**
	 * The listView that displays device
	 */
	ListView lstDevices;
	/**
	 * The arrayAdpter that is bound to the ListView
	 */
	BluetoothDevicesAdapter arrayAdapter;
	/**
	 * The items to display
	 */
	private List<BluetoothDevice> foundDevices;
	/******************************************************************************************/
	/** Broadcast Receiver **************************************************************************/
	/******************************************************************************************/

	/******************************************************************************************/
	/** Listening for new BlueTooth Devices finding **/
	/** ---------------------------------------------------------- **/
	/**
	 * The boolean to know if we are listening for new device discovery
	 * To unregister or register the newBluetoothFoundBroadCastReceiver
	 */
	private boolean listeningNewDevices = false;
	/**
	 * Define the brodcast receiver that listen for new Bluetooth Devices Found
	 * It's activated by the call of bluetoothAdapter.startDiscovery();
	 */
	private BroadcastReceiver newBluetoothFoundBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// retireve the device name
			String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
			// retrieve the device itself
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// store them
			if (!foundDevices.contains(device)) {
				foundDevices.add(device);
				arrayAdapter.notifyDataSetChanged();
			}
			strLog.append("New Device found " + device.getName() + ":" + device.getAddress() + "\r\n");
			txvLog.setText(strLog.toString());
			// for fun tell it to the user
			Log.e("DiscoverBlueToothDevicesService", "New Device found " + device.getName() + ":" + device.getAddress());
		}
	};

	/******************************************************************************************/
	/** Listening for BlueTooth Discoverability state change **/
	/** ---------------------------------------------------------------- **/
	/**
	 * Define the brodcast receiver that listen for ScanMode changes
	 * If scanmode==Discoverable then it can be detect by any other devices
	 */
	private BroadcastReceiver bluetoothDiscoverabilityBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Find the current scanMode (you can also call EXTRA_PREVIOUS_SCAN_MODE for the
			// previous state)
			int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
			switch (scanMode) {
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
				// Start listening for new devices finding
				startListeningNewBlueToothDevice();
				// update the gui
				strLog.append("SCAN_MODE_CONNECTABLE_DISCOVERABLE \r\n");
				txvLog.setText(strLog.toString());
				btnBeDiscoverable.setEnabled(false);
				txvDiscoverableState.setText("SCAN_MODE_CONNECTABLE_DISCOVERABLE");
				break;
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
			case BluetoothAdapter.SCAN_MODE_NONE:
				// update the gui
				strLog.append(" SCAN_MODE_NONE \r\n");
				txvLog.setText(strLog.toString());
				btnBeDiscoverable.setEnabled(true);
				txvDiscoverableState.setText("SCAN_MODE_NONE");
				break;
			}
		}
	};

	/******************************************************************************************/
	/** Listening for BlueTooth Discovery Process Begin and End **/
	/** ---------------------------------------------------------------------- **/
	/**
	 * Define the brodcast receiver that listen for Start and End of the discovery process
	 * The discovery process begins when bluetoothAdapter.startDiscovery(); for 12s
	 * It's when the device look around to detects others devices
	 */
	private BroadcastReceiver bluetoothDiscoveryBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// The Start Discovery Process
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
				// Start listening for new devices finding
				startListeningNewBlueToothDevice();
				// Try to find if some one around
				strLog.append("ACTION_DISCOVERY_STARTED \r\n");
				txvLog.setText(strLog.toString());
				btnStartDiscovery.setEnabled(false);
				txvDiscoveryState.setText("ACTION_DISCOVERY_STARTED");
			}
			// The Stop Discovery Process
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
				// Stop finding others devices
				strLog.append("ACTION_DISCOVERY_FINISHED \r\n");
				txvLog.setText(strLog.toString());
				btnStartDiscovery.setEnabled(true);
				txvDiscoveryState.setText("ACTION_DISCOVERY_FINISHED");
			}
		}
	};

	/******************************************************************************************/
	/** Managing life cycle **************************************************************************/
	/******************************************************************************************/

	/*
	 * * (non-Javadoc) *
	 * * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Build the view, instanciate graphical elements, add listeners, as usual
		setContentView(R.layout.activity_discover_devices);
		txvLog = (TextView) findViewById(R.id.txv_discover_log);
		btnBeDiscoverable = (Button) findViewById(R.id.btn_be_discoverable);
		btnBeDiscoverable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Set the device as discoverable
				manageDiscovering();
			}
		});
		btnStartDiscovery = (Button) findViewById(R.id.btn_start_discovery);
		btnStartDiscovery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// begin to look around to find devices
				discoverOthersDevices();
			}
		});
		txvDiscoverableState = (TextView) findViewById(R.id.txv_discoverable_state);
		txvDiscoveryState = (TextView) findViewById(R.id.txv_discovery_state);
		// ListView initialization
		lstDevices = (ListView) findViewById(R.id.lsv_devices);
		initializeListView();
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	}

	/**
	 * initializeListView
	 */
	private void initializeListView() {
		// Instanciate the listView
		foundDevices = new ArrayList<BluetoothDevice>();
		arrayAdapter = new BluetoothDevicesAdapter(this, foundDevices);
		lstDevices.setAdapter(arrayAdapter);
	}

	/**
	 * To know if an initialization is doen
	 */
	private boolean notInitialized = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.e("DiscoverDevicesActivity", "onResume");
		super.onResume();
		if (!MyApplication.getInstance().isServiceOn()) {
			Log.e("DiscoverDevicesActivity", "onResume Service Off");
			// We check if the Bluetooth state to update the Gui with it
			if (bluetoothAdapter.isEnabled()) {
				if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					// Ensure the button be discoverable is off
					btnBeDiscoverable.setEnabled(false);
					txvDiscoverableState.setText("SCAN_MODE_CONNECTABLE_DISCOVERABLE");
				} else {
					// Ensure the button be discoverable is on
					btnBeDiscoverable.setEnabled(true);
				}
				if (bluetoothAdapter.isDiscovering()) {
					// Ensure the button be discovery is off
					btnStartDiscovery.setEnabled(false);
				} else {
					// Ensure the button be discovery is on
					btnStartDiscovery.setEnabled(true);
				}
				// read bluetooth information (just FYI)
				// String adress = bluetoothAdapter.getAddress();
				// String name = bluetoothAdapter.getName();
				// First check if the device is already paired:
				Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
				// If there are paired devices
				if (pairedDevices.size() > 0) {
					// Loop through paired devices
					for (BluetoothDevice device : pairedDevices) {
						// add them to the list of avaiable devices
						if (!foundDevices.contains(device)) {
							foundDevices.add(device);
							arrayAdapter.notifyDataSetChanged();
						}
						strLog.append("Already known paired devices : " + device.getName() + "\r\n");
						Log.e("DiscoverDevicesActivity",
								"paired device " + device.getName() + ":" + device.getAddress());
					}
				}
				txvLog.setText(strLog.toString());
			} else {
				txvLog.setText("BlueTooth disable");
				txvDiscoverableState.setText("BlueTooth disable : can not be discoverable");
				txvDiscoveryState.setText("BlueTooth disable : can not discover devices");
			}
			// register the Discoverability state changed
			registerDiscovBroadcastReceiever();
			// and kill the socket if it already exists to avoid socket exception latter
			MyApplication.getInstance().resetBluetoothSocket();
			// Initialize the server connection
			// because if the other device wants to connect to you, you need to be able to accept
			// the
			// connexion and you do that by implementing a server connexion
			creatingServerSocketConnection();
		} else {
			Log.e("DiscoverDevicesActivity", "onResume Service On");
			notInitialized = true;
			if (MyApplication.getInstance().isJustQuitingComActivity()) {
				// the service is on, the comActivity just quit
				// the user just wants to quit the application and not come back to
				// discoveringActivity
				MyApplication.getInstance().isJustQuitingComActivity(false);
				finish();
			} else {
				// the communication service is on
				// just relaunch the CommunicationActivity
				startCommunicationActivity();
			}
		}
	}

	/* * (non-Javadoc) * * @see android.app.Activity#onPause() */
	@Override
	protected void onPause() {
		Log.e("DiscoverDevicesActivity", "onPause");
		super.onPause();
		if (!notInitialized) {
			// unregister the broacastreciever that are used during the discovery phase
			unregisterDiscoveryBroadcastReceiver();
			//and kill the serverSocketThread
			try {
				if(bluetoothServerSocket!=null) {
					bluetoothServerSocket.close();
				}
			} catch (IOException e) {
				Log.e("DiscoverDevicesActivity", "onPause bluetoothServerSocket.close()", e);
			}
		}
		notInitialized=false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	/***************************************************************************************/
	/** Managing Discovery *******************************************************************/
	/**************************************************************************************/
	/** * Manage the discovery processus */
	private void manageDiscovering() {
		// To know if we need to ask for settting the mode Discovery on
		boolean askForDiscoverability = false;
		// First you need to manage the visibility of the device:
		switch (bluetoothAdapter.getScanMode()) {
		case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
			isDiscoverable = true;
			break;
		case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
			askForDiscoverability = true;
			break;
		case BluetoothAdapter.SCAN_MODE_NONE:
			askForDiscoverability = true;
			break;
		}
		// Ask for discovery
		if (askForDiscoverability) {
			// ask to be discoverable
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), REQUEST_DISCOVERABLE);
			// here this activity is going on pause
			// so it can listen for discovery change state (because onPause unregister BroadCast)
			// => manage button and textView state in onActivityResult
		} else {
			// Already discoverable, update gui
			txvDiscoverableState.setText("SCAN_MODE_CONNECTABLE_DISCOVERABLE");
			startListeningNewBlueToothDevice();
			strLog.append("SCAN_MODE_CONNECTABLE_DISCOVERABLE is on \r\n");
			txvLog.setText(strLog.toString());
		}
	}

	/*
	 * * (non-Javadoc) * * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("DiscoverDevicesActivity", "onActivityResult");
		// The Activity Action_REQUEST_DISCOVERABLE
		if (requestCode == REQUEST_DISCOVERABLE) {
			// the resultCode is the time during the device is discoverable
			if (resultCode > 0) {
				isDiscoverable = true;
				discoverableTimeRemaining = resultCode;
				txvDiscoverableState.setText("SCAN_MODE_CONNECTABLE_DISCOVERABLE (" + discoverableTimeRemaining + "s)");
				btnBeDiscoverable.setEnabled(false);
			} else {
				isDiscoverable = false;
			}
		}
	}

	/******************************************************************************************/
	/** Devices Selection **************************************************************************/
	/******************************************************************************************/
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.android2ee.android.tuto.communication.bluetooth.gui.arrayadapter.BluetoothAdapterCallBack
	 * #itemSelected(int)
	 */
	@Override
	public void itemSelected(int position) {
		// A device has been choose by the user, connect with it
		Toast.makeText(this, "Item selected : " + position, Toast.LENGTH_SHORT).show();
		// Store the device to connect with
		MyApplication.getInstance().setRemoteDevice(arrayAdapter.getItem(position));
		// start the communication activity
		startCommunicationActivity();
	}

	/**
	 * Start the communication activity
	 */
	private synchronized void startCommunicationActivity() {
		startActivity(new Intent(this, CommunicationActivity.class));
	}

	/****************************************************************************************/
	/** Managing connection *******************************************************************/
	/****************************************************************************************/

	/**
	 * * Create a server socket connection Acting like a server
	 * Because in this activity a server request connection can be sent by the other device
	 * So handle that call, store the socket and start CommunicationActivity
	 */
	private void creatingServerSocketConnection() {
		try {
			bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
					MyApplication.MY_BLUETOOTH_SERVER_NAME, MyApplication.MY_UUID);
			// The thread that will listen for client to connect
			Thread acceptServerThread = new Thread() {
				@Override
				public void run() {
					// Keep listening until exception occurs or a socket is returned
					while (true) {
						try {
							// Wait for connection request and accept it
							// once it's accpeted, store the Socket to use it in the communication
							// activity
							MyApplication.getInstance().setBluetoothSocket(bluetoothServerSocket.accept());
							// If a connection was accepted
							if (MyApplication.getInstance().getBluetoothSocket() != null) {
								// And close the connection server but not the connection
								// This releases the server socket and all its resources, but does
								// not close the connected BluetoothSocket that's been returned by
								// accept().
								bluetoothServerSocket.close();
								// Launch the communication activity
								startCommunicationActivity();
								break;
							}
						} catch (IOException e) {
							Log.e("DiscoverDevicesActivity", "creatingServerSocketConnection thread", e);
							break;
						}
					}
				}
			};
			acceptServerThread.start();
		} catch (IOException e) {
			Log.e("DiscoverDevicesActivity", "creatingServerSocketConnection", e);			
		}
	}

	/******************************************************************************************/
	/** Register and unregister receiver **************************************************************************/
	/******************************************************************************************/

	/**
	 * Register Broadcast to listen for discovery state changes
	 */
	private void registerDiscovBroadcastReceiever() {
		// register the previous BroadCast associated to the Intent Action State Changed
		registerReceiver(bluetoothDiscoverabilityBroadCastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		// Listen for begin and end of the discoverability process
		registerReceiver(bluetoothDiscoveryBroadCastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		registerReceiver(bluetoothDiscoveryBroadCastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
	}

	/** * To call when activity pause or leave */
	private void unregisterDiscoveryBroadcastReceiver() {
		// unregister the previous Broadcast receiver
		unregisterReceiver(bluetoothDiscoveryBroadCastReceiver);
		unregisterReceiver(bluetoothDiscoverabilityBroadCastReceiver);
		// stop listening for new devices
		stopListeningNewBlueToothDevice();
	}

	/**
	 * Discover others devices
	 */
	private void discoverOthersDevices() {
		if (!listeningNewDevices) {
			registerReceiver(newBluetoothFoundBroadCastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
			listeningNewDevices = true;
		}
		bluetoothAdapter.startDiscovery();
	}

	/** * Start listening for new Device found */
	private void startListeningNewBlueToothDevice() {
		if (!listeningNewDevices) {
			registerReceiver(newBluetoothFoundBroadCastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
			listeningNewDevices = true;
			bluetoothAdapter.startDiscovery();
		}
	}

	/** * Stop listening for new Device found */
	private void stopListeningNewBlueToothDevice() {
		if (listeningNewDevices) {
			unregisterReceiver(newBluetoothFoundBroadCastReceiver);
			listeningNewDevices = false;
		}
	}

}
