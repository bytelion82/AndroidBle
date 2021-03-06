package com.example.TLQ;
//Author:TOOSAKA
//MADE WITH LQresier
//do not copy
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



public class  MainActivity extends Activity {
    public static int VAL_ACC_X = 0;
    public static int VAL_ACC_Y = 0;
    public static int VAL_ACC_Z = 0;
    public static int VAL_GYR_X = 0;
    public static int VAL_GYR_Y = 0;
    public static int VAL_GYR_Z = 0;
    public static float VAL_ANG_X = 0;
    public static float VAL_ANG_Y = 0;
    public static float VAL_ANG_Z = 0;

    public static final int MESSAGE_STATIC_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME =4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothRfcommClient  mRfcommClient = null;

    private TextView mStatus;
    private EditText name;
    private EditText password;

/////////////////////////////////onCreate/////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);                         //????????????
        this.getWindow().setNavigationBarColor(0x9966CCFF);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,                       //???????????????
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStatus = (TextView) findViewById(R.id.message);                                            //?????????????????????textview
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();                                   //bluetooth adapter
        name =(EditText) findViewById(R.id.name);

        password =(EditText) findViewById(R.id.password);
        if(mBluetoothAdapter == null){                                                              //??????bluetooth????????????
            Toast.makeText(this, "Bluetooth Wrong!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if(!mBluetoothAdapter.isEnabled()){                                                         //??????????????????????????????
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);               //?????????????????????????????????
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        mRfcommClient = new BluetoothRfcommClient(this,mHandler);

        ImageButton mImageButton = (ImageButton) findViewById(R.id.bluetooth);                      //??????????????????????????????????????????
        mImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MyBluetooth.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);

            }
        });

        mImageButton = (ImageButton) findViewById(R.id.control);                                    //??????control?????????????????????
        mImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, TsControl.class);
