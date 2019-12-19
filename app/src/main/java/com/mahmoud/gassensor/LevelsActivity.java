package com.mahmoud.gassensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


public class LevelsActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";

    Button but_high,but_mid,but_low;

    Intent intent;
    String lang;

    StringBuilder message;

    BluetoothAdapter myBluetooth;
    Set<BluetoothDevice> piaredDevices;
    BluetoothSocket btSocket;

    String addrss,name;

    Thread thread;

    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;

    Handler handler;

    //"8ce255c0-200a-11e0-ac64-0800200c9a66"
   private static  UUID MY_UUID_INSECURE=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 //  private static final UUID MY_UUID_INSECURE=UUID.fromString("00-21-31-00-0800200c9a66");




    @Override
    protected void onResume() {
        super.onResume();
        startConnection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);

        //broadcasts when bond state changes (ie:pairing)


        try {
            myBluetooth=BluetoothAdapter.getDefaultAdapter();
            addrss=myBluetooth.getAddress();
            piaredDevices=myBluetooth.getBondedDevices();
            if (piaredDevices.size()>0){
                for (BluetoothDevice bt:piaredDevices)
                {
                    addrss=bt.getAddress().toString();
                    name=bt.getName().toString();

                 //   Toast.makeText(this, addrss+" "+name, Toast.LENGTH_SHORT).show();
                    if (addrss.equals("")||name.equals("HC-05")){
                        mBTDevice=bt;
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
                        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
                        mBTDevice.getUuids();
                        for (ParcelUuid uuid: uuids) {
                          //  Toast.makeText(this, uuid.getUuid().toString(), Toast.LENGTH_SHORT).show();
                            MY_UUID_INSECURE=uuid.getUuid();
                        }
                    }

                }
            }
        }catch (Exception we){
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        intent=getIntent();
        lang=intent.getStringExtra("lang");

        but_high=findViewById(R.id.but_high);
        but_mid=findViewById(R.id.but_mid);
        but_low=findViewById(R.id.but_low);

        message=new StringBuilder();


        startConnection();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));


        if (lang.equals("en")){
            but_high.setText("High");
            but_mid.setText("Middle");
            but_low.setText("Low");
        }else if (lang.equals("ar")){
            but_high.setText("مرتفع");
            but_mid.setText("متوسط");
            but_low.setText("منخفض");
        }

        but_high.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LevelsActivity.this,HighActivity.class);
                i.putExtra("lang",lang);

                startActivity(i);
            }
        });

        but_mid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LevelsActivity.this,MiddleActivity.class);
                i.putExtra("lang",lang);
                try {
                    if (btSocket!=null){
                        btSocket.close();
                    }
                   // btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });

        but_low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LevelsActivity.this,LowActivity.class);
                i.putExtra("lang",lang);
                startActivity(i);
            }
        });
    }


    BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text=intent.getStringExtra("TheMessage");
            Toast.makeText(context,text, Toast.LENGTH_SHORT).show();
            //the action that depandance on sensor read
        }
    };

    //create method for starting connection
    //remember the connection will fail and app will crash if you haven't paired first
    public void startConnection(){
        try {
            startBTConnection(mBTDevice,MY_UUID_INSECURE);
        }catch (Exception e){

        }
    }

    //starting service method
    public void startBTConnection(BluetoothDevice device,UUID uuid){
        Log.d(TAG,"startBTConnection: Initializing RFCOM Bluetooth Connection.");
        if(device!=null){
          //  Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
          //  Toast.makeText(this, uuid.toString(), Toast.LENGTH_SHORT).show();
           // mBluetoothConnection=new BluetoothConnectionService(LevelsActivity.this,uuid);
          //  mBluetoothConnection.startClient(device,uuid);
          //  mBluetoothConnection.write("1_on".getBytes());
            myBluetooth=BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
             device=myBluetooth.getRemoteDevice(device.getAddress());//connects to the device's address and check if it's available
            try {
                btSocket=device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//create a RFCOMM (SPP) connection
                btSocket.connect();
                Connection connection=new Connection(btSocket);
                thread=new Thread(connection);
                thread.start();
                handler=new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        change(msg.arg1);
                    }
                };
              /*  btSocket.getOutputStream().write("O".getBytes());
                byte[] buffer=new byte[1024];// buffer store for the stream

                int bytes;//bytes returned from read();
                bytes=btSocket.getInputStream().read(buffer);
                String incomingMessage=new String(buffer,0,bytes);*/
               // btSocket.close();
            } catch (IOException e) {
               // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                try {
                    btSocket.close();
                    btSocket=device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//create a RFCOMM (SPP) connection
                    btSocket.connect();
                    Connection connection=new Connection(btSocket);
                    thread=new Thread(connection);
                    thread.start();
                    handler=new Handler(){
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            super.handleMessage(msg);
                            change(msg.arg1);
                        }
                    };
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }else {
          //  Toast.makeText(this, "There is no device to cinnect", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(thread.isAlive()){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void change(int incomingMessage){
        if (incomingMessage==1){
            but_low.setEnabled(true);
            but_low.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.low_on));

            but_mid.setEnabled(false);
            but_mid.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.middle));

            but_high.setEnabled(false);
            but_high.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.high));
        }else if (incomingMessage==2){
            but_mid.setEnabled(true);
            but_mid.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.middle_on));

            but_low.setEnabled(false);
            but_low.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.low));

            but_high.setEnabled(false);
            but_high.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.high));
        }
        else if (incomingMessage==3){
            but_high.setEnabled(true);
            but_high.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.high_on));

            but_low.setEnabled(false);
            but_low.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.low));

            but_mid.setEnabled(false);
            but_mid.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.middle));
        }
    }

    public class Connection extends Thread{

        BluetoothSocket socket;

        public Connection( BluetoothSocket socket){
            this.socket=socket;
         //   Toast.makeText(LevelsActivity.this, "hi", Toast.LENGTH_SHORT).show();
          //  Thread thread = new Thread(this);
          //  thread.start();
        }
        public void run(){

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true){
                try {
                    Log.d(TAG,"run start");
                    byte[] buffer=new byte[1024];// buffer store for the stream
                    int bytes;//bytes returned from read();
                    bytes=socket.getInputStream().read(buffer);
                    final String incomingMessage=new String(buffer,0,bytes);
                    // but_low.setText("hi");
                    Log.d(TAG,incomingMessage);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (incomingMessage.equalsIgnoreCase("a")){
                                but_low.setEnabled(true);
                                but_low.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.low_on));

                                but_mid.setEnabled(false);
                                but_mid.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.middle));

                                but_high.setEnabled(false);
                                but_high.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.high));
                            }else if (incomingMessage.equalsIgnoreCase("b")){
                                but_mid.setEnabled(true);
                                but_mid.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.middle_on));

                                but_low.setEnabled(false);
                                but_low.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.low));

                                but_high.setEnabled(false);
                                but_high.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.high));
                            }
                            else if (incomingMessage.equalsIgnoreCase("c")){
                                but_high.setEnabled(true);
                                but_high.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.high_on));

                                but_low.setEnabled(false);
                                but_low.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.low));

                                but_mid.setEnabled(false);
                                but_mid.setBackgroundDrawable(LevelsActivity.this.getResources().getDrawable(R.drawable.middle));
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
