package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


Button listen,send,listDevices;
ListView listView;
TextView msg_box,status;
EditText writeMsg;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;
    SendReceive sendReceive;

    static  final  int STATE_LISTENING = 1;
    static  final  int STATE_CONNECTING=2;
    static  final  int STATE_CONNECTED=3;
    static  final  int STATE_CONNECTION_FAILED=4;
    static  final  int STATE_MESSAGE_RECIVED=5;

    int REQUEST_ENABLE_BLUETOOTH=1;

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("b43ae580-f8f4-4128-a193-45f1afb8044c");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewByIdes();
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
        implementListeners();
    }
    private  void implementListeners(){
        listDevices.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt= bluetoothAdapter.getBondedDevices();
                String[] strings= new String[bt.size()];
                btArray= new BluetoothDevice[bt.size()];
                int index=0;
                if (bt.size()>0){
                    for(BluetoothDevice device : bt)
                    {
                        btArray[index]= device;
                        strings[index]=device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                    listView.setAdapter(arrayAdapter);

                }
            }
        });
        listen.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view){
                 ServerClass serverClass=new ServerClass();
                 serverClass.start();
             }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                    ClientClass clientClass=new ClientClass(btArray[i]);
                    clientClass.start();

                    status.setText("Connecting");
            }
        });
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String string=String.valueOf(writeMsg.getText());
                sendReceive.write(string.getBytes());
            }

        } );
    }

    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case STATE_LISTENING:
                    status.setText("listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("connection failed");
                    break;
                case STATE_MESSAGE_RECIVED:
                    status.setText("connecting");
                    byte[] readBuff= (byte[])msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void findViewByIdes(){
        listen=(Button)findViewById(R.id.listen);
        send=(Button) findViewById(R.id.send);
        listView=(ListView) findViewById(R.id.listview);
        msg_box=(TextView) findViewById(R.id.msg);
        status=(TextView) findViewById(R.id.status);
        writeMsg=(EditText) findViewById(R.id.writemsg);
        listDevices=(Button) findViewById(R.id.listDevices);

    }
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
    private class ClientClass extends  Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1){
            device=device1;
            try {
                socket=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run(){
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
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
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
