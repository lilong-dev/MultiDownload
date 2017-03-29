package com.ll.multidownload.bean;

/**
 * Created by admin on 2017/3/28.
 * each thread download info
 */

public class DownloadInfo {
    private int threadId;
    private int startPos;//下载的起始位置
    private int endPos;//下载的结束为止
    private int completeSize;//已经下载的文件大小
    private String downloadUrl;

    public  DownloadInfo(int threadId,int startPos,int endPos,int completeSize,String downloadUrl){
        this.threadId = threadId;
        this.startPos = startPos;
        this.endPos = endPos;
        this.completeSize = completeSize;
        this.downloadUrl = downloadUrl;
    }
    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getCompleteSize() {
        return completeSize;
    }

    public void setCompleteSize(int completeSize) {
        this.completeSize = completeSize;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
