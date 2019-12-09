package com.mahmoud.gassensor;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MiddleActivity extends AppCompatActivity {

    Intent intent;
    String lang;

    TextView txt_mid1, txt_mid2, txt_mid3, txt_mid4;
    Button but_lightOff;

    BluetoothAdapter myBluetooth;
    BluetoothSocket btSocket;
    Set<BluetoothDevice> piaredDevices;

    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;
    String addrss, name;
    private static UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

//    static final UUID myUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_middle);

        txt_mid1 = findViewById(R.id.txt_mid1);
        txt_mid2 = findViewById(R.id.txt_mid2);
        txt_mid3 = findViewById(R.id.txt_mid3);
        txt_mid4 = findViewById(R.id.txt_mid4);
        but_lightOff = findViewById(R.id.but_lightOff);

        intent = getIntent();
        lang = intent.getStringExtra("lang");
        if (lang.equals("ar")) {
            txt_mid1.setText("1. ممنوع تشغيل اى جهاز كهربى.");
            txt_mid2.setText("2. ابعاد اى اشياء قابلة للاشتعال.");
            txt_mid3.setText("3. لا يجب تشغيل مرواح الشفاط.");
            txt_mid4.setText("4. الخروج من المنزل او المكان الذى تعرض لتسرب غاز.");
            but_lightOff.setText("أغلق الكهرباء");
        } else if (lang.equals("en")) {
            txt_mid1.setText("1. do not any electrical device.");
            txt_mid2.setText("2. keep out any flammable object.");
            txt_mid3.setText("3. electric range hood should not be turned on.");
            txt_mid4.setText("4. go out of the house or the place where the gas leaked.");
            but_lightOff.setText("Turn off The Electricity");
        }

        try {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            addrss = myBluetooth.getAddress();
            piaredDevices = myBluetooth.getBondedDevices();
            if (piaredDevices.size() > 0) {
                for (BluetoothDevice bt : piaredDevices) {
                    addrss = bt.getAddress().toString();
                    name = bt.getName().toString();

                    //   Toast.makeText(this, addrss+" "+name, Toast.LENGTH_SHORT).show();
                    if (addrss.equals("") || name.equals("HC-05")) {
                        mBTDevice = bt;
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
                        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
                        mBTDevice.getUuids();
                        /*for (ParcelUuid uuid: uuids) {
                            //  Toast.makeText(this, uuid.getUuid().toString(), Toast.LENGTH_SHORT).show();
                            MY_UUID_INSECURE=uuid.getUuid();
                        }*/
                    }

                }
            }
        } catch (Exception we) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
        startBTConnection(mBTDevice, MY_UUID_INSECURE);

        but_lightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   startBTConnection(mBTDevice,MY_UUID_INSECURE);
                try {
                    if (btSocket!=null){
                        btSocket.getOutputStream().write("Off".getBytes());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void startBTConnection(BluetoothDevice device, UUID uuid) {

        if (device != null) {
            //  Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
            //  Toast.makeText(this, uuid.toString(), Toast.LENGTH_SHORT).show();
            // mBluetoothConnection=new BluetoothConnectionService(LevelsActivity.this,uuid);
            //  mBluetoothConnection.startClient(device,uuid);
            //  mBluetoothConnection.write("1_on".getBytes());
            myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
            device = myBluetooth.getRemoteDevice(device.getAddress());//connects to the device's address and check if it's available
            try {
                btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//create a RFCOMM (SPP) connection
                btSocket.connect();
                btSocket.getOutputStream().write("Off".getBytes());

            } catch (IOException e) {
                try {
                    btSocket.close();
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//create a RFCOMM (SPP) connection
                    btSocket.connect();
                    btSocket.getOutputStream().write("hi".getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


  /*  private void bluetooth_connect_decices()throws IOException
    {
        try {
            myBluetooth=BluetoothAdapter.getDefaultAdapter();
            addrss=myBluetooth.getAddress();
            piaredDevices=myBluetooth.getBondedDevices();

            if (piaredDevices.size()>0){

                for (BluetoothDevice bt:piaredDevices)
                {
                    addrss=bt.getAddress().toString();
                    name=bt.getName().toString();

                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

                }
            }
        }catch (Exception we){
            myBluetooth=BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
            BluetoothDevice device=myBluetooth.getRemoteDevice(addrss);//connects to the device's address and check if it's available
            btSocket=device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
            btSocket.connect();
        }
    }*/

}