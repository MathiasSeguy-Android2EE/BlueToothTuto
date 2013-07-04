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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android2ee.android.tuto.communication.bluetooth.MyApplication;
import com.android2ee.android.tuto.communication.bluetooth.R;
import com.android2ee.android.tuto.communication.bluetooth.gui.arrayadapter.BluetoothAdapterCallBack;
import com.android2ee.android.tuto.communication.bluetooth.gui.arrayadapter.BluetoothMessageAdapter;
import com.android2ee.android.tuto.communication.bluetooth.gui.model.BluetoothMessage;
import com.android2ee.android.tuto.communication.bluetooth.service.CommunicationService;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to displays and send message to the other device
 *        There is no communication here, it's just gui
 *        The activity is bound to the communicationService in charge of the bluetooth communication
 */
public class CommunicationActivity extends Activity implements BluetoothAdapterCallBack {
	/**
	 * Edt Message
	 */
	EditText edtMessage;
	/**
	 * button send
	 */
	Button btnSend;
	/**
	 * The listView
	 */
	ListView lstMessage;
	/**
	 * The items to display
	 */
	List<BluetoothMessage> items;
	/**
	 * The arrayAdpter that is bound to the ListView
	 */
	BluetoothMessageAdapter arrayAdapter;
	/**
	 * The Bluetooth Adapter
	 */
	private BluetoothAdapter bluetoothAdapter;
	/**
	 * The bluetooth client socket
	 */
	BluetoothSocket bluetoothClientSocket = null;
	/**
	 * Strings to display the devices name
	 */
	String localDeviceName = null, remoteDeviceName = null;

	/******************************************************************************************/
	/** Managing life cycle **************************************************************************/
	/******************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("CommunicationActivity", "onCreate");
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().setComActivity(this);
		setContentView(R.layout.activity_main);
		// Les findViewById
		instantiateView();
		// ListView initialization
		initializeListView();
		// Add listener on ListView and button
		addListeners();
		// retrieve the blueToothAdpater
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// bind with the service that handles the bluetooth communication
		if (MyApplication.getInstance().getBluetoothSocket() != null) {
			// the service is already started so bind to it
			bindService(new Intent(this, CommunicationService.class), onService, BIND_AUTO_CREATE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.e("CommunicationActivity", "onResume");
		super.onResume();
		// register the receiver that listens for Intent launched by the communication service
		registerReceiver(communicationServiceReceiver, new IntentFilter(
				CommunicationService.BLUETOOTH_COMMUNICATION_INTENT_ACTION));
		// if the socket is not set yet, => you have to connect to the other device as a client
		// else the other device is already connected to you
		if (MyApplication.getInstance().getBluetoothSocket() == null) {
			Log.e("CommunicationActivity", "onResume Socket connected ==null");
			// Initialize the connection as a client:
			creatingClientSocketConnection();
		} else {
			Log.e("CommunicationActivity", "onResume Socket connected "
					+ MyApplication.getInstance().getBluetoothSocket().isConnected());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		Log.e("CommunicationActivity", "onPause");
		super.onPause();
		// unregister the receiver that listens for Intent launched by the communication service
		unregisterReceiver(communicationServiceReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.e("CommunicationActivity", "onDestroy");
		super.onDestroy();
		// unbind to the service
		unbindService(onService);
		// reset bluetooth connection
		MyApplication.getInstance().resetBluetoothSocket(true);
		// ensure the client thread dies
		if (bluetoothClientSocket != null) {
			try {
				bluetoothClientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/****************************************************************************************/
	/** Managing connection *******************************************************************/
	/****************************************************************************************/

