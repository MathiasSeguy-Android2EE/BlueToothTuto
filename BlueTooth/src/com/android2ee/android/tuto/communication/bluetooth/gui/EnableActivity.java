package com.android2ee.android.tuto.communication.bluetooth.gui;

import com.android2ee.android.tuto.communication.bluetooth.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class EnableActivity extends Activity {
	/** * The Bluetooth Adapter */
	private BluetoothAdapter bluetoothAdapter;
	/**
	 * * The unique Request code send with the Intent BluetoothAdapter.ACTION_REQUEST_ENABLE when
	 * starting the activty to enable bluetooth
	 */
	private final int REQUEST_ENABLE_BT = 11021974;
	/** * A boolean to know if BlueTooth is enabled */
	boolean blueToothEnable = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_enable);
		// settingUpBluetooth
		if (supportBluetooth()) {
			// Then Enable bluetooth
			enableBluetooth();
		}else {
			finish();
		}
	}

	/***************************************************************************************/
	/** ENABLE BlueTooth *******************************************************************/
	/***************************************************************************************/
	/**
	 * * Instanciate the bluetoothadapter *
	 * 
	 * @return true is device support bluetooth
	 */
	private boolean supportBluetooth() {
		boolean isBluetooth = false;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			// the device supports BlueTooth
			isBluetooth = true;
		}
		return isBluetooth;
	}

	/**
	 * * Test if bluetooth is enabled, If the Bluetooth is not enable, then ask the user to set it
	 * enable
	 */
	private void enableBluetooth() {
		blueToothEnable = bluetoothAdapter.isEnabled();
		if (!blueToothEnable) {
			// Ask the user to set enable the Bluetooth
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}else {
			//just call the next activity
			Intent discoverDeviceIntent=new Intent(this,DiscoverDevicesActivity.class);
			startActivity(discoverDeviceIntent);
			//and die
			finish();
		}
	}

	/* * (non-Javadoc) @see android.app.Activity#onActivityResult(int, int, android.content.Intent) */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// The Activty ACTION_REQUEST_ENABLE returns
		if (requestCode == REQUEST_ENABLE_BT) {
			switch (resultCode) {
			case Activity.RESULT_OK:
				// The result is ok 
				enableBluetooth();
				break;
			case Activity.RESULT_CANCELED:
			default:
				// the result is KO
				blueToothEnable = false;
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.enable, menu);
		return true;
	}

}
