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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android2ee.android.tuto.communication.bluetooth.R;
import com.android2ee.android.tuto.communication.bluetooth.gui.model.BluetoothMessage;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to be the arrayAdpater bound to the listView that displays
 *        BluetoothMessages
 */
public class BluetoothMessageAdapter extends ArrayAdapter<BluetoothMessage> implements BluetoothAdapterCallBack {

	/**
	 * Inflater
	 */
	LayoutInflater inflater;

	/**
	 * CallBack
	 */
	BluetoothAdapterCallBack callBack;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 * @param objects
	 *            The list of objects to display
	 */
	public BluetoothMessageAdapter(Context context, List<BluetoothMessage> objects) {
		super(context, R.layout.item, objects);
		inflater = LayoutInflater.from(getContext());
		callBack = (BluetoothAdapterCallBack) context;
	}

	// Avoid using temp variable as method's variable
	private static BluetoothMessage btMessage;
	private static View myview;
	private static ViewHolder viewHolder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		btMessage = getItem(position);
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
		viewHolder.getTxvMessage().setText(btMessage.getMessage());
		viewHolder.getTxvName().setText(btMessage.getName());

		viewHolder.getTxvMessage().setOnClickListener(new MyOnClickListener(position, this));
		viewHolder.getTxvName().setOnClickListener(new MyOnClickListener(position, this));
		return myview;
	}

	/**
	 * @param position
	 */
	public void itemSelected(int position) {
		callBack.itemSelected(position);
	}

	/******************************************************************************************/
	/** Managing the odd/even lines **************************************************************************/
	/******************************************************************************************/

	@Override
	public int getViewTypeCount() {
		// return the number of type managed by the list view:
		// We have two types, one for the message send by the local device, the other for the
		// message received from the other device
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// return the type of the element to be displayed at position position
		// We have two types, one for the message send by the local device, the other for the
		// message received from the other device
		if (((BluetoothMessage) getItem(position)).isLocalDevice()) {
			return 0;
		} else {
			return 1;
		}

	}

	/**
	 * @author Mathias Seguy (Android2EE)
	 * @goals
	 *        This class aims to simple click listener
	 */
	public static class MyOnClickListener implements OnClickListener {
		// No comment, sorry
		int position;
		BluetoothAdapterCallBack callback;

		/**
		 * @param position
		 */
		private MyOnClickListener(int position, BluetoothAdapterCallBack callback) {
			super();
			this.position = position;
			this.callback = callback;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			callback.itemSelected(position);
		}

	}

	/******************************************************************************************/
	/** ViewHolder **************************************************************************/
	/******************************************************************************************/

	public static class ViewHolder {
		// No comment, sorry, not the goal of that tutorial
		View boundView;
		TextView txvName;
		TextView txvMessage;

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
		public final TextView getTxvMessage() {
			if (null == txvMessage) {
				txvMessage = (TextView) boundView.findViewById(R.id.message);
			}
			return txvMessage;
		}

		/**
		 * @param txvAdress
		 *            the txvAdress to set
		 */
		public final void setTxvMessage(TextView txvMessage) {
			this.txvMessage = txvMessage;
		}

	}

}
