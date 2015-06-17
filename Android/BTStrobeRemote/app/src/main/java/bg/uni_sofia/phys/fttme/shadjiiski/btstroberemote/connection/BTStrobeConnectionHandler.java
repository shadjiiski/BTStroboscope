package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by shadjiiski on 28.03.15.
 */
public class BTStrobeConnectionHandler extends BTConnectionHandler {

    public static final String TAG = "BTStrobeConnHandler";
    private static final String DEFAULT_MODULE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final String DEFAULT_CLIENT_MAC = "30:14:08:18:30:21";
    private static final String RESPONSE_ACKNOWLEDGE = "ACK";
    private static final boolean DEFAULT_AUTO_CONNECT = true;
    private static final int DEFAULT_MAX_CONNECT_RETRIES = 5;
    private static final int RECORD_BYTES = 7;

    private Context context;
    private byte[] buffer;
    private Runnable checkResponse = new Runnable() {
        @Override
        public void run() {
            byte[] resp = new byte[3];
            int read, idx = 0;
            try {
                while(idx < 3 && (read = read()) != -1)
                    resp[idx++] = (byte) read;
                final String result = new String(new char[]{(char) resp[0],(char) resp[1],(char) resp[2]});
                if(!result.equalsIgnoreCase(RESPONSE_ACKNOWLEDGE)){
                    Log.d(TAG, "Client response is not " + RESPONSE_ACKNOWLEDGE + ", " + result + " was received");
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Client response: " + result, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not read client response over RFCOMM socket", e);
            }
        }
    };

    public BTStrobeConnectionHandler(Context context) {
        this(context, DEFAULT_MODULE_UUID, DEFAULT_CLIENT_MAC, DEFAULT_AUTO_CONNECT, DEFAULT_MAX_CONNECT_RETRIES);
    }

    public BTStrobeConnectionHandler(Context context, boolean autoConnect) {
        this(context, DEFAULT_MODULE_UUID, DEFAULT_CLIENT_MAC, autoConnect, DEFAULT_MAX_CONNECT_RETRIES);
    }

    public BTStrobeConnectionHandler(Context context, String moduleUUID, String clientMac, boolean autoConnect, int maxConnectRetries) {
        super(context, moduleUUID, clientMac, autoConnect, maxConnectRetries);
        buffer = new byte[RECORD_BYTES];
        this.context = context;
    }

    /**
     *
     * @param frequency min 1.0 Hz, max 100.0 Hz
     * @param duty min 1 % max 99 %
     */
    public void setStrobeParameters(double frequency, int duty){
        if(getCurrentState() != ConnectionState.CONNECTED){
            Log.d(TAG, "State is " + getCurrentState().toString() + ". Data can't be sent");
            Toast.makeText(context, "You are not connected", Toast.LENGTH_SHORT).show();
            return;
        }

//      Whole period and light period should be sent in micros. 3 bytes for each. the 7th byte is checksum (the last byte of the one's complement of the sum of the other 6 bytes)
        int wholePeriod = (int) (0.5 + 1000000/frequency); //micros
        int lightPeriod = (int) (0.5 + duty * 10000/frequency); //micros
        Log.d(TAG, "Sending wholePeriod of " + wholePeriod + " micros and lightPeriod of " + lightPeriod + " micros");
        int check = 0;
        int tmp = 0;

        for (int i = 0; i < 3; i++) {
            tmp = wholePeriod & 0xFF;
            wholePeriod >>= 8;
            buffer[2-i] = (byte) tmp;
            check += tmp;

            tmp =  lightPeriod & 0xFF;
            lightPeriod >>= 8;
            buffer[5-i] = (byte) tmp;
            check += tmp;
        }
        buffer[6] = (byte)(0xFF & (~check));

        try {
            send(buffer);
            new Thread(checkResponse).start();
        } catch (IOException e) {
            Log.e(TAG, "Could not send strobe parameters", e);
            Toast.makeText(context, "Error. Params not set.", Toast.LENGTH_SHORT).show();
        }
    }
}
