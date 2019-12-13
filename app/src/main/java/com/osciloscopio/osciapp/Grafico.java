package com.osciloscopio.osciapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.sql.Array;
import java.sql.ParameterMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Grafico extends AppCompatActivity {

    private static final String TAG = "Grafico";

    // Variables:
    private LineChart mChart;
    Handler handler = new Handler();
    private final int TIEMPO = 100;
    private float randomF;
    private float minF = 4f;
    private float maxF = 7f;
    Random rng = new Random();
    float arrayDatos[] = new float[50];

    float dato = 0;
    boolean estaBajando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);

        mChart = (LineChart) findViewById(R.id.line);
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
        Arrays.fill(arrayDatos, 0);

        actualizarGraficoTiempo(arrayDatos);

        generarGrafico(arrayDatos);
    }

    public void agregarDatoFinalArray(float arrayD[], float datoNuevo) {
        for (int i = 0; i < arrayD.length; i++) {
            if (i == arrayD.length - 1) {
                arrayD[i] = datoNuevo;
            }else {
                arrayD[i] = arrayD[i + 1];
            }
        }
        generarGrafico(arrayD);
    }

    public void actualizarGraficoTiempo(final float arrayD[]){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("update");
                if( dato > 20) {
                    estaBajando = true;
                }else if (dato < 0) {
                    estaBajando = false;
                }
                if (estaBajando) {
                    dato = dato - rng.nextFloat() * (maxF - minF);
                }else {
                    dato = dato + rng.nextFloat() * (maxF - minF);
                }
                agregarDatoFinalArray(arrayD, dato);
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
}
