package ar.com.lrusso.ardudrone;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.text.Html;

import java.util.Calendar;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Main extends Activity implements JoyStick.JoyStickListener
	{
	private Context 						context;
	private Activity						activity;

	public static EditText					receiverTextbox;
	public static Button					cameraButton;
	public static ImageViewRounded			cameraCapture;
	public static TextView					cameraCaptureStatus;
	public static JoyStick					joyStick1;
	public static JoyStick					joyStick2;
	public static TextView					signalTextbox;
	public static TextView					signalTextboxTitle;
	public static TextView                  counterTextbox;
	public static TextView                  counterTextboxTitle;

	public static BluetoothUtils			bluetooth = null;

	public static boolean 					connected = false;

	public static final Handler 			counterHandler = new Handler();
	public static final int     			counterMaxMinutes = 8;
	public static boolean       			counterEnabled = false;
	public static int           			counter;

	@Override protected void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		context = this;
		activity = this;

		setTitle(getResources().getString(R.string.app_name) + " - " + getResources().getString(R.string.textDisconnected));

		receiverTextbox = (EditText) findViewById(R.id.receiverTextbox);
		cameraButton = (Button) findViewById(R.id.cameraButton);
	    cameraCapture = (ImageViewRounded) findViewById(R.id.cameraCapture);
	    cameraCaptureStatus = (TextView) findViewById(R.id.cameraCaptureStatus);
	    joyStick1 = (JoyStick) findViewById(R.id.joy1);
	    joyStick2 = (JoyStick) findViewById(R.id.joy2);
		signalTextbox = (TextView) findViewById(R.id.signalTextbox);
		signalTextboxTitle = (TextView) findViewById(R.id.signalTextboxTitle);
		counterTextbox = (TextView) findViewById(R.id.counterTextbox);
		counterTextboxTitle = (TextView) findViewById(R.id.counterTextboxTitle);

	    joyStick1.setListener(this);
	    joyStick1.setEnabled(false);
	    joyStick1.setButtonDrawable(R.drawable.joystick1disabled);

	    joyStick2.setListener(this);
	    joyStick2.setEnabled(false);
	    joyStick2.setButtonDrawable(R.drawable.joystick2disabled);
	    joyStick2.setType(JoyStick.TYPE_2_AXIS_UP_DOWN);

	    signalTextboxTitle.setTextColor(Color.parseColor("#C0C0C0"));
		counterTextboxTitle.setTextColor(Color.parseColor("#C0C0C0"));

		cameraButton.setEnabled(false);
		cameraButton.setTextColor(Color.parseColor("#808080"));

	    IntentFilter filter = new IntentFilter();
	    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
	    registerReceiver(mReceiver, filter);

	    // EXAMPLE
	    // cameraCapture.setImageDrawable(getResources().getDrawable(R.drawable.example));
	    }

	public void onDestroy()
		{
		if (bluetooth!=null)
			{
			try
				{
				bluetooth.disconnect();
				}
				catch(Exception e)
				{
				}
			}
		super.onDestroy();
		}

	@Override public boolean onCreateOptionsMenu(Menu menu)
		{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
		}	

	public boolean onOptionsItemSelected(MenuItem item)
		{
		switch (item.getItemId())
			{
			case R.id.action_settings:
			View menuItemView = findViewById(R.id.action_settings);
			PopupMenu popupMenu = new PopupMenu(this, menuItemView);
			popupMenu.inflate(R.menu.popup_menu);

    		Menu popupMenu2 = popupMenu.getMenu();
    		if (connected==true)
    			{
        	    popupMenu2.findItem(R.id.connect).setVisible(false);
    			}
    			else
    			{
            	popupMenu2.findItem(R.id.disconnect).setVisible(false);
    			}

			popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{  
				public boolean onMenuItemClick(MenuItem item)
    				{
					if (item.getTitle().toString().contains(getResources().getString(R.string.textConnect)))
						{
						clickInConnect();
						}
					else if (item.getTitle().toString().contains(getResources().getString(R.string.textDisconnect)))
						{
						clickInDisconnect();
						}
					else if (item.getTitle().toString().contains(getResources().getString(R.string.textPrivacy)))
						{
						clickInPolicy();
						}
	    			else if (item.getTitle().toString().contains(getResources().getString(R.string.textAbout)))
						{
	    				clickInAbout();
						}
					return true;  
    				}  
				});              
			popupMenu.show();
			return true;
			
			default:
			return super.onOptionsItemSelected(item);
			}
		}
	
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
		{
		try
			{
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
				{
				clickInExit();
				return false;
				}
			}
			catch(NullPointerException e)
			{
			}
		return super.onKeyUp(keyCode, event);
		}

	private void clickInExit()
		{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog, int which)
				{
				switch (which)
					{
					case DialogInterface.BUTTON_POSITIVE:
					activity.finish();
					System.exit(0);
					break;
	
					case DialogInterface.BUTTON_NEGATIVE:
					break;
					}
				}
			};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.textExit).setPositiveButton(R.string.textYes, dialogClickListener).setNegativeButton(R.string.textNo, dialogClickListener).show();
		}

	private void clickInConnect()
		{
		if (bluetoothIsEnabled()==true)
			{
			try
				{
				bluetooth = new BluetoothUtils();
				final AlertDialog.Builder singlechoicedialog = new AlertDialog.Builder(context);
				final String[] names = bluetooth.getNames();
				singlechoicedialog.setTitle(getResources().getString(R.string.textPairedDevices));
				singlechoicedialog.setItems(names, new DialogInterface.OnClickListener()
					{
					public void onClick(DialogInterface dialog, int item)
						{
						try
							{
							bluetooth.disconnect();
							}
							catch(Exception e)
							{
							}
						
						boolean connectionResult = false;
						try
							{
							connectionResult = bluetooth.connect(item);
							}
							catch(Exception e)
							{
							}

						if (connectionResult==true)
							{
							dialog.cancel();
							bluetooth.setTargetDeviceName(names[item]);
							Toast.makeText(activity, R.string.textConnected, Toast.LENGTH_SHORT).show();
							activity.setTitle(activity.getString(R.string.app_name) + " - " + activity.getResources().getString(R.string.textConnectedTo) + " " + names[item]);

							connected = true;

							joyStick1.setEnabled(true);
							joyStick1.setButtonDrawable(R.drawable.joystick1enabled);

							joyStick2.setEnabled(true);
							joyStick2.setButtonDrawable(R.drawable.joystick2enabled);

							signalTextboxTitle.setTextColor(Color.parseColor("#FFFF00"));
							counterTextboxTitle.setTextColor(Color.parseColor("#FFFF00"));

							cameraButton.setEnabled(true);
							cameraButton.setTextColor(Color.BLACK);

							startCountDown();
							}
							else
							{
							dialog.cancel();
							bluetooth.setTargetDeviceName("");
							activity.setTitle(activity.getString(R.string.app_name) + " - " + activity.getResources().getString(R.string.textDisconnected));
							connected = false;

							joyStick1.setEnabled(false);
							joyStick1.setButtonDrawable(R.drawable.joystick1disabled);

							joyStick2.setEnabled(false);
							joyStick2.setButtonDrawable(R.drawable.joystick2disabled);

							signalTextboxTitle.setTextColor(Color.parseColor("#C0C0C0"));
							counterTextboxTitle.setTextColor(Color.parseColor("#C0C0C0"));

							cameraButton.setEnabled(false);
							cameraButton.setTextColor(Color.parseColor("#808080"));

							try
								{
								bluetooth.disconnect();
								}
								catch(Exception e)
								{
								}

							AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
							alertDialog.setTitle(activity.getResources().getString(R.string.textMessage));
							alertDialog.setMessage(activity.getResources().getString(R.string.textConnectingError));
							alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.textOK),new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
							alertDialog.show();
							}
						}
					});
				AlertDialog alert_dialog = singlechoicedialog.create();
				alert_dialog.show();
				}
				catch(Exception e)
				{
				}
			}
			else
			{
			AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
			alertDialog.setTitle(activity.getResources().getString(R.string.textMessage));
			alertDialog.setMessage(activity.getResources().getString(R.string.textBluetoothOff));
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.textOK),new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
			alertDialog.show();
			}
		}

	private void clickInDisconnect()
		{
		Toast.makeText(context, context.getResources().getString(R.string.textDisconnected), Toast.LENGTH_LONG).show();

		if (bluetooth!=null)
			{
			try
				{
				bluetooth.disconnect();
				}
				catch(Exception e)
				{
				}
			}
		connected = false;

		joyStick1.setEnabled(false);
		joyStick1.setButtonDrawable(R.drawable.joystick1disabled);

		joyStick2.setEnabled(false);
		joyStick2.setButtonDrawable(R.drawable.joystick2disabled);

		signalTextboxTitle.setTextColor(Color.parseColor("#C0C0C0"));
		counterTextboxTitle.setTextColor(Color.parseColor("#C0C0C0"));

		cameraButton.setEnabled(false);
		cameraButton.setTextColor(Color.parseColor("#808080"));

		setTitle(getResources().getString(R.string.app_name) + " - " + getResources().getString(R.string.textDisconnected));

		counterEnabled = false;
	    counterTextbox.setText("");
	    counterTextbox.setTextColor(Color.BLACK);

	    signalTextbox.setText("");
	    signalTextbox.setTextColor(Color.BLACK);
		}
	
	private void clickInPolicy()
		{
		LayoutInflater inflater = LayoutInflater.from(this);
		View view=inflater.inflate(R.layout.privacy, null);

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);  
		alertDialog.setTitle(getResources().getString(R.string.textPrivacy));  
		alertDialog.setView(view);
		alertDialog.setPositiveButton(getResources().getString(R.string.textOK), new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog, int whichButton)
				{
				}
			});
		alertDialog.show();
		}
	
	private void clickInAbout()
		{
		String anos = "";
		String valor = getResources().getString(R.string.textAboutMessage);
		int lastTwoDigits = Calendar.getInstance().get(Calendar.YEAR) % 100;
		if (lastTwoDigits<=5)
			{
			anos = "2005";
			}
			else
			{
			anos ="2005 - 20" + String.valueOf(lastTwoDigits).trim();
			}
		
		valor = valor.replace("ANOS", anos);
		
		TextView msg = new TextView(this);
		msg.setText(Html.fromHtml(valor));
		msg.setPadding(10, 20, 10, 25);
		msg.setGravity(Gravity.CENTER);
		float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
		float size = new EditText(this).getTextSize() / scaledDensity;
		msg.setTextSize(size);			

		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.textAbout)).setView(msg).setIcon(R.drawable.ic_launcher).setPositiveButton(getResources().getString(R.string.textOK),new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog,int which)
				{
				}
			}).show();
		}
	
	private boolean bluetoothIsEnabled()
		{
		try
			{
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null)
				{
			    // Device does not support Bluetooth
				return false;
				}
			else
				{
				if (mBluetoothAdapter.isEnabled())
					{
					return true;
					}
					else
					{
					return false;
					}
				}
			}
			catch(Exception e)
			{
			}
		return false;
		}	

	// RECEIVER TO KNOW WHEN THE CONNECTION IS LOST/DISCONNECTED WITH THE BLUETOOTH TARGET DEVICE
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
		{
	    @Override public void onReceive(Context context, Intent intent)
	    	{
	    	try
	    		{
		        String action = intent.getAction();
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
		        	{
		        	if (device.getName().equals(bluetooth.getTargetDeviceName()))
		        		{
		        		if (connected==true)
		        			{
			        		clickInDisconnect();
		        			}
		        		}
		        	}
	    		}
	    		catch(Exception e)
	    		{
	    		}
	    	}
		};

	public void myClickHandler(View view)
    	{
		if (view.getId()==R.id.cameraButton)
			{
    		
			}
    	}
	
    @Override public void onMove(JoyStick joyStick, double angle, double power, int direction)
    	{
        switch (joyStick.getId())
        	{
            case R.id.joy1:
            detectMovementJoy1(joyStick.getDirection());
            break;
            
            case R.id.joy2:
            detectMovementJoy2(joyStick.getDirection());
            break;
        	}
    	}
    
    private void detectMovementJoy1(int value)
    	{
    	if (value==JoyStick.DIRECTION_UP)
    		{
    		cameraCaptureStatus.setText("ADELANTE");
    		bluetooth.send("W");
    		}
    	else if (value==JoyStick.DIRECTION_UP_RIGHT)
			{
    		cameraCaptureStatus.setText("ADELANTE-DERECHA");
    		bluetooth.send("E");
			}
    	else if (value==JoyStick.DIRECTION_UP_LEFT)
			{
    		cameraCaptureStatus.setText("ADELANTE-IZQUIERDA");
    		bluetooth.send("Q");
			}
    	else if (value==JoyStick.DIRECTION_DOWN)
			{
    		cameraCaptureStatus.setText("ATRÁS");
			bluetooth.send("X");
			}
    	else if (value==JoyStick.DIRECTION_DOWN_LEFT)
			{
    		cameraCaptureStatus.setText("ATRÁS-IZQUIERDA");
    		bluetooth.send("Z");
			}
    	else if (value==JoyStick.DIRECTION_DOWN_RIGHT)
			{
    		cameraCaptureStatus.setText("ATRÁS-DERECHA");
    		bluetooth.send("C");
			}
    	else if (value==JoyStick.DIRECTION_LEFT)
			{
    		cameraCaptureStatus.setText("IZQUIERDA");
    		bluetooth.send("A");
			}
    	else if (value==JoyStick.DIRECTION_RIGHT)
			{
    		cameraCaptureStatus.setText("DERECHA");
    		bluetooth.send("D");
			}
    	else
    		{
    		cameraCaptureStatus.setText("");
    		}
    	}
    
    private void detectMovementJoy2(int value)
    	{
    	if (value==JoyStick.DIRECTION_UP)
    		{
    		cameraCaptureStatus.setText("SUBIR");
    		bluetooth.send("U");
    		}
    	else if (value==JoyStick.DIRECTION_DOWN)
			{
    		cameraCaptureStatus.setText("BAJAR");
    		bluetooth.send("J");
			}
    	else
    		{
    		cameraCaptureStatus.setText("");
    		}
    	}
    
    private void startCountDown()
    	{
    	try
    		{
    		counterEnabled = true;
    	    counter = counterMaxMinutes * 60;
    	    counterTextbox.setText("");
    	    counterTextbox.setTextColor(Color.YELLOW);

            final Runnable runnable = new Runnable()
            	{
                public void run()
                	{ 
                	if (counterEnabled == true)
                		{
                		counter = counter - 1;
                		if (counter>=0)
                			{
    	            		if (counter==60)
    	            			{
    	            			counterTextbox.setTextColor(Color.RED);
    	            			Toast.makeText(activity, activity.getResources().getString(R.string.textFlyTimeWarning), Toast.LENGTH_LONG).show();
    	            			}
    	            		counterTextbox.setText(getDurationString(counter));
                			}
                			else
                			{
                			counterEnabled = false;
                			clickInDisconnect();
                			}
                		}
                	if (counterEnabled == true)
                		{
                		counterHandler.postDelayed(this, 1000);
                		}
                	}
            	};
            counterHandler.post(runnable);
    		}
    		catch(Exception e)
    		{
    		}
        }
    
    private String getDurationString(int seconds)
    	{
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return twoDigitString(minutes) + " : " + twoDigitString(seconds);
    	}

    private String twoDigitString(int number)
    	{
        if (number == 0)
        	{
            return "00";
        	}
        if (number / 10 == 0)
        	{
            return "0" + number;
        	}
        return String.valueOf(number);
    	}
	}