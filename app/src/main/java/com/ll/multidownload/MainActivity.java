package com.ll.multidownload;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private String downloadUrl = "http://cdn.qiyestore.com/statics/qiyestore/app/2.0/android.apk";
    private String localPath;
    private String fileName = "download.apk";
    private Button btnStartDownload;
    private Button btnDelete;
    private ProgressBar mProgressBar;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStartDownload = (Button) findViewById(R.id.btn_start_download);
        btnDelete = (Button)findViewById(R.id.btn_delete_url);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.getDownLoadPath();
        downloadManager = new DownloadManager(this,downloadUrl,localPath,fileName,4,mHandler);
        /**
         * 下载
         */
        btnStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(downloadManager.getDownloadState() == DownloadManager.PAUSE){
                    btnStartDownload.setText("暂停");
                    downloadManager.init();
                }else if(downloadManager.getDownloadState() == DownloadManager.DOWNLOADING){
                  btnStartDownload.setText("继续下载");
                  downloadManager.pause();
              }else {
                  btnStartDownload.setText("暂停");
                  downloadManager.init();
              }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.delete();
            }
        });
    }
    private void getDownLoadPath() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
            localPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() +File.separator+"download"+File.separator;
        else
            localPath = getFilesDir().getAbsolutePath()
                    + File.separator+"download"+File.separator;
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DownloadManager.INIT_COMPLETED:
                    mProgressBar.setMax(downloadManager.getFileSize());
                    downloadManager.download();////初始化完成 开始下载
                    break;
                case DownloadManager.DOWNLOADING://正在下载
                        mProgressBar.setProgress(downloadManager.getCompleteSize());
                    break;
                case DownloadManager.DOWNLOAD_COMPLETED://下载完成
                    btnStartDownload.setText("下载完成");
                    Toast.makeText(MainActivity.this,"下载完成!",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

}
