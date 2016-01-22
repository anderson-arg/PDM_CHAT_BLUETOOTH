package pdm.ifpb.edu.br.pdm_chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final String tag = "API";
    private final int REQUEST_ENABLE_BT = 3;
    private final int SCANER_FAIL = 4;
    private final int DELL = 7;

    // variaveis da tela
    private TextView textView;
    private ListView listView;
    private Button btnScaner;

    // variaveis do BT
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> nomesBT = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    // variaveis service
    private Cliente cliente;
    private Servidor servidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        // iniciando e verificando se o dispositivo suporta BT
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Log.i(tag, "Dispositivo não suporta bluetooth");
        }

        // ativando bluetooth
        if(!bluetoothAdapter.isEnabled()){
            Intent ativarBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativarBT, REQUEST_ENABLE_BT);
        }

        // permitindo descoberta do dispositivo
        Intent discobertaBT = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discobertaBT.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discobertaBT);

        // recuperando BT emparelhados no cell
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                nomesBT.add(device.getName()+"\n"+device.getAddress());
            }
        }

        // registrando filtro para escanear bluetooth na area
        IntentFilter escanearArea = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(new BroadcastReceiver(), escanearArea);
        escanearArea = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(new BroadcastReceiver(), escanearArea);


        // conectando servidor
        conectarService();
    }

    public void init(){
        textView = (TextView) findViewById(R.id.textView);
        listView = (ListView) findViewById(R.id.listView);
        btnScaner = (Button) findViewById(R.id.btnScaner);
        btnScaner.setOnClickListener(new OnclickButton());

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nomesBT);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClick());
    }

    private class OnclickButton implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            bluetoothAdapter.startDiscovery();
            Toast.makeText(MainActivity.this, "Procurando dispositivos...", Toast.LENGTH_LONG).show();
        }
    }

    private class OnItemClick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this, "Aguarde um momento...", Toast.LENGTH_LONG).show();
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() -17);
            conectarCliente(address);
        }
    }

    public void conectarCliente(String address){
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        cliente = new Cliente(device, bluetoothAdapter, this, handler);
        cliente.start();
    }

    public void conectarService(){
        cliente = null;
        servidor = null;
        servidor = new Servidor(bluetoothAdapter, this, handler);
        servidor.start();
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                adapter.add(device.getName()+"\n"+device.getAddress());
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                builder.setTicker("Ticker");
                builder.setContentText("Escaneamento terminado!");
                builder.setContentTitle("Scaner");
                builder.setSubText("");
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setAutoCancel(true);

                NotificationManagerCompat nmc = NotificationManagerCompat.from(MainActivity.this);
                nmc.notify(1, builder.build());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private final android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case SCANER_FAIL:
                    Toast.makeText(MainActivity.this, "Dispositivo indisponivel!", Toast.LENGTH_LONG).show();
                    break;
                case DELL:
                    destroy();
                    finish();
                    break;
            }
        }
    };

    public void destroy(){
        cliente.cancel();
        cliente = null;
        servidor.cancel();
        servidor = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }
}
