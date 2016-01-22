package pdm.ifpb.edu.br.pdm_chat;

import android.app.Fragment;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TelaConversa extends AppCompatActivity {

    private final String tag = "API";
    private final int MESSAGE_WRITE = 1;
    private final int MESSAGE_READ = 2;
    private final int DISCONNECT = 6;
    private final int DELL = 7;

    private Button btnSair;
    private Button btnEnviar;
    private EditText editText;
    private ListView listView;

    // variavel para ser acessada pela Class Conectado
    private ArrayAdapter<String> conversas;

    private Conectado conectado;
    private BluetoothSocket ganbCliente = Cliente.ganbSocket;
    private BluetoothSocket ganbServidor = Servidor.ganbSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_conversa);
        init();

        if(this.getIntent().getStringExtra("socket").equals("cliente")){
            Log.i(tag, "Instanciando Conectado: "+this.getIntent().getStringExtra("socket"));
            Log.i(tag, "Cliente: "+ganbCliente.getRemoteDevice().getName()+" "+ganbCliente.getRemoteDevice().getAddress());
            conectado = new Conectado(ganbCliente, this, handler);
            conectado.start();
            Servidor.cancel();
            //Cliente.handler.obtainMessage(DELL, -1, -1, null).sendToTarget();
        }else if(this.getIntent().getStringExtra("socket").equals("servidor")){
            Log.i(tag, "Instanciando Conectado: "+this.getIntent().getStringExtra("socket"));
            Log.i(tag, "Servidor: "+ganbServidor.getRemoteDevice().getName()+" "+ganbServidor.getRemoteDevice().getAddress());
            conectado = new Conectado(ganbServidor, this, handler);
            conectado.start();
            Servidor.cancel();
            //Servidor.handler.obtainMessage(DELL, -1, -1, null).sendToTarget();
        }
    }

    public void init(){
        btnSair = (Button) findViewById(R.id.btnSair);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.listView2);

        conversas = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(conversas);

        btnEnviar.setOnClickListener(new OnclickButton());
        btnSair.setOnClickListener(new OnclickButton());
    }

    private class OnclickButton implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if(v.equals(btnEnviar)){
                byte[] textoByte = editText.getText().toString().getBytes();
                editText.setText(null);
                if(textoByte.length > 0)
                    conectado.writer(textoByte);
            }else if(v.equals(btnSair)){
                back();
                finish();
            }
        }
    }

    public void chamarTela(){
        conectado.cancel();
        conectado = null;
        Intent main = new Intent(TelaConversa.this, MainActivity.class);
        startActivity(main);
    }

    public void back(){
        String disconnect = "exit";
        byte[] buff = disconnect.getBytes();
        conectado.writer(buff);
        chamarTela();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        back();
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
                case MESSAGE_WRITE:
                    byte[] bufferWrite = (byte[]) msg.obj;
                    String textWrite = new String(bufferWrite);
                    conversas.add("Eu: "+textWrite);
                    break;
                case MESSAGE_READ:
                    byte[] bufferRead = (byte[]) msg.obj;
                    String textRead = new String(bufferRead, 0, msg.arg1);
                    conversas.add(conectado.getBluetoothSocket().getRemoteDevice().getName()+" diz: "+textRead);
                    break;
                case DISCONNECT:
                    chamarTela();
                    finish();
                    break;
            }
        }
    };
}
