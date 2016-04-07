package ru.deklarant.scanapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import ru.deklarant.scanapp.ru.deklarant.scanapp.utils.TcpSendTask;

public class ScanCompletedDIalogFragment extends DialogFragment implements View.OnClickListener {

    private SharedPreferences sp;
    private OnScanResultDialogClosedListener scanResultDialogClosedListener;

    public ScanCompletedDIalogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Сканирование выполнено");
        //настраивам View
        View v = inflater.inflate(R.layout.scan_data_dialog, null);
        Button yesButton = (Button) v.findViewById(R.id.send_button);
        Button noButton = (Button) v.findViewById(R.id.cancel_button);
        Button settingsButton = (Button) v.findViewById(R.id.settings_button);
        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        RadioButton tcpButton=(RadioButton)v.findViewById(R.id.tcp_send_button);
        final TextView label=(TextView)v.findViewById(R.id.no_settings_view);
        tcpButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sp= PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String ip=sp.getString("IP",null);
                    String port=sp.getString("port",null);
                    label.setVisibility(ip!=null &&ip.length()>0 && port!=null && port.length()>0?View.GONE:View.VISIBLE);
                }
            }
        });
        tcpButton.setChecked(true);
        //проверяем, есть ли настройки
        return v;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            scanResultDialogClosedListener = (MainActivity) context;
        }catch (ClassCastException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.cancel_button:
                if(scanResultDialogClosedListener!=null){
                    scanResultDialogClosedListener.OnScanResultDialogClosed(ScanCompletedDialogCode.RESUME_SCANCODE);
                }
                dismiss();
                break;
            case R.id.settings_button:
                Intent intent=new Intent(getActivity(),PrefActivity.class);
                startActivity(intent);
                if(scanResultDialogClosedListener!=null){
                    scanResultDialogClosedListener.OnScanResultDialogClosed(ScanCompletedDialogCode.RESUME_SCANCODE);
                }
                dismiss();
                break;
            case R.id.send_button:
                if(scanResultDialogClosedListener!=null){
                    scanResultDialogClosedListener.OnScanResultDialogClosed(ScanCompletedDialogCode.SEND_TCP_CODE);
                }
                dismiss();
                break;
            default:break;
        }
    }
public interface OnScanResultDialogClosedListener {
    public void OnScanResultDialogClosed(ScanCompletedDialogCode code);
}
    public enum ScanCompletedDialogCode{
        SEND_TCP_CODE,SEND_BLUETOOTH_CODE,RESUME_SCANCODE;
    }
}
