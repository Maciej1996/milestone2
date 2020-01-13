package com.example.customkeyboard;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static androidx.core.app.ActivityCompat.startActivityForResult;


public class KeyboardServiceIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private Keyboard mKeyBoard;
    private KeyboardView mKeyView;

    BluetoothAdapter bluetoothAdapter;
    SendReceive sendReceive;


    static  final  int STATE_CONNECTING=2;
    static  final  int STATE_CONNECTED=3;
    static  final  int STATE_CONNECTION_FAILED=4;
    static  final  int STATE_MESSAGE_RECIVED=5;

    int REQUEST_ENABLE_BLUETOOTH=1;

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("b43ae580-f8f4-4128-a193-45f1afb8044c");


    public View onCreateInputView() {

        mKeyView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        mKeyBoard = new Keyboard(this, R.xml.qwerty);
        mKeyView.setKeyboard(mKeyBoard);
        mKeyView.setOnKeyboardActionListener(this);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            System.err.println("Nie wspiera");
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }else{
            System.out.println("wspiera"+bluetoothAdapter);
        }


        return mKeyView;
    }

//   protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
//        if(!bluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
//        }
//    }
 /*   private  void implementListeners(){

        listen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ServerClass serverClass=new ServerClass();
                serverClass.start();
            }
        });
    }*/

    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case STATE_MESSAGE_RECIVED:

                    byte[] readBuff= (byte[])msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    InputConnection inputConnection = getCurrentInputConnection();
                    inputConnection.commitText(String.valueOf(tempMsg), 1);
                    break;
            }
            return true;
        }
    });


    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;
        public ServerClass(){
            try {
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run(){
            BluetoothSocket socket=null;
            while(socket==null){
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket!=null){
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive= new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class SendReceive extends  Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut= bluetoothSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            inputStream=tempIn;
            outputStream=tempOut;
        }
        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIVED, bytes,-1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }



    // Listeners
    @Override
    public void onPress(int primaryCode) {
        // no-op
    }

    @Override
    public void onRelease(int primaryCode) {
        // no-op
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        //1
        if (primaryCode == 49) {
            ServerClass serverClass=new ServerClass();
            serverClass.start();
        }
        if (primaryCode == 50) {
            InputConnection inputConnection = getCurrentInputConnection();
            inputConnection.deleteSurroundingText(50,50);

        }



    }



    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {
        // no-op
    }

    @Override
    public void swipeRight() {
        // no-op
    }

    @Override
    public void swipeDown() {
        // no-op
    }

    @Override
    public void swipeUp() {
        // no-op
    }
}