	/** * Create a server socket connection Acting like a client */
	private void creatingClientSocketConnection() {
		try {
			// find the device you want to connect to (already stored in the application)
			// It's the one discovered and choose by the user
			BluetoothDevice bluetoothDevice = MyApplication.getInstance().getRemoteDevice();
			// if a device has been choosen
			if (bluetoothDevice != null) {
				// Ask to connect to with that UUID
				bluetoothClientSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MyApplication.MY_UUID);
				// The thread that will listen for client to connect
				Thread acceptClientThread = new Thread() {
					@Override
					public void run() {
						// Cancel discovery because it will slow down the connection
						bluetoothAdapter.cancelDiscovery();
						try {
							// Connect the device through the socket. This will block
							// until it succeeds or throws an exception
							bluetoothClientSocket.connect();
							// Set the bluetoothSocket with that socket
							MyApplication.getInstance().setBluetoothSocket(bluetoothClientSocket);
						} catch (IOException connectException) {
							// Unable to connect; close the socket and get out
							try {
								if (MyApplication.getInstance().getBluetoothSocket() != null) {
									MyApplication.getInstance().getBluetoothSocket().close();
								}
							} catch (IOException closeException) {
							}
							return;
						}
						// So the socket has been created and store
						// the CommunicationService is so launched
						// so bind to it
						bindToService();
					}
				};
				// start the thread
				acceptClientThread.start();
			}
		} catch (IOException e) {
			Log.e("CommunicationActivity", "creatingClientSocketConnection", e);
		}

	}

	/**
	 * Just bind to the CommunicationService service
	 */
	private void bindToService() {
		// the service is already started so bind to it
		bindService(new Intent(this, CommunicationService.class), onService, BIND_AUTO_CREATE);
	}

	/*************************************************************************************/
	/** Managing bind with the communicationService **************************************************************/
	/*************************************************************************************/
	/**
	 * The service that handles the bluetooth communication between the devices
	 */
	private CommunicationService comService = null;
	/**
	 * The serviceConnection object
	 */
	private ServiceConnection onService = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			comService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			comService = ((CommunicationService.LocalBinder) service).getService();
			// now the send button can be enable
			btnSend.setEnabled(true);
		}
	};
	/**
	 * The one that listen for the intent sent by the service
	 */
	private BroadcastReceiver communicationServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// just retrieve the message and use it:
			handleInComingMessage(intent.getStringExtra(CommunicationService.BLUETOOTH_MESSAGE));
		}

	};

	/******************************************************************************************/
	/** Instantiate Activity **************************************************************************/
	/******************************************************************************************/

	/**
	 * As usual
	 */
	private void initializeListView() {
		// Instanciate the listView
		items = new ArrayList<BluetoothMessage>();
		arrayAdapter = new BluetoothMessageAdapter(this, items);
		lstMessage.setAdapter(arrayAdapter);
		lstMessage.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	/**
	 * As usual
	 */
	private void addListeners() {
		lstMessage.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemSelected(position);
			}
		});
		// Add listener
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSendClicked();
			}
		});
	}

	/**
	 * As usual
	 */
	private void instantiateView() {
		edtMessage = (EditText) findViewById(R.id.edt_message);
		btnSend = (Button) findViewById(R.id.btn_add);
		btnSend.setEnabled(false);
		lstMessage = (ListView) findViewById(R.id.lsv_messages);
		Log.e(this.getClass().getSimpleName(), "My log message");
	}

	/******************************************************************************************/
	/** Managing selection **************************************************************************/
	/******************************************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.android2ee.formation.mai.mmxiii.premiertp.HumanAdapterCallBack#itemSelected(int)
	 */
	@Override
	public void itemSelected(int position) {
		BluetoothMessage btMessage = arrayAdapter.getItem(position);
		edtMessage.setText(btMessage.getMessage());
	}

	/**
	 * Called when a click is done on the btnSend
	 */
	private void btnSendClicked() {
		// Read EditText
		String str = edtMessage.getText().toString();
		if (localDeviceName == null) {
			localDeviceName = bluetoothAdapter.getName();
		}
		// Build the Bluetooth message
		BluetoothMessage toto = new BluetoothMessage(localDeviceName, str, true);
		// Add it to the list
		arrayAdapter.add(toto);
		// Ask the communication service to send the message
		comService.sendMessage(str);
		// and flush the editText
		edtMessage.setText("");
	}

	/**
	 * @param readMessage
	 */
	private void handleInComingMessage(String readMessage) {
		// Build the Bluetooth message
		if (remoteDeviceName == null) {
			remoteDeviceName = MyApplication.getInstance().getBluetoothSocket().getRemoteDevice().getName();
		}
		BluetoothMessage toto = new BluetoothMessage(remoteDeviceName, readMessage, false);
		// Add the string to the txvMessages
		arrayAdapter.add(toto);
	}

}
