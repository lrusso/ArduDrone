package ar.com.lrusso.ardudrone;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

public class BluetoothUtils
	{
	private static final String UUID_CODE = "00001101-0000-1000-8000-00805F9B34FB";
	private ArrayList<BluetoothDevice> devices;
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;
	private BluetoothUtilsThread bluetoothThread;
	private String targetDeviceName = "";
	
	public BluetoothUtils()
		{
		devices = new ArrayList<BluetoothDevice>();
		adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null)
			{
			return;
			}
			
		for (BluetoothDevice d : adapter.getBondedDevices())
			{
			devices.add(d);
			}
		}
	
	public String[] getNames()
		{
		String names[] = new String[devices.size()];
		for (int i = 0; i < devices.size(); i++)
			{
			names[i] = devices.get(i).getName();
			}
		return names;
		}

	public boolean connect(int index)
		{
		if (index < 0 || index >= devices.size())
			{
			return false;
			}
		
		try
			{
			BluetoothDevice device = devices.get(index);
			socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUID_CODE));
			socket.connect();
			
			bluetoothThread = new BluetoothUtilsThread(socket);
			bluetoothThread.start();
			return true;
			}
			catch (NullPointerException e)
			{
			return false;
			}
			catch (IOException e)
			{
			return false;
			}
			catch (Exception e)
			{
			return false;
			}
		}
	
	public void disconnect()
		{
		try
			{
			socket.close();
			}
			catch (Exception e)
			{
			}
		try
			{
			bluetoothThread.stop();
			}
			catch(Exception e)
			{
			}
		}

	public boolean isConnected()
		{
		if (socket == null)
			{
			return false;
			}
		return socket.isConnected();
		}

	public void send(String value)
		{
		if (socket == null)
			{
			return;
			}
		try
			{ 
			socket.getOutputStream().write(value.getBytes());
			}
			catch (NullPointerException e)
			{
			}
			catch (IOException e)
			{
			}
			catch (Exception e)
			{
			}
		}
	
	public String getTargetDeviceName()
		{
		return targetDeviceName;
		}

	public void setTargetDeviceName(String targetDeviceName)
		{
		this.targetDeviceName = targetDeviceName;
		}
	}

final class BluetoothUtilsThread extends Thread
	{
	private static final int MESSAGE_READ = 123;
	private final InputStream mmInStream;

	public BluetoothUtilsThread(BluetoothSocket socket)
		{
		InputStream tmpIn = null;
		try
    		{
			tmpIn = socket.getInputStream();
    		}
			catch (Exception e)
			{
			}
		mmInStream = tmpIn;
		}

	public void run()
		{
		byte[] buffer = new byte[1024];
		int bytes;
    
		while (true)
    		{
			try
        		{
				bytes = mmInStream.read(buffer);
				mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
        		}
        		catch(Exception e)
        		{
        		}
    		}
		}

	private static Handler mHandler = new Handler()
 		{
		@Override public void handleMessage(Message msg)
			{
			switch (msg.what)
        		{
        		case MESSAGE_READ:
        		byte[] readBuf = (byte[]) msg.obj;
        		String readMessage = new String(readBuf, 0, msg.arg1);
        		if (readMessage.length()>0)
	        		{
        			Main.receiverTextbox.append(readMessage);
	        		}
        		break;
        		}
			}
 		};
	}