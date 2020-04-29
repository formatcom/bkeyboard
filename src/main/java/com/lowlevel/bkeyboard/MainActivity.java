package com.lowlevel.bkeyboard;

import android.util.Log;
import android.os.Bundle;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.UUID;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.OutputStream;

// REF: https://developer.android.com/studio/debug/am-logcat?hl=es-419

// REF: https://android.googlesource.com/platform/frameworks/base/
// REF: https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r55
// REF: https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r55/core/java/
// REF: https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r55/core/java/android/view/View.java

// REF: https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r55/core/res/res/values/
// REF: https://developer.android.com/reference/android/view/inputmethod/InputMethodManager
// REF: https://developer.android.com/reference/android/app/Activity


public class MainActivity extends Activity implements OnClickListener
{
	// TAG: com.lowlevel.bkeyboard, usado para filtrar en logcat
	private static final String TAG = "com.lowlevel.bkeyboard";

	private static final int REQUEST_ENABLE_BT = 1;

	// REF: https://www.bluetooth.com/specifications/assigned-numbers/service-discovery/
	private static final UUID UUIDSPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static final int KEYMAP_DEFAULT = 0;
	private static final int KEYMAP_ANIMAL_CROSSING_ES = 1;

	private byte  KEYMAP[][] = new byte[2][0xFF];

	private BluetoothDevice mmDevice;
	private BluetoothSocket mmSocket;
	private OutputStream    mmOutStream;

	private Spinner spiDevice;
	private Spinner spiKeyMap;
	private Button  btnAttach;


	private Set<BluetoothDevice> pairedDevices;

	private boolean attach;

	private void setKeyMap() {

		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x22] = 0x40; // "
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x26] = 0x5E; // &
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x27] = 0x2D; // '
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x28] = 0x2A; // (
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x29] = 0x28; // )
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x2A] = 0x7D; // *
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x2B] = 0x5D; // +
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x2D] = 0x2F; // -
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x2F] = 0x26; // /
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x3A] = 0x3E; // :
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x3B] = 0x3C; // ;
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x3D] = 0x29; // =
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x3F] = 0x5F; // ?
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x5E] = 0x7B; // ^
		KEYMAP[KEYMAP_ANIMAL_CROSSING_ES][0x5F] = 0x3F; // _
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setKeyMap();

		List<String> devices = new ArrayList<String>();

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {

			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}

			pairedDevices = bluetoothAdapter.getBondedDevices();

			if (pairedDevices.size() > 0) {
				// There are paired devices. Get the name and address of each paired device.
				for (BluetoothDevice device : pairedDevices) {
					String _device = String.format("%s %s", device.getName(), device.getAddress());
					devices.add(_device);
				}
			}

		}

		LinearLayout layoutRoot     = new LinearLayout(this);
		LinearLayout layoutSelector = new LinearLayout(this);
		LinearLayout layoutKeyMap   = new LinearLayout(this);

		layoutRoot.setOrientation(LinearLayout.VERTICAL);

		btnAttach = new Button(this);
		btnAttach.setText("Attach");

		spiDevice = new Spinner(this);

		btnAttach.setOnClickListener(this);

		ArrayAdapter<String> list = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devices);
		spiDevice.setAdapter(list);

		layoutSelector.addView(spiDevice, new ViewGroup.LayoutParams(
						800,
						ViewGroup.LayoutParams.WRAP_CONTENT));

		layoutSelector.addView(btnAttach, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

		spiKeyMap = new Spinner(this);

		/* A la final no lo necesito, pero lo dejo por aqui :3
		spiKeyMap.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				Log.d(TAG, String.format("select:[%d]", position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {}
		});
		*/

		List<String> keymap = new ArrayList<String>();
		keymap.add("KEYMAP DEFAULT");
		keymap.add("KEYMAP ANIMAL CROSSING ES");

		ArrayAdapter<String> listKeyMap = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, keymap);
		spiKeyMap.setAdapter(listKeyMap);

		layoutKeyMap.addView(spiKeyMap, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));


		layoutRoot.addView(layoutSelector, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

		layoutRoot.addView(layoutKeyMap, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

		setContentView(layoutRoot);
	}

	public void Detach() {
		Log.d(TAG, "Detach");
		try {
			mmSocket.close();
			attach = false;
			btnAttach.setText("Attach");
		} catch (IOException e) {
			Log.e(TAG, "Could not close the client socket", e);
		}
	}

	public void Attach(BluetoothDevice device) {

		Log.d(TAG, "Attach");

		BluetoothSocket tmp    = null;
		OutputStream    tmpOut = null;

		mmDevice = device;
		try {
			tmp = device.createRfcommSocketToServiceRecord(UUIDSPP);
		} catch (IOException e) {
			Log.e(TAG, "Socket's create() method failed", e);
			attach = false;
			return;
		}

		mmSocket = tmp;

		try {
			mmSocket.connect();
		} catch (IOException e) {
			Detach();
			return;
		}

		try {
			tmpOut = mmSocket.getOutputStream();
		} catch (IOException e) {
			Log.e(TAG, "Error occurred when creating output stream", e);
			Detach();
			return;
		}

		mmOutStream = tmpOut;

		attach = true;
		btnAttach.setText("Detach");
	}

	@Override
	public void onClick(View view) {
		if (attach){
			Detach();
			return;
		}

		Log.d(TAG, "onClick");
		Log.d(TAG, String.format("select: %s [%d]",
				spiDevice.getSelectedItem().toString(), spiDevice.getSelectedItemPosition()));


		if (pairedDevices.size() > 0) {
			int i = 0;
			for (BluetoothDevice device : pairedDevices) {
				if (i != spiDevice.getSelectedItemPosition()) {
					i++;
					continue;
				}

				Attach(device);
				break;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (!attach) return true;

		if (event.getAction() == MotionEvent.ACTION_UP) return true;

		int keyCode = event.getKeyCode();
		byte[] c = new byte[1];

		try {
			c[0] = (byte) event.getUnicodeChar();

			// REF: https://www.arduino.cc/en/Reference/KeyboardModifiers
			// REF: https://www.arduino.cc/en/Reference.KeyboardWrite
			if (keyCode == KeyEvent.KEYCODE_DEL) c[0] = 0x08;

			if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) return true;

			// fix this
			if (keyCode == KeyEvent.KEYCODE_UNKNOWN)
			{
				c[0] = 0x3A; // ñ
			}else

			if (KEYMAP[spiKeyMap.getSelectedItemPosition()][c[0]] != 0)
			{
				c[0] = KEYMAP[spiKeyMap.getSelectedItemPosition()][c[0]];
			}

			mmOutStream.write(c);
		} catch (IOException e) {
			Log.e(TAG, "SendKey", e);
			Detach();
			return true;
		}

		Log.d(TAG, event.toString());
		Log.d(TAG, String.format("KeyCode: %d", event.getKeyCode()));
		Log.d(TAG, String.format("ascii:   %d", c[0]));
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}

		Log.d(TAG, "onTouchEvent");
		return true;
	}

}
