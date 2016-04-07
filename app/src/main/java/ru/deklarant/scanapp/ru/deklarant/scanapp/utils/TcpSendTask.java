package ru.deklarant.scanapp.ru.deklarant.scanapp.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by сони on 05.04.2016.
 */
public class TcpSendTask extends AsyncTask<Byte[],Void,Boolean> {
    private OnTcpSendEndedListener TcpSendEndedListener;
    private Context mContext;
    private ProgressDialog progressDialog;

    public TcpSendTask(Context context){
        this.mContext=context;
        progressDialog=new ProgressDialog(mContext);
    }

    public void setTcpSendEndedListener(OnTcpSendEndedListener listener){
        this.TcpSendEndedListener=listener;
    }

    @Override
    protected Boolean doInBackground(Byte[]... params) {
        try {
            Socket socket=new Socket("192.168.173.1",12344);
            OutputStream output=socket.getOutputStream();
            byte[] data=toPrimitives(params[0]);
            output.write(data);
            output.flush();
            output.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setTitle("Отправка");
        progressDialog.setMessage("Идет отправка данных");
        progressDialog.show();
    }


    private byte[] toPrimitives(Byte[] oBytes)
    {
        byte[] bytes = new byte[oBytes.length];

        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        if(this.TcpSendEndedListener!=null){
            this.TcpSendEndedListener.OnTcpSendEnded(aBoolean);
        }
    }

    public interface OnTcpSendEndedListener{
        public void OnTcpSendEnded(boolean result);
    }
}
