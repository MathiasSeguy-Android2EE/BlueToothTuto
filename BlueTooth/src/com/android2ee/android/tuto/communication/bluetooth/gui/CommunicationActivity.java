package com.android2ee.android.tuto.communication.bluetooth.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class CommunicationActivity extends Activity implements BluetoothAdapterCallBack {
	/**
	 * Edt Message
	 */
	EditText edtMessage;
	/**
	 * button Add
	 */
	Button btnAdd;
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
	 * The handler that update the UI using data thrown by the ConnectedThread
	 */
	Handler myHandlerSocketConnected;
	/**
	 * The message.isWhat launched to the handler
	 */
	private final int MESSAGE_READ = 4112008;
	/**
	 * The thread that manage the data exchange between the both device
	 */
	ConnectedThread connectedThread;

	/**
	 * Strings to display the devices name
	 */
	String localDeviceName = null, remoteDeviceName = null;
	/**
	 * Boolean to create the communication thread only once
	 */
	boolean alreadyManagedCommunication = false;
	/******************************************************************************************/
	/** Managing life cycle **************************************************************************/
	/******************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Les findViewById
		instantiateView();
		// ListView initialization
		initializeListView();
		// Add listener on ListView and button
		addListeners();
		// We suppose the device we want to talk with is found
		// Here two cases, we act as a server or as a client
		// But in reality, you have cover those 2 cases by implementing the both
		// Initialise the Handler that will listen to the connection exchange between the devices
		myHandlerSocketConnected = new Handler() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				handleInComingMessage(readMessage);
			}

		};
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (MyApplication.getInstance().getBluetoothSocket() != null) {
			Log.e("CommunicationActivity", "onCreate Socket != null connected "
					+ MyApplication.getInstance().getBluetoothSocket().isConnected());
			manageConnectedSocket();
		} else {
			Log.e("CommunicationActivity", "onCreate Socket connected == null ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
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
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		try {
			if (MyApplication.getInstance().getBluetoothSocket() != null) {
				MyApplication.getInstance().getBluetoothSocket().close();
			}
		} catch (IOException e) {
			Log.e("CommunicationActivity", "onDestroy", e);
		}
		super.onDestroy();
	}

	/****************************************************************************************/
	/** Managing connection *******************************************************************/
	/****************************************************************************************/

	/** * Create a server socket connection Acting like a client */
	private void creatingClientSocketConnection() {
		try {
			// find the device you want to connect to
			BluetoothDevice bluetoothDevice = MyApplication.getInstance().getRemoteDevice();// blueToothDevice.get("knowDeviceName");blueT
			if (bluetoothDevice != null) {
				// Ask to connect to with that UUID
				final BluetoothSocket bluetoothClientSocket = bluetoothDevice
						.createRfcommSocketToServiceRecord(MyApplication.MY_UUID);
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
						// Do work to manage the connection (in a separate thread)
						manageConnectedSocket();
					}
				};
				acceptClientThread.start();
			}
		} catch (IOException e) {
			Log.e("CommunicationActivity", "creatingClientSocketConnection", e);
		}

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
			// And if you want to talk somewhere in your code, just use the following lines:
			String str = "message";
			connectedThread.write(str.getBytes());
		}

	}

	/** * The thread that manage communication */
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
					// Send the obtained bytes to the UI Activity
					myHandlerSocketConnected.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				} catch (IOException e) {
					Log.e("CommunicationActivity", "ConnectedThread run", e);
					break;
				}
			}
		}

		/* Call this from the main Activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				// outputSocketStream.write(bytes);
				MyApplication.getInstance().getOutputSocketStream().write(bytes);
			} catch (IOException e) {
				Log.e("CommunicationActivity", "ConnectedThread write", e);
			}
		}

		/* Call this from the main Activity to shutdown the connection */
		public void cancel() {
			try {
				MyApplication.getInstance().getBluetoothSocket().close();
			} catch (IOException e) {
				Log.e("CommunicationActivity", "ConnectedThread cancel", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/******************************************************************************************/
	/** Instantiate Activity **************************************************************************/
	/******************************************************************************************/

	/**
	 * 
	 */
	private void initializeListView() {
		// Instanciate the listView
		items = new ArrayList<BluetoothMessage>();
		arrayAdapter = new BluetoothMessageAdapter(this, items);
		lstMessage.setAdapter(arrayAdapter);
		lstMessage.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	/**
	 * 
	 */
	private void addListeners() {
		lstMessage.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemSelected(position);
			}
		});
		// Add listener
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAddClicked();
			}
		});
	}

	/**
	 * 
	 */
	private void instantiateView() {
		edtMessage = (EditText) findViewById(R.id.edt_message);
		btnAdd = (Button) findViewById(R.id.btn_add);
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
		BluetoothMessage human = arrayAdapter.getItem(position);
		edtMessage.setText(human.getMessage());
	}

	/**
	 * Called when a click is done on the btnAdd
	 */
	private void btnAddClicked() {
		// Read EditText
		String str = edtMessage.getText().toString();
		if (localDeviceName == null) {
			localDeviceName = bluetoothAdapter.getName();
		}
		BluetoothMessage toto = new BluetoothMessage(localDeviceName, str, true);
		// Add the string to the txvMessages
		arrayAdapter.add(toto);
		connectedThread.write(str.getBytes());
		// Or
		// items.add(str);
		// arrayAdapter.notifyDataSetChanged();
		// and flush the editText
		edtMessage.setText("");
	}

	/**
	 * @param readMessage
	 */
	private void handleInComingMessage(String readMessage) {
		if (remoteDeviceName == null) {
			remoteDeviceName = MyApplication.getInstance().getBluetoothSocket().getRemoteDevice().getName();
		}
		BluetoothMessage toto = new BluetoothMessage(remoteDeviceName, readMessage, false);
		// Add the string to the txvMessages
		arrayAdapter.add(toto);
		// update the Gui with the message
		// mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
	}

}