//                startActivity(intent);
                sendData();
            }
        });

    }
    public void sendData(){


        name=(EditText) findViewById(R.id.name);
        password=(EditText)findViewById(R.id.password);
        String wifi = name.getText().toString();
        String pas = password.getText().toString();
        if (!wifi.equals("name")&&!pas.equals("password")){
            byte[] bytes1 = (wifi+":"+pas).getBytes();
            String s = Bytes2hexStr(bytes1);
            MainActivity.SendData_Byte(bytes1);
            Toast.makeText(this,wifi+pas+ "????????????="+s, Toast.LENGTH_SHORT).show();
        }



    }

    /**
     * bytes??????????????????????????????
     *
     * @param b byte??????
     */
    public  String Bytes2hexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");

        for (int i = 0; i < b.length; i++) {
            stmp = Integer.toHexString(b[i] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            // sb.append(" ");//??????Byte?????????????????????
        }
        return sb.toString().toUpperCase().trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public synchronized void onResume(){                                                            //resume?????????????????????

        super.onResume();
        if(mRfcommClient != null){
            if(mRfcommClient.getState()==BluetoothRfcommClient.STATE_NONE ){
                //????????????
                mRfcommClient.start();
            }
        }
    }

    @Override
    public void onDestroy() {                                                                       //destroy?????????
        if (mRfcommClient != null) mRfcommClient.stop();
        super.onDestroy();
    }


////////////////////////////////////////???????????????///////////////////////////////////////////////////
    @Override                                                                                       //?????????UI
    public void onBackPressed(){
        Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("TOOSAKA")
                .setMessage("close the BLT?");

        mBuilder.setPositiveButton("sure", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        mBuilder.setNegativeButton("no", null).show();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){                                               //?????????????????????
                    String address = data.getExtras().getString(MyBluetooth.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mRfcommClient.connect(device);

                }
                break;

            case REQUEST_ENABLE_BT:
                if(resultCode != Activity.RESULT_OK){                                               //??????????????????
                    Toast.makeText(this, "Connect Fail...", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message mMessage){

            switch(mMessage.what){

                case MESSAGE_STATIC_CHANGE:

                    switch(mMessage.arg1){//???arg1???arg2?????????????????????????????????                        //????????????
                        case BluetoothRfcommClient.STATE_CONNECTED:                                 //????????????????????????connected to ?????????
                            mStatus.setText("Connected To ");
                            mStatus.append("" + mConnectedDeviceName);
                            break;

                        case BluetoothRfcommClient.STATE_CONNECTING:                                //???????????????????????????connecting
                            mStatus.setText("Connecting...");
                            break;

                        case BluetoothRfcommClient.STATE_NONE:                                      //????????????????????????fail
                            mStatus.setText("Connect Fail...");
                            break;

                    }
                    break;

                case MESSAGE_READ:                                                                  //
                    byte[] mRead = (byte[]) mMessage.obj;
                    DataAnl(mRead , mMessage.arg1);
                    break;

                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = mMessage.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected:"+mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), mMessage.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    static void SendData(String message){                                                           //SendData?????????????????????????????? //used to be static
        if(mRfcommClient.getState()!= BluetoothRfcommClient.STATE_CONNECTED){                       //?????????????????????
//            Looper.prepare();
//// ????????????UI??????
//            Toast.makeText(mContext, "text", Toast.LENGTH_SHORT).show();
//            Looper.loop();
            return;
        }

        if(message.length() >0 ){                                                                   //message????????????0???????????????
            byte[] send = message.getBytes();
            mRfcommClient.write(send);
        }
    }



    static void SendData_Byte(byte[] data){                                                         //????????????????????????
        if(mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {

            return;
        }

        mRfcommClient.write(data);
    }



    static void Send_Command(byte data){                                                            //????????????????????????
        byte[] bytes = new byte[6];
        byte sum = 0;

        if(mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED)
            return;

        bytes[0] = (byte)0xaa;
        bytes[1] = (byte)0xaf;
        bytes[2] = (byte)0x01;
        bytes[3] = (byte)0x01;
        bytes[4] = (byte)data;

        for(int i=0; i<5; i++)
            sum += bytes[i];

        bytes[5] = sum;
        SendData_Byte(bytes);

    }
    static int Buffer_Length = 1000;
    static byte[] Read = new byte[Buffer_Length];
    static int ReadLength = 0;//?????????????????????
    static int ReadState = 0;//?????????????????????
    static int ReadCount = 0;//??????

////////////////////////////////////////////////////////////////////////////////////////////////////
    static void DataAnl(byte[] data, int length){                                                   //????????????????????????  //work point 4 me
        for(int i=0; i<length; i++){
            //????????????AA
            if(ReadState == 0){
                if(data[i] == (byte)0xaa){
                    ReadState = 1;
                    Read[0] = (byte)0xaa;
                }
            }

            //????????????AA
            else if(ReadState == 1){
                if(data[i] == (byte)0xaa){
                    ReadState = 2;
                    Read[1] = (byte)0xaa;
                }
                else
                    ReadState = 0;
            }

            else if(ReadState == 2){
                ReadState = 3;
                Read[2] = data[i];
            }

            else if(ReadState == 3){
                if(data[i] > 45)
                    ReadState = 0;
                else{
                    ReadState = 4;
                    Read[3] = data[i];
                    ReadLength = data[i];
                    if(ReadLength < 0)
                        ReadLength = -ReadLength;
                    ReadCount = 4;
                }
            }

            else if(ReadState == 4){
                ReadLength--;
                Read[ReadCount] = data[i];
                ReadCount++;
                if(ReadLength <= 0)
                    ReadState = 5;
            }

            else if(ReadState == 5){
                Read[ReadCount] = data[i];
                if(ReadCount <= (Buffer_Length-1))
                    FrameAnl(ReadCount+1);
                ReadState = 0;
            }
        }


    }
////////////////////////////////////////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////////////////////////////////////////
    static void FrameAnl(int length){                                                               //???????????????????????????????????????
        byte sum = 0;
        for(int i=0; i<(length-1); i++)
            sum += Read[i];
        if(sum==Read[length-1])//?????????????????????
        {

            if(Read[2]==1)//??????????????????
            {
                VAL_ANG_X = ((float)(BytetoUint(4)))/100;
                VAL_ANG_Y = ((float)(BytetoUint(6)))/100;
                VAL_ANG_Z = ((float)(BytetoUint(8)))/100;
            }
            if(Read[2]==2)//??????????????????????????????
            {
                VAL_ACC_X = BytetoUint(4);
                VAL_ACC_Y = BytetoUint(6);
                VAL_ACC_Z = BytetoUint(8);
                VAL_GYR_X = BytetoUint(10);
                VAL_GYR_Y = BytetoUint(12);
                VAL_GYR_Z = BytetoUint(14);
            }

        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



    static short BytetoUint(int count)
    {
        short r = 0;
        r <<= 8;  //r??????8???
        r |= (Read[count] & 0x00ff);
        r <<= 8;
        r |= (Read[count+1] & 0x00ff);
        return r;
    }

    static int CheckTheBluetooth(){
        if(mRfcommClient.getState()!= BluetoothRfcommClient.STATE_CONNECTED){
            return 1;
        }
        else{
            return 0;
        }
    }



    private byte[] toByteArray(String arg) {
        if (arg != null) {
			/* 1.??????????????String????????' '??????????????String??????????char???????????? */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
			/* ??????char?????????????????????????????????????????????????????????????????????????? */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
				/* ?????? ????????char???????????????????????????????????????16???????????????????????? */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }

    /**
     * ??????String??????????byte[]????????????
     * @param arg
     *            ??????????????????????String????????????
     * @return ?????????????????byte[]????????????
     */
    private byte[] toByteArray2(String arg) {
        if (arg != null) {
			/* 1.??????????????String????????' '??????????????String??????????char???????????? */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte)NewArray[i];
            }
            return byteArray;

        }
        return new byte[] {};
    }
}
