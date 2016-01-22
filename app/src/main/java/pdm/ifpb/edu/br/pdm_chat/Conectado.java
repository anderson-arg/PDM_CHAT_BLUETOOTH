package pdm.ifpb.edu.br.pdm_chat;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Anderson on 14/01/2016.
 */
public class Conectado extends Thread {
    private final String tag = "API";
    private final int MESSAGE_WRITE = 1;
    private final int MESSAGE_READ = 2;
    private final int DISCONNECT = 6;

    private final BluetoothSocket bluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private Context context;

    private Handler handler;

    public Conectado(BluetoothSocket socket, Context context, Handler handler){
        this.bluetoothSocket = socket;
        this.context = context;
        this.handler = handler;
        InputStream tmpInput = null;
        OutputStream tmpOutput = null;

        try {
            tmpInput = socket.getInputStream();
            tmpOutput = socket.getOutputStream();
        } catch (IOException e) {
            Log.i(tag, "erro ao tentar receber o input e output");
        }

        inputStream = tmpInput;
        outputStream = tmpOutput;
    }

    public void run(){
        byte[] buffer = new byte[1024];
        int bytes;

        while(true){
            try {
                bytes = inputStream.read(buffer);
                Log.i(tag, "Recebeu os dados de entrada");
                // enviar mensagem para a tela
                String texto = new String(buffer, 0, bytes);
                Log.i(tag, bluetoothSocket.getRemoteDevice().getName() + " diz: " + texto);

                if(texto.equals("exit")){
                    handler.obtainMessage(DISCONNECT, -1, -1, null).sendToTarget();
                }else{
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }

            } catch (IOException e) {
                Log.i(tag, "erro ao tentar receber dados de entrada");
                break;
            }
        }
    }

    public void writer(byte[] bytes){
        try {
            outputStream.write(bytes);
            handler.obtainMessage(MESSAGE_WRITE, -1, -1, bytes).sendToTarget();
        } catch (IOException e) {
            Log.i(tag, "erro ao tentar escrever bytes");
        }
    }

    public BluetoothSocket getBluetoothSocket(){
        return this.bluetoothSocket;
    }

    public void cancel(){
        try {
            bluetoothSocket.close();
        } catch (IOException e) {

        }
    }
}
