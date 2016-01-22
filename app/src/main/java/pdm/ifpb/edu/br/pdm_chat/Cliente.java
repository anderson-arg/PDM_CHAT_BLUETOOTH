package pdm.ifpb.edu.br.pdm_chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Anderson on 14/01/2016.
 */
public class Cliente extends Thread {
    private final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final String chat = "ChatPDM";
    private final String tag = "API";
    private final int SCANER_FAIL = 4;

    private final BluetoothSocket bluetoothSocket;
    private final BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    // Utilizando para ser acessado da outra tela
    public static BluetoothSocket ganbSocket;
    public static Handler handler;

    public Cliente(BluetoothDevice device, BluetoothAdapter adapter, Context context, Handler handler){
        bluetoothDevice = device;
        bluetoothAdapter = adapter;
        this.context = context;
        this.handler = handler;

        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.i(tag, "erro ao tentar criar o protocolo SDP");
        }
        bluetoothSocket = tmp;
    }

    public void run(){
        bluetoothAdapter.cancelDiscovery();

        try {
            bluetoothSocket.connect();
            ganbSocket = bluetoothSocket;
            Log.i(tag, "Cliente conectou com o servidor");
            chamarTela();
        } catch (IOException e) {
            Log.i(tag, "erro ao tentar conectar com o servidor");
            handler.obtainMessage(SCANER_FAIL, -1, -1, null).sendToTarget();
            cancel();
        }

    }

    public void chamarTela(){
        // chamar tela e enviar o socket
        ArrayList<BluetoothSocket> socket = new ArrayList<BluetoothSocket>();
        socket.add(bluetoothSocket);

        Intent telaPrincipal = new Intent(context, TelaConversa.class);
        telaPrincipal.putExtra("socket", "cliente");
        context.startActivity(telaPrincipal);
    }

    public void cancel(){
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            return;
        }
    }
}

