package ru.deklarant.scanapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.microblink.hardware.camera.CameraType;
import com.microblink.recognition.InvalidLicenceKeyException;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417ScanResult;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.recognizers.settings.RecognizerSettingsUtils;
import com.microblink.util.RecognizerCompatibility;
import com.microblink.util.RecognizerCompatibilityStatus;
import com.microblink.view.CameraAspectMode;
import com.microblink.view.CameraEventsListener;
import com.microblink.view.recognition.RecognizerView;
import com.microblink.view.recognition.ScanResultListener;

import ru.deklarant.scanapp.ScanCompletedDIalogFragment.OnScanResultDialogClosedListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ru.deklarant.scanapp.ru.deklarant.scanapp.utils.TcpSendTask;

public class MainActivity extends AppCompatActivity implements ScanResultListener, CameraEventsListener, OnScanResultDialogClosedListener {

    private static RecognizerSettings[] recognizerSettings;
    private static RecognizerView mRecognizerView;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 69;
    private static String SERIAL_KEY = "UZDLKDUJ-76VGLVI7-IVJMRF24-WLMQNR5U-RCQNAZ76-ZEPUKWUK-NI52XIKL-5OMYNKGV";

    private byte[] barcodeData;

    private SharedPreferences sp;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecognizerView = (RecognizerView) findViewById(R.id.RecognView);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //установка настроек и проверка доступности распознавания
        RecognizerCompatibilityStatus compabilityStatus = RecognizerCompatibility.getRecognizerCompatibilityStatus(this);
        if (compabilityStatus == RecognizerCompatibilityStatus.RECOGNIZER_SUPPORTED) {
            RecognizerSettings[] settings = setupSettingsArray();
            if (!RecognizerCompatibility.cameraHasAutofocus(CameraType.CAMERA_BACKFACE, this)) {
                recognizerSettings = RecognizerSettingsUtils.filterOutRecognizersThatRequireAutofocus(settings);
            }
            RecognitionSettings recognitionSettings = new RecognitionSettings();
            recognitionSettings.setRecognizerSettingsArray(settings);
            mRecognizerView.setRecognitionSettings(recognitionSettings);
            //установка слушателей
            mRecognizerView.setScanResultListener(this);
            mRecognizerView.setCameraEventsListener(this);
            mRecognizerView.setAspectMode(CameraAspectMode.ASPECT_FILL);
            try {
                mRecognizerView.setLicenseKey(SERIAL_KEY);
            } catch (InvalidLicenceKeyException e) {
                e.printStackTrace();
            }
            mRecognizerView.create();
            //загружаем настройки
            sp = PreferenceManager.getDefaultSharedPreferences(this);
        } else {
            Toast.makeText(this, "К сожалению, Ваше устройство не поддерживает механизм распознавания", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private RecognizerSettings[] setupSettingsArray() {
        Pdf417RecognizerSettings sett = new Pdf417RecognizerSettings();
        // отключаем сканирование белых кодов на черном фоне
        sett.setInverseScanning(false);
        //разрешаем сканирование кодов с битой чексуммой
        sett.setUncertainScanning(true);
        //отключает сканирование с пустой белой зоной (объявленной в стандарте)
        sett.setNullQuietZoneAllowed(false);
        //итоговый массив настроек
        return new RecognizerSettings[]{sett};
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRecognizerView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // you need to pass all activity's lifecycle methods to RecognizerView
        mRecognizerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // you need to pass all activity's lifecycle methods to RecognizerView
        mRecognizerView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecognizerView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // you need to pass all activity's lifecycle methods to RecognizerView
        mRecognizerView.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // you need to pass all activity's lifecycle methods to RecognizerView
        mRecognizerView.changeConfiguration(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PrefActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraPreviewStarted() {

    }

    @Override
    public void onCameraPreviewStopped() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }

    @Override
    public void onAutofocusFailed() {

    }

    @Override
    public void onAutofocusStarted(Rect[] rects) {

    }

    @Override
    public void onAutofocusStopped(Rect[] rects) {

    }

    @Override
    public void onScanningDone(RecognitionResults recognitionResults) {

        mRecognizerView.pauseScanning();
        //разбор результатов
        BaseRecognitionResult[] dataArray = recognitionResults.getRecognitionResults();
        for (BaseRecognitionResult result : dataArray) {
            if (result instanceof Pdf417ScanResult) {

                Pdf417ScanResult scanResult = (Pdf417ScanResult) result;
                barcodeData = scanResult.getRawData().getAllData();
                ScanCompletedDIalogFragment dialog = new ScanCompletedDIalogFragment();
                Bundle args = new Bundle();
                args.putByteArray("barcodeData", barcodeData);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "resultDialog");
            }
        }
    }

    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];

        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; // Autoboxing

        return bytes;
    }

    @Override
    public void OnScanResultDialogClosed(ScanCompletedDIalogFragment.ScanCompletedDialogCode resultCode) {
        switch (resultCode) {
            case SEND_TCP_CODE:
                TcpSendTask task = new TcpSendTask(MainActivity.this);
                task.setTcpSendEndedListener(new TcpSendTask.OnTcpSendEndedListener() {
                    @Override
                    public void OnTcpSendEnded(boolean result) {
                        Toast.makeText(MainActivity.this, result ? "Отправлено" : "Не отправлено", Toast.LENGTH_SHORT).show();
                        //пауза, чтобы новое сканирование начиналось не сразу
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mRecognizerView.resumeScanning(true);
                    }
                });
                task.execute(new Byte[][]{toObjects(barcodeData)});
                break;
        }
    }
}
