package com.ll.multidownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ll.multidownload.bean.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/3/28.
 */

public class DBManager {
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public DBManager(Context context){
        dbHelper = new DBHelper(context,null,null,0);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询数据库中是否有数据
     * @param url
     * @return
     */
    public boolean isHasInfos(String url){
        String sql = "SELECT COUNT(*)  FROM download WHERE download_url=?";
        Cursor cursor = db.rawQuery(sql,new String[]{url});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    /**
     * 保存下载的信息
     * @param infos
     */
    public void saveDownloadInfos(List<DownloadInfo> infos){
        for (DownloadInfo info : infos) {
            this.saveDownloadInfo(info);
        }

    }

    private void saveDownloadInfo(DownloadInfo info) {
        db.execSQL("INSERT INTO download(thread_id,start_pos,end_pos,complete_size,download_url) VALUES(?,?,?,?,?)",
                new Object[]{info.getThreadId(),info.getStartPos(),info.getEndPos(),info.getCompleteSize(),info.getDownloadUrl()});
    }

    /**
     * 查询所有线程下载的信息
     */
    public  List<DownloadInfo> getDownloadInfos(String url){
        List<DownloadInfo> list = new ArrayList<DownloadInfo>();
        String sql = "SELECT thread_id, start_pos, end_pos,complete_size,download_url from download where download_url=?";
        Cursor cursor = db.rawQuery(sql, new String[] { url });
        while(cursor.moveToNext()){
            DownloadInfo info = new DownloadInfo(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
                                         cursor.getString(4));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    /**
     * 更新数据库中的下载信息
     * @param threadId
     * @param compeleteSize
     * @param urlstr
     */
    public void updataInfos(int threadId, int compeleteSize, String urlstr) {
        db.execSQL("UPDATE download SET complete_size = ? WHERE thread_id = ? AND download_url = ?",
                new Object[]{compeleteSize, threadId, urlstr});
    }
    /**
     * 关闭数据库
     */
    public void closeDb(){
        dbHelper.close();
    }

    /**
     *
     * @param url
     */
    public void deleteByUrl(String url){
        db.execSQL("DELETE  FROM download WHERE download_url=?",new Object[]{url});
    }
}
