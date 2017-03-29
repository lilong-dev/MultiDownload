package com.ll.multidownload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.ll.multidownload.bean.DownloadInfo;
import com.ll.multidownload.db.DBManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/3/28.
 * download manager
 */

public class DownloadManager{
    private String downloadUrl;//下载的地址
    private String localPath;//保存的地址
    private int completeSize;//已经下载的大小
    private int threadNum;//开启的线程数量
    private int block;// 每条线程下载的长度
    private int fileSize;// 所要下载的文件的大小
    private DBManager dbManager;
    static final int INIT = 1;//定义三种下载的状态：初始化状态，正在下载状态，暂停状态
    static final int DOWNLOADING = 2;
    static final int PAUSE = 3;
    private int state = INIT;
    private Handler mHandler;//用于更新
    private List<DownloadInfo> infos;
    private String fileName;
    static final int INIT_COMPLETED = 4;//初始化完成
    static final int DOWNLOAD_COMPLETED = 5;//下载完成
    public DownloadManager(Context context,String downloadUrl, String localPath,String fileName,int threadNum,Handler handler){
        this.downloadUrl = downloadUrl;
        this.localPath = localPath;
        this.threadNum = threadNum;
        this.mHandler = handler;
        this.fileName = fileName;
        this.dbManager = new DBManager(context);
    }

    /**
     *查询每个线程下载的信息
     */
    public void init(){
        completeSize = 0;
        new Thread(){
            @Override
            public void run() {
                super.run();
                if(dbManager.isHasInfos(downloadUrl)){//第一次下载
                    initFile();
                    block = (fileSize % threadNum) == 0 ? fileSize / threadNum
                            : fileSize / threadNum + 1;
                    infos = new ArrayList<DownloadInfo>();
                    for(int i = 0; i < threadNum;i++){
                        int endPos = (i + 1) != threadNum ? ((i + 1) * block - 1)
                                : fileSize;
                        DownloadInfo info = new DownloadInfo(i, i * block, endPos, 0, downloadUrl);
                        infos.add(info);
                    }
                    //保存infos中的数据到数据库
                    dbManager.saveDownloadInfos(infos);
                }else{
                    //得到数据库中已有的urlstr的下载器的具体信息
                    infos = dbManager.getDownloadInfos(downloadUrl);
                    for(DownloadInfo info:infos){
                        completeSize += info.getCompleteSize();
                    }
                    fileSize = infos.get(infos.size() - 1).getEndPos();
                }

                mHandler.sendEmptyMessage(INIT_COMPLETED);
            }
        }.start();

    }

    /**
     * 初始化文件
     */
    private void initFile() {
        HttpURLConnection connection = null;
        RandomAccessFile accessFile = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            // 获得文件的长度
            if (connection.getResponseCode() == 200){
                fileSize = connection.getContentLength();
            }
            if (fileSize <= 0) return;
            // 在本地创建文件
            File dir = new File(localPath);
            if (!dir.exists())
                dir.mkdirs();
            File saveFile = new File(dir,fileName);
            accessFile = new RandomAccessFile(saveFile, "rwd");
            accessFile.setLength(fileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (connection != null)
                connection.disconnect();
            if (accessFile != null)
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * 开始下载
     */
    public void download(){
        if(isDownloadComplete()){
            mHandler.sendEmptyMessage(DOWNLOAD_COMPLETED);//下载完成
            return;//已经下载完毕
        }
        if(infos == null) return;
        if(state == DOWNLOADING) return;
        state = DOWNLOADING;
        for(DownloadInfo info:infos){
            new DownloadThread(info.getThreadId(),info.getStartPos(),info.getEndPos(),info.getCompleteSize(),info.getDownloadUrl()).start();
        }
    }
    /**
     * 下载线程
     */
    class DownloadThread extends Thread{
         private int threadId;
         private int startPos;
         private int endPos;
         private int mCompleteSize;
         private String downloadUrl;
        public DownloadThread(int threadId,int startPos,int endPos,int completeSize,String downloadUrl){
            this.threadId = threadId;
            this.startPos = startPos;
            this.endPos = endPos;
            this.mCompleteSize = completeSize;
            this.downloadUrl = downloadUrl;
        }

        @Override
        public void run() {
            super.run();
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            byte buf[] = new byte[4096];
            int length = -1;
            try {
                URL url = new URL(downloadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                // 设置下载位置
                connection.setRequestProperty("Range", "bytes="
                        + (startPos + mCompleteSize) + "-" + endPos);
                connection.setAllowUserInteraction(true);
                File file = new File(localPath,fileName);
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(startPos + mCompleteSize);
                // 开始下载
                if (connection.getResponseCode() == 206) {
                    // 读取数据
                    inputStream = connection.getInputStream();
                    while ((length = inputStream.read(buf)) != -1){
                        raf.write(buf, 0, length);
                        mCompleteSize += length;
                        append(length);
                        update();
                        if (state == PAUSE){
                            //更新数据库中的信息
                            dbManager.updataInfos(threadId,mCompleteSize,downloadUrl);
                            return;
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    if (connection != null)
                        connection.disconnect();
                    if (raf != null)
                        raf.close();
                    if (inputStream != null)
                        inputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * 已下载的大小
     * @param size
     */
    public synchronized void append(int size){
        completeSize += size;
    }

    /**
     * 更新
     */
    public void update(){
        if(!isDownloadComplete()){
            mHandler.sendEmptyMessageDelayed(DOWNLOADING,200);
        }else{
            mHandler.sendEmptyMessageDelayed(DOWNLOAD_COMPLETED,200);//下载完成
        }
    }
    /**
     * 是否下载完
     * @return
     */
    public boolean isDownloadComplete(){
        if(completeSize < fileSize){
           return false;
        }else{
           return true;
        }
    }
    /**
     * 获取已下载的大小
     * @return
     */
    public int getCompleteSize(){
        return  this.completeSize;
    }

    /**
     * 获取下载文件的大小
     * @return
     */
    public int getFileSize(){
        return this.fileSize;
    }
    /**
     * 设置暂停
     */
    public void pause(){
        state = PAUSE;
    }

    /**
     * 重置下载状态
     */
    public void reset(){
        state = INIT;
    }

    /**
     *
     */
    public void delete(){
        dbManager.deleteByUrl(downloadUrl);
    }

    /**
     * 获取下载的状态
     * @return
     */
    public int getDownloadState(){
        return this.state;
    }
}
