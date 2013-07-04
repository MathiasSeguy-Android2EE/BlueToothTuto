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
package com.android2ee.android.tuto.communication.bluetooth.gui.arrayadapter;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android2ee.android.tuto.communication.bluetooth.R;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to the arrayAdpater for listViews that display BlueTooth device
 */
public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice> implements BluetoothAdapterCallBack {

	/**
	 * Layout inflater
	 */
	LayoutInflater inflater;

	/**
	 * Callback
	 */
	BluetoothAdapterCallBack callBack;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the context
	 * @param objects
	 *            the list of objects to display
	 */
	public BluetoothDevicesAdapter(Context context, List<BluetoothDevice> objects) {
		super(context, R.layout.item, objects);
		inflater = LayoutInflater.from(getContext());
		callBack = (BluetoothAdapterCallBack) context;
	}

	// Avoid using temp variable as method's variable
	private static BluetoothDevice device;
	private static View myview;
	private static ViewHolder viewHolder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		device = getItem(position);
		myview = convertView;
		if (null == myview) {
			if (getItemViewType(position) == 0) {
				myview = inflater.inflate(R.layout.item, null);

			} else {
				myview = inflater.inflate(R.layout.item_odd, null);
			}
			viewHolder = new ViewHolder(myview);
			myview.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) myview.getTag();
		}
		viewHolder.getTxvAdress().setText(device.getAddress());
		viewHolder.getTxvName().setText(device.getName());

		viewHolder.getTxvAdress().setOnClickListener(new MyOnClickListener(position, this));
		viewHolder.getTxvName().setOnClickListener(new MyOnClickListener(position, this));
		return myview;
	}

	// /**
	// * @param position
	// */
	public void itemSelected(int position) {
		callBack.itemSelected(position);
	}

	/******************************************************************************************/
	/** Managing differents view **************************************************************************/
	/******************************************************************************************/

	/******************************************************************************************/
	/** Managing the odd/even lines **************************************************************************/
	/******************************************************************************************/

	@Override
	public int getViewTypeCount() {
		// return the number of type managed by the list view:
		// We have two types, one for the even line, the other for the odd lines
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// return the type of the element to be displayed at position position
		// We have two types, one for the even line, the other for the odd lines
		return position % 2;
	}

	public static class MyOnClickListener implements OnClickListener {

		int position;
		BluetoothAdapterCallBack callBack;

		/**
		 * @param position
		 */
		private MyOnClickListener(int position, BluetoothAdapterCallBack callBack) {
			super();
			this.position = position;
			this.callBack = callBack;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			callBack.itemSelected(position);
		}

	}

	/******************************************************************************************/
	/** ViewHolder **************************************************************************/
	/******************************************************************************************/

	public static class ViewHolder {
		View boundView;
		TextView txvName;
		TextView txvAdress;

		/**
		 * @param boundView
		 */
		private ViewHolder(View boundView) {
			super();
			this.boundView = boundView;
		}

		/**
		 * @return the txvName
		 */
		public final TextView getTxvName() {
			if (null == txvName) {
				txvName = (TextView) boundView.findViewById(R.id.nom);
			}
			return txvName;
		}

		/**
		 * @param txvName
		 *            the txvName to set
		 */
		public final void setTxvName(TextView txvName) {
			this.txvName = txvName;
		}

		/**
		 * @return the txvAdress
		 */
		public final TextView getTxvAdress() {
			if (null == txvAdress) {
				txvAdress = (TextView) boundView.findViewById(R.id.message);
			}
			return txvAdress;
		}

		/**
		 * @param txvAdress
		 *            the txvAdress to set
		 */
		public final void setTxvAdress(TextView txvAdress) {
			this.txvAdress = txvAdress;
		}

	}

}
