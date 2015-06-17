package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by shadjiiski on 28.03.15.
 */
public class BTConnectionHandler implements Runnable {
    public static final String TAG = "BTConnectionHandler";

    private int maxConnectRetries;

    private String moduleUUID;
    private String clientMac;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private List<ConnectionStateListener> stateListeners;
    private ConnectionState currentState;

    public BTConnectionHandler(Context context, String moduleUUID, String clientMac, boolean autoConnect, int maxConnectRetries){
        this.moduleUUID = moduleUUID;
        this.clientMac = clientMac;
        this.btAdapter = ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        this.btSocket = null;
        currentState = ConnectionState.DISCONNECTED;
        stateListeners = new ArrayList<ConnectionStateListener>();
        this.maxConnectRetries = maxConnectRetries;

        if(autoConnect)
            connect();
    }

    public boolean addConnectionStateListener(ConnectionStateListener l){
        return l != null && stateListeners.add(l);
    }

    public boolean removeConnectionStateListener(ConnectionStateListener l){
        return l != null && stateListeners.remove(l);
    }

    public ConnectionStateListener[] getConnectionStateListeners(){
        return stateListeners.toArray(new ConnectionStateListener[stateListeners.size()]);
    }

    public void send(byte... data) throws IOException {
        if(currentState != ConnectionState.CONNECTED){
            Log.d(TAG, "Can't send data, must be connected, but currentState = " + currentState.toString());
            return;
        }

        if(btSocket == null || !btSocket.isConnected()){
            Log.d(TAG, "Can't send data, no open RFCOMM socket is present");
            return;
        }

        OutputStream os = null;
        os = btSocket.getOutputStream();
        os.write(data);
        os.flush();
    }
    public int read() throws IOException {
        if(currentState != ConnectionState.CONNECTED){
            Log.d(TAG, "Can't read data, must be connected, but currentState = " + currentState.toString());
            return -1;
        }

        if(btSocket == null || !btSocket.isConnected()){
            Log.d(TAG, "Can't read data, no open RFCOMM socket is present");
            return -1;
        }

        return btSocket.getInputStream().read();
    }

    public int read(byte[] b) throws IOException {
        if(currentState != ConnectionState.CONNECTED){
            Log.d(TAG, "Can't read data, must be connected, but currentState = " + currentState.toString());
            return -1;
        }

        if(btSocket == null || !btSocket.isConnected()){
            Log.d(TAG, "Can't read data, no open RFCOMM socket is present");
            return -1;
        }
        return btSocket.getInputStream().read(b);
    }

    public int available() throws IOException {
        return btSocket.getInputStream().available();
    }

    public void connect(){
        if(currentState != ConnectionState.DISCONNECTED) {
            Log.d(TAG, "Already connected, first disconnect");
            return;
        }

        if(btAdapter == null || clientMac == null || moduleUUID == null) {
            String msg = "Null objects prevent connection(";
            if(btAdapter == null) msg += " btAdapter ";
            if(clientMac == null) msg += " clientMac ";
            if(moduleUUID == null) msg += " moduleUUID ";
            msg += ")";
            Log.d(TAG, msg);
            return;
        }
        while (btSocket != null && btSocket.isConnected()) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close previously opened socket", e);
            }
        }

        if(btAdapter.isDiscovering())
            btAdapter.cancelDiscovery();

        changeState(ConnectionState.CONNECTING);
        new Thread(this).start();
    }

    public void disconnect(){
        if(currentState == ConnectionState.DISCONNECTED){
            Log.d(TAG, "Can't disconnect, you have to connect first");
            return;
        }

        //currentState is CONNECTED or CONNECTING. Disconnect / abort
        if(btSocket != null){
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close socket", e);
            }
        }
        btSocket = null;
        changeState(ConnectionState.DISCONNECTED);
    }

    @Override
    public void run() {
        Log.d(TAG, "Trying to establish connection");
        BluetoothDevice client = btAdapter.getRemoteDevice(clientMac);
        btSocket = null;
        try{
            btSocket = client.createInsecureRfcommSocketToServiceRecord(UUID.fromString(moduleUUID));
        }catch (IOException e){
            Log.e(TAG, "Could not open RFCOMM socket", e);
        }

        int attempt = 0;
        while (btSocket != null && !btSocket.isConnected() && attempt++ <= maxConnectRetries){
            try {
                btSocket.connect();
            } catch (IOException e) {
                String msg = "Could not connect socket. " + (attempt <= maxConnectRetries ? "will retry in a second." : "Aborting.");
                Log.e(TAG, msg, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Log.e(TAG, "Interrupted while waiting to retry connection", ex);
                }
            }
        }

        if(btSocket != null && btSocket.isConnected())
            changeState(ConnectionState.CONNECTED);
        else
            changeState(ConnectionState.DISCONNECTED);
    }

    public ConnectionState getCurrentState(){
        return currentState;
    }

    private void changeState(ConnectionState targetState){
        ConnectionState oldState = currentState;
        currentState = targetState;
        Log.d(TAG, "Connection state changed: " + oldState.toString() + " -> " + currentState.toString());
        for(ConnectionStateListener l: stateListeners)
            l.connectionStateChanged(this, oldState, currentState);
    }
}
