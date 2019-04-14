package infinitedreams.shanjinur.sipo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class sensordata extends Activity {
    AtomicBoolean atomicBoolean ;
    String address = null;
    private ProgressDialog progress;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private Button calset,measure;
    private Button start,reset,pause ;
    private EditText calval ;
    private Typeface typeface ;
    private Button discon ;
    private TextView stopwatch ,sensorvalue;
    boolean sus ;
    boolean measured ;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected void onCreate(Bundle savedInstanceState) {
        Context context = this ;
        super.onCreate(savedInstanceState);
        handler = new Handler() ;
        setContentView(R.layout.sensordata_layout);

        AssetManager am = context.getApplicationContext().getAssets();
        handler = new Handler() ;
        atomicBoolean = new AtomicBoolean(false) ;

        typeface = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "font.ttf"));
        Intent newint = getIntent();
        address = newint.getStringExtra(devicelist.EXTRA_ADDRESS); //receive the address of the bluetooth device
        calset = findViewById(R.id.calset) ;
        calset.setTypeface(typeface);
        calval = findViewById(R.id.calval) ;
        calval.setTypeface(typeface);
        measure = findViewById(R.id.measure) ;
        measure.setTypeface(typeface);
        discon = findViewById(R.id.disconnect) ;
        discon.setTypeface(typeface);
        stopwatch = findViewById(R.id.tvTimer) ;
        stopwatch.setTypeface(typeface);
        sensorvalue = findViewById(R.id.sensorvalue) ;
        sensorvalue.setTypeface(typeface);
        start = findViewById(R.id.start) ;
        start.setTypeface(typeface);
        pause = findViewById(R.id.btPause) ;
        pause.setTypeface(typeface);
        reset = findViewById(R.id.btReset) ;
        reset.setTypeface(typeface);
        sus = false ;
        new ConnectBT().execute(); //Call the class to connect
        measured = false ;
        start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);
                reset.setEnabled(false);
                HandlerThread handlerThread = new HandlerThread("") ;
                handlerThread.start();
                Handler h = new Handler(handlerThread.getLooper()) ;
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        while (true){
                            int soun = 0 ;
                            int c=0;
                            if (btSocket!=null)
                            {
                                try
                                {
                                    measured = true ;
                                    btSocket.getOutputStream().write("1".getBytes()) ;
                                    int b1 = btSocket.getInputStream().read() ;
                                    int b2 = btSocket.getInputStream().read() ;
                                    soun = b2 + b1*256 ;
                                    Toast.makeText(getApplicationContext(),String.valueOf(soun),Toast.LENGTH_SHORT).show();
                                    System.out.println(soun);
                                    final int finalSoun = soun ;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sensorvalue.setText(String.valueOf(finalSoun+new Random().nextInt(500)));
                                        }
                                    });
                                }
                                catch (IOException e)
                                {
                                    msg("Error");
                                }
                            }
                        }
                    }
                },0) ;
            }
        });
    }

    public void getSensorData(View view)
    {
        int soun = 0 ;
        if (btSocket!=null)
        {
            try
            {
                measured = true ;
                btSocket.getOutputStream().write("1".getBytes()) ;
                int b1 = btSocket.getInputStream().read() ;
                System.out.println(b1);
                int b2 = btSocket.getInputStream().read() ;
                System.out.println(b2);
                soun = b2 + b1*256 ;
                sensorvalue.setText(String.valueOf(soun)) ;
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    public void msg(String s)
    {
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCalset(View view) {
        int soun = 0 ;
        if (btSocket!=null)
        {
            try
            {
                measured = true ;
                String s = calval.getText().toString() + "\n" ;
                btSocket.getOutputStream().write("2".getBytes()) ;
                btSocket.getOutputStream().write(s.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }


    public void reset(View view) {
        MillisecondTime = 0L ;
        StartTime = 0L ;
        TimeBuff = 0L ;
        UpdateTime = 0L ;
        Seconds = 0 ;
        Minutes = 0 ;
        MilliSeconds = 0 ;
        measured = false ;
        stopwatch.setText("00:00:00");
        sensorvalue.setText("");
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    public void setPause(View view) {
        TimeBuff += MillisecondTime;
        handler.removeCallbacks(runnable);
        reset.setEnabled(true);
    }

    public void disconnect(View view) {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            stopwatch.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(sensordata.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        private void msg(String s)
        {
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}


