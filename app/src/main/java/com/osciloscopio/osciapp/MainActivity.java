package com.osciloscopio.osciapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.charts.Chart.*;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //---------------MPAndroidChart-------------
    private static final String TAG = "MainActivity";

    private LineChart mChart;
    Handler handler = new Handler();
    private final int TIEMPO = 10000;
    float arrayDatos[] = new float[250];
    float arrayDNuevo[];

    //float dato = 0;
    boolean primero = true;
    boolean segundo = false;
    boolean estaParado = false;
    int contadorDatos = 0;
    //---------------MPAndroidChart-------------

    //1)
    Button IdEncender,IdDesconectar;
    TextView IdBufferIn;
    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;

    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //-------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Enlaza los controles con sus respectivas vistas
        IdEncender = (Button) findViewById(R.id.IdEncender);
        IdDesconectar = (Button) findViewById(R.id.IdDesconectar);
        IdBufferIn = (TextView) findViewById(R.id.IdBufferIn);

        Arrays.fill(arrayDatos, 0);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    System.out.println(readMessage + "asd");
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        System.out.println(dataInPrint + "wasa");
                        contadorDatos = contadorDatos + 1;
                        if (!estaParado){
                            double db = Double.parseDouble(dataInPrint);
                            agregarDatoFinalArray(arrayDatos, db);
                            IdBufferIn.setText("Último Dato Obtenido: " + dataInPrint);//<-<- AQUI ES PARA ACTUALIZAR EL LINECHART PARTE A MODIFICAR >->->
                        }
                        if(primero){
                            actualizarArray(arrayDatos);
                        }
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

        // Configuracion onClick listeners para los botones
        // para indicar que se realizara cuando se detecte
        // el evento de Click
        IdEncender.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                estaParado = !estaParado;
            }
        });
        IdDesconectar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btSocket!=null)
                {
                    try {btSocket.close();}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();;}
                }
                finish();
            }
        });

        //---------------MPAndroidChart-------------
        mChart = (LineChart) findViewById(R.id.linechart);
        mChart.setNoDataText("Aún no se envían Datos");
        // Estilos del Chart
        mChart.setDrawBorders(true);
        mChart.setDrawGridBackground(true);
        mChart.setGridBackgroundColor(Color.LTGRAY);

        // mChart.

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        /*for (int i = 0; i <100; i++) {
            randomF = minF + rng.nextFloat() * (maxF - minF);
            arrayDatos[i] = randomF;
        }*/



        generarGrafico(arrayDatos);
        //---------------MPAndroidChart-------------
    }

    public void agregarDatoFinalArray(float arrayD[], double datoNuevo) {
        float d = (float) datoNuevo;
        for (int i = 0; i < arrayD.length; i++) {
            if (i == arrayD.length - 1) {
                arrayD[i] = d;
            }else {
                arrayD[i] = arrayD[i + 1];
            }
        }
        generarGrafico(arrayD);
    }

    public void actualizarArray(final float arrayD[]){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                int arrayDlength = arrayD.length -1;
                if(segundo){
                    float arrayDNuevo[] = new float[contadorDatos];
                    Arrays.fill(arrayDNuevo,0);
                    for (int i = arrayDNuevo.length -1; i > 0; i --){
                        if(arrayDlength > 0){
                            arrayDNuevo[i] = arrayD[arrayDlength];
                            arrayDlength = arrayDlength -1;
                        }
                    }
                    segundo = false;
                    System.out.println("WASSSSSSSSSSSSSSSAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                }
                if(primero){
                    primero = false;
                    segundo = true;
                }

                System.out.println("handler ---------------------------------------------------------------------------------------" + contadorDatos);
                contadorDatos = 0;

                handler.postDelayed(this, TIEMPO);
            }
        }, TIEMPO);
    }

    public void generarGrafico(float[] arrayD) {
        ArrayList<Entry> yValues = new ArrayList<>();
        /*for (int i = 0; i < 50; i++) {
            randomF = minF + rng.nextFloat() * (maxF - minF);
            yValues.add(new Entry(i, randomF));
        }*/
        for (int i = 0; i < arrayD.length; i++) {
            yValues.add(new Entry(i,arrayD[i]));
        }

        LineDataSet set1 = new LineDataSet(yValues, "Data Set 1");
        set1.setFillAlpha(110);
        set1.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        mChart.invalidate();
        mChart.setData(data);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}