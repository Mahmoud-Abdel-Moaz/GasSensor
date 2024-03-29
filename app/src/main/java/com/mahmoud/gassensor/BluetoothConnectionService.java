package com.mahmoud.gassensor;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService  {

    private static final String TAG="BluetoothConnectionServ";

    private static final String appName="MYAPP";

    private static  UUID MY_UUID_INSECURE=UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdupter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context,UUID uuid) {
        MY_UUID_INSECURE=uuid;
        this.mContext = context;
        mBluetoothAdupter=BluetoothAdapter.getDefaultAdapter();

        start();
    }


    //Thread waiting for connection
    private class AcceptThread extends Thread {
        // The Local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp=null;
            // Create a new listing server socket
            try {
                tmp=mBluetoothAdupter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);
                Log.d(TAG,"AcceptThread: Setting up Server using"+MY_UUID_INSECURE);

            }catch (IOException e){
                Log.e(TAG,"AcceptThread: IOException: "+e.getMessage());
            }
            mmServerSocket=tmp;

        }

        public void run(){
            Log.d(TAG,"run: AcceptThread Running.");

            BluetoothSocket socket=null;

            try {
                //this is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG,"run:RFCOM server socket start...");

                socket=mmServerSocket.accept();

                Log.d(TAG,"run: RFCOM server socket accept connection...");
            }catch (IOException e){
                Log.e(TAG,"AcceptThread: IOException: "+e.getMessage());
            }

            if (socket != null) {
                connected(socket,mmDevice);
            }

            Log.i(TAG,"End mAcceptThread");
        }

        public void cancel(){
            Log.d(TAG,"cancel: Canceling AcceptThread");
            try {
                mmServerSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel: Close of AcceptThread ServerSocket failed"+e.getMessage());
            }
        }
    }



    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device,UUID uuid){
            Log.d(TAG,"ConnectThread:Started.");
            mmDevice=device;
            deviceUUID=uuid;
        }

        public void run(){
            BluetoothSocket tmp=null;
            Log.i(TAG,"RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG,"ConnectThread: Trying to create InsecureRfcommSocket using UUID:" +
                        MY_UUID_INSECURE);
                tmp=mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG,"ConnectThread: Could not create InsecureRfcommSocket"+e.getMessage());
            }
            mmSocket=tmp;

            //Always cancel discovery because it will slow down connection
            mBluetoothAdupter.cancelDiscovery();

            // Make a connection to the BluetoothSocket


            try {
                //this is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                Log.d(TAG,"run:ConnectThread connected");
                Toast.makeText(mContext, "run:ConnectThread connected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                //Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG,"run: Closed Socket.");
                } catch (IOException ex) {
                    Log.e(TAG,"mConnectThread: run: Unable to close  connection in socket "+ex.getMessage());

                }
                Log.d(TAG,"run: ConnectThread: Could not  connect to UUID: "+MY_UUID_INSECURE);
            }

            connected(mmSocket,mmDevice);

        }
        public void cancel(){
            Log.d(TAG,"cancel: Closing Client Socket.");
            try {
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel: close() of mmSocket in ConnectThread failed."+e.getMessage());
            }
        }
    }

    /*
    * Start the chat service. Specifically start AccepThread to begin a
    * session in listening (server) mode. Called by the Activity on Resume()
    * */



    public synchronized void start(){
        Log.d(TAG,"start");

        //Cancel any thread attempting to make a connection
        if (mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread=null;
        }
        if (mInsecureAcceptThread==null){
            mInsecureAcceptThread=new AcceptThread();
            //.start using to start the thread
            mInsecureAcceptThread.start();
        }
    }

    /*
    * AcceptThread starts and sit waiting for connection.
    * Then ConnectThread starts and attempts to mack a connection  with the other devices AcceptThread.
    * */

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG,"startClient: Started.");

        //initprogress dialog
        mProgressDialog=ProgressDialog.show(mContext,"Connectiog Bluetooth","Please Wait...",true);

        mConnectThread=new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection , Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/

    private class ConnectedThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private  final OutputStream mmOutStream;

        public  ConnectedThread(BluetoothSocket socket){
            Log.d(TAG,"ConnectedThread: Starting");
            mmSocket=socket;
            InputStream tmpIn=null;
            OutputStream tmpOut=null;

            //dismiss the progressDialog when connection is established
            try {
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn=mmSocket.getInputStream();
                tmpOut=mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream=tmpIn;
            mmOutStream=tmpOut;

        }

        public void run(){
            byte[] buffer=new byte[1024];// buffer store for the stream

            int bytes;//bytes returned from read();

            //Keep listening to the InputStream until on exception occurs
            while (true){
                //Read from the InputStream
                try {
                    bytes=mmInStream.read(buffer);
                    String incomingMessage=new String(buffer,0,bytes);
                    Log.d(TAG,"InputStream: "+incomingMessage);

                    Intent incomingMessageIntnt = new Intent("incomingMessage");
                    incomingMessageIntnt.putExtra("theMessage",incomingMessage);


                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntnt);
                } catch (IOException e) {
                    Log.e(TAG,"write: Error reading to inputStream. "+e.getMessage());
                    break;
                }

            }
        }

        //Call this from main activity to send data to the remote device
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"write: Writing to outputStream: "+text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG,"write: Error writing to outputStream. "+e.getMessage());
            }

        }

        // Call this from the main activity to shutdown the connection
        public void cancel(){
            try {
                mmSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private void connected(BluetoothSocket socket, BluetoothDevice mmDevice) {
        Log.d(TAG,"connected: Starting");

        //Start the thread to manage the connection and perrform transmissions
        mConnectedThread=new ConnectedThread(socket);
        mConnectedThread.start();
    }

    /**
     * write to the ConnectedThread in an unsynchroized manned
     *
     */
    public void write(byte[] out){
        //Create temporary object
        ConnectThread r;

        // Synchranize a copy pf the ConnectedThread
        Log.d(TAG,"write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }
}
