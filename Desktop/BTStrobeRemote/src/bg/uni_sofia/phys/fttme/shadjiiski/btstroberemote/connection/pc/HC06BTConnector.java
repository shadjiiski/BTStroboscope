/**
 * 
 */
package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.pc;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class HC06BTConnector implements DiscoveryListener
{
//    TODO private static final String RESPONSE_ACKNOWLEDGE = "ACK";
	private static final int RECORD_BYTES = 7;
//	private static final String TARGET_NAME = "HC-06";
	private static final String TARGET_ADDRESS = "301408183021";

	private static Object lock = new Object();
	private LocalDevice localDevice;
	private RemoteDevice remoteDevice;
	private String connectionURL;
	private DiscoveryAgent discoveryAgent;
	private StreamConnection connection;
	private InputStream inputStream;
	private OutputStream outputStream;

    private byte[] buffer;

    public HC06BTConnector() throws IOException
	{
    	buffer = new byte[RECORD_BYTES];
		init();
	}
	
	protected void test() throws IOException
	{
		DataInputStream is = connection.openDataInputStream();
		DataOutputStream os = connection.openDataOutputStream();
		String msg = "Fake here!";
		System.out.println("Sending message: " + msg);
		os.writeUTF(msg);
		System.out.println("Received message: " + is.readUTF());
	}

	private void findDevice() throws BluetoothStateException
	{
		// Asume the client is a pre-knonw (?paired?) device
		System.out.println("Quick peek in pre-known devices list.");
		RemoteDevice[] devices = discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
		if (devices != null)
		{
			for (RemoteDevice d : devices)
			{
				if (isTargetDevice(d))
				{
					remoteDevice = d;
					break;
				}
			}
		}
		// Not preknown? May be already cached?
		if (remoteDevice == null)
		{
			System.out.println("Searching in cached devices list.");
			devices = discoveryAgent.retrieveDevices(DiscoveryAgent.CACHED);
			if (devices != null)
			{
				for (RemoteDevice d : devices)
				{
					if (isTargetDevice(d))
					{
						remoteDevice = d;
						break;
					}
				}
			}
		}
		// Still can't find the remote, so start an inquiry
		if (remoteDevice == null)
		{
			discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
			System.out.println("Device inquiry started.");
			try
			{
				synchronized (lock)
				{
					lock.wait();
				}
			} catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
			System.out.println("Device inquiry completed.");
		}
	}

	private boolean isTargetDevice(RemoteDevice d)
	{
		String name = null;
		try
		{
			name = d.getFriendlyName(false);
		} catch (Exception e){}
		System.out.println("Found device: " + (name == null ? name : name + " (" + d.getBluetoothAddress() + ")"));
		if (d.getBluetoothAddress().equalsIgnoreCase(TARGET_ADDRESS)) // found it
			return true;
		else
			return false;
	}

	private void prepareConnectionUrl() throws BluetoothStateException
	{
		if(remoteDevice == null)
		{
			System.err.println("No remote device.");
			return;
		}
		
		discoveryAgent.searchServices(null, new UUID[]{new UUID(0x1101)}, remoteDevice, this); //Serial port profile
		System.out.println("Searching for Serial Port Profile Service");
		try
		{
			synchronized (lock)
			{
				lock.wait();
			}
		}catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}
		System.out.println("Service searching complete.");
	}
	
	public void init() throws IOException
	{
		localDevice = LocalDevice.getLocalDevice();
		discoveryAgent = localDevice.getDiscoveryAgent();
		findDevice();
		prepareConnectionUrl();
	}
	
	public void connect() throws IOException
	{
		if(connectionURL == null)
		{
			System.out.println("No connection url prepared. Invoking initialisation");
			init();
		}
		
		connection = (StreamConnection) Connector.open(connectionURL);
		inputStream = connection.openInputStream();
		outputStream = connection.openOutputStream();
	}
	
	protected void send(byte... data) throws IOException
	{
		if(outputStream == null)
		{
			System.err.println("No open outputStream");
			return;
		}
		
		outputStream.write(data, 0, data.length);
	}
	
	public void disconnect(long delay) throws IOException
	{
		if(connection == null)
		{
			System.err.println("No open connection");
			return;
		}
		if(delay > 0)
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		if(inputStream != null)
			inputStream.close();
		if(outputStream != null)
			outputStream.close();
		connection.close();
		connection = null;
	}
	
    /**
    *
    * @param frequency min 1.0 Hz, max 100.0 Hz
    * @param duty min 1 % max 99 %
    */
    public void setStrobeParameters(double frequency, int duty){
        if(connection == null){
        	System.err.println("No open connection");
            return;
        }

//      Whole period and light period should be sent in micros. 3 bytes for each. the 7th byte is checksum (the last byte of the one's complement of the sum of the other 6 bytes)
        int wholePeriod = (int) (0.5 + 1000000/frequency); //micros
        int lightPeriod = (int) (0.5 + duty * 10000/frequency); //micros
        System.out.println("Sending wholePeriod of " + wholePeriod + " micros and lightPeriod of " + lightPeriod + " micros");
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
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod)
	{
		if(isTargetDevice(btDevice))
		{
			remoteDevice = btDevice;
			discoveryAgent.cancelInquiry(this);
		}
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord)
	{
		for (int i = 0; i < servRecord.length; i++)
		{
			String url = servRecord[i].getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT, false);
			if(url != null)
			{
				System.out.println("Prepared url: " + url);
				connectionURL = url;
			}
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode)
	{
		synchronized (lock)
		{
			lock.notify();
		}
	}

	@Override
	public void inquiryCompleted(int discType)
	{
		synchronized (lock)
		{
			lock.notify();
		}
	}

	public static void main(String[] args)
	{
		try
		{
			HC06BTConnector connector = new HC06BTConnector();
			connector.connect();
//			connector.test();
			connector.setStrobeParameters(10.0, 1);
			connector.disconnect(1000);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
