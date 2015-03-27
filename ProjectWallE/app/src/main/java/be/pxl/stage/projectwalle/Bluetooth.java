package be.pxl.stage.projectwalle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Bluetooth extends Activity {

    private TextView txtString, txtStringLength, chooseCocktail;
    private Handler bluetoothIn;
    private String cocktail;
    private String[] arraySpinner;
    private final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    // private static UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bluetooth);


        //Link the buttons and textViews to respective views
        Button btnVooruit = (Button) findViewById(R.id.btnVooruit);
        Button btnAchteruit = (Button) findViewById(R.id.btnAchteruit);
        Button btnLinks = (Button) findViewById(R.id.btnLinks);
        Button btnRechts = (Button) findViewById(R.id.btnRechts);
        Button btnStop = (Button) findViewById(R.id.btnStop);


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnVooruit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cocktail = new String();
                cocktail = "z";

                mConnectedThread.write(cocktail);

            }
        });

        btnAchteruit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cocktail = new String();
                cocktail = "s";

                mConnectedThread.write(cocktail);

            }
        });
        btnLinks.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cocktail = new String();
                cocktail = "q";

                mConnectedThread.write(cocktail);

            }
        });
        btnRechts.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cocktail = new String();
                cocktail = "d";

                mConnectedThread.write(cocktail);

            }
        });
        btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cocktail = new String();
                cocktail = "a";

                mConnectedThread.write(cocktail);

            }
        });

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice deviceExtra = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Parcelable[] uuidExtra = intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");


        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        //return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //return  device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID

        Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
        return (BluetoothSocket) m.invoke(device, 1);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        String action = "android.bluetooth.device.action.UUID";
        IntentFilter filter = new IntentFilter(action);
        registerReceiver(mReceiver, filter);

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        device.fetchUuidsWithSdp();

        try {
            try {
                btSocket = createBluetoothSocket(device);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Could not connect with Wall-E, please try again.", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"We were not enable to make a connection, please try again.",Toast.LENGTH_LONG).show();

            try
            {
                btSocket.close();
            } catch (IOException e2)
            {

                Toast.makeText(getApplicationContext(),"We were not enable to close the connection, please disable bluetooth and try again.",Toast.LENGTH_SHORT).show();

            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getApplicationContext(),"We were not enable to close the connection, please disable bluetooth and try again.",Toast.LENGTH_SHORT).show();

        }

    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure, please try again!", Toast.LENGTH_LONG).show();
                finish();

            }
        }

    }
}
    
