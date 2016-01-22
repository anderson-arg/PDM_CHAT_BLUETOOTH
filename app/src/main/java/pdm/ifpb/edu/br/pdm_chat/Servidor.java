package pdm.ifpb.edu.br.pdm_chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Anderson on 14/01/2016.
 */
public class Servidor extends Thread{
    private final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final String chat = "ChatPDM";
    private final String tag = "API";

    private static BluetoothServerSocket bluetoothServerSocket;
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    // variavel para ser acessado da telaConversa
    public static BluetoothSocket ganbSocket;
    public static Handler handler;

    public Servidor(BluetoothAdapter bluetoothAdapter, Context context, Handler handler){
        this.bluetoothAdapter = bluetoothAdapter;
        this.context = context;
        this.handler = handler;

        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(chat, MY_UUID);
        } catch (IOException e) {
            Log.i(tag, "erro ao tentar ouvir o protocolo SDP");
        }
        bluetoothServerSocket = tmp;
    }

    public void run(){
        BluetoothSocket socket = null;
        while(true){
            try {
                socket = bluetoothServerSocket.accept();
                ganbSocket = socket;
                Log.i(tag, "Servidor recebeu dados: "+socket.getRemoteDevice().getName()+" "+socket.getRemoteDevice().getAddress());
            } catch (IOException e) {
                Log.i(tag, "erro ao tentar receber o cliente");
                break;
            }

            if(socket != null){
                // chamar tela e enviar o socket
                ArrayList<BluetoothSocket> btSocket = new ArrayList<BluetoothSocket>();
                btSocket.add(socket);

                Intent telaPrincipal = new Intent(context, TelaConversa.class);
                telaPrincipal.putExtra("socket", "servidor");
                context.startActivity(telaPrincipal);
                //cancel();
                //break;
            }
        }
    }

    public static void cancel(){
        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {

        }
    }
}
