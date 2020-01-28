

package com.example.paycraft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;

    private Button btnConnect, btnVisible;

    private String connectedDeviceName = null;
    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private DataShareServices dataShareServices = null;

    private TextView textViewResult;
    private EditText editText1, editText2;
    private RadioGroup radioGroup;
    private Button resultBtn;
    private Handler handler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case DataShareServices.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,
                                    connectedDeviceName));

                            break;
                        case DataShareServices.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case DataShareServices.STATE_LISTEN:
                        case DataShareServices.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.charAt(0) == ' ') {
                        Toast.makeText(getApplicationContext(),
                                "Got result " + readMessage,
                                Toast.LENGTH_SHORT).show();
                        textViewResult.setText(readMessage);

                    } else if (readMessage.charAt(0) == 'q') {
                        Toast.makeText(getApplicationContext(),
                                "Caluculting and sending " + readMessage,
                                Toast.LENGTH_SHORT).show();
                        Calculate(readMessage);
                    } else {

                        Toast.makeText(getApplicationContext(),
                                "Error in Sending  " + readMessage,
                                Toast.LENGTH_SHORT).show();
                    }

                    // chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }


    });
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                //  sendMessage(message);
            }
            return true;
        }
    };

    private void Calculate(String readMessage) {
        String res = "";
        String ns1 = "", ns2 = "", op = "";
        int count = 0;
        for (int i = 0; i < readMessage.length(); i++) {
            if (readMessage.charAt(i) != ',') {
                if (count == 0) {

                }
                if (count == 1) {
                    ns1 = ns1 + readMessage.charAt(i);
                }
                if (count == 2) {
                    ns2 = ns2 + readMessage.charAt(i);
                }
                if (count == 3) {
                    op = op + readMessage.charAt(i);
                }
            } else {
                count++;
            }

        }
        float r;
        if (op.charAt(0) == 'D') {
            r = Float.parseFloat(ns1.trim()) / Float.parseFloat(ns2.trim());
            Toast.makeText(MainActivity.this, r + " Div", Toast.LENGTH_LONG).show();
        } else if (op.charAt(0) == 'M') {
            r = Float.parseFloat(ns1.trim()) * Float.parseFloat(ns2.trim());
            Toast.makeText(MainActivity.this, r + " Mul", Toast.LENGTH_LONG).show();

        } else {
            r = 0;
            Toast.makeText(MainActivity.this, r + " Error in cal ", Toast.LENGTH_LONG).show();
            //error

        }
        Toast.makeText(MainActivity.this, "calculated and sent " + r, Toast.LENGTH_LONG).show();

        sendMessage(" " + r);

        return;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.connect_dv_securly_btn);
        btnVisible = findViewById(R.id.visible_btn);

        resultBtn = findViewById(R.id.requset_result_btn);
        textViewResult = findViewById(R.id.res_txt);
        editText1 = findViewById(R.id.fist_oprent_edit);
        editText2 = findViewById(R.id.sec_oprent_edit);
        radioGroup = findViewById(R.id.radioGroup);
        resultBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String wht = "", n1 = "", n2 = "", op = "";

                if (editText1.getText().toString() != "") {
                    n1 = editText1.getText().toString();
                } else {
                    Toast.makeText(MainActivity.this, "Enter N1", Toast.LENGTH_LONG).show();
                }

                if (editText2.getText().toString() != "") {
                    n2 = editText2.getText().toString();
                } else {
                    Toast.makeText(MainActivity.this, "Enter in N2", Toast.LENGTH_LONG).show();
                }
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioDiv) {
                    op = "Div";

                } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioMul) {
                    op = "Mul";
                } else {
                    Toast.makeText(MainActivity.this, "Select Opration", Toast.LENGTH_LONG).show();
                }

                wht = "q";  // 1 for request

                String d = wht + "," + n1 + "," + n2 + "," + op;
                sendMessage(d);
            }
        });

        btnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent serverIntent;
                serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

            }
        });

        btnVisible.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ensureDiscoverable();
            }
        });


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }


    }


    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        dataShareServices.connect(device, secure);
    }


    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        if (dataShareServices.getState() != DataShareServices.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String msgbyts = message;
        byte[] send = msgbyts.getBytes();
        dataShareServices.write(send);

        outStringBuffer.setLength(0);

    }


    private final void setStatus(int resId) {
        final androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void setupChat() {

        dataShareServices = new DataShareServices(this, handler);

        outStringBuffer = new StringBuffer();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (dataShareServices == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (dataShareServices != null) {
            if (dataShareServices.getState() == DataShareServices.STATE_NONE) {
                dataShareServices.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dataShareServices != null)
            dataShareServices.stop();
    }

}
