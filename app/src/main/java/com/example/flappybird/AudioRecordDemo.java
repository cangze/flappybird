package com.example.flappybird;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecordDemo {

    private static final String TAG="AudioRecord";
    static final int SAMPLE_RATE_IN_HZ=8000;
    static final int BUFFER_SIZE= AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun;
    Object mLock;
    int mvolume;
    Thread voicrThread;
    public AudioRecordDemo(){
        mLock=new Object();
    }
    public void getNoiseLevel(){
        if(isGetVoiceRun){
            Log.e(TAG,"it's still recording");
            return;
        }
        mAudioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ,AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT,BUFFER_SIZE);
        if(mAudioRecord==null){
            Log.e("SOUND","mAudioRecord初始化失败");
        }
        isGetVoiceRun=true;
        voicrThread=new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer=new short[BUFFER_SIZE];
                while (isGetVoiceRun){
                    //r是实际读取的数据长度，
                    int r=mAudioRecord.read(buffer,0,BUFFER_SIZE);
                    long v=0;
                    //将buffer内容去处，进行平方和运算
                    for (int i=0;i<buffer.length;i++){
                        v+=buffer[i]*buffer[i];
                    }
                    //平方和除以数据总长度，得到音量大小
                    double mean=v/(double)r;
                    double volume=10*Math.log10(mean);
                    mvolume=(int)volume;
                    Log.d(TAG,"分贝值"+volume);
                    //大概一秒十次
                    synchronized (mLock){
                        try {
                            mLock.wait(100);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord=null;
            }
        });
        voicrThread.start();
    }

    public int getMvolume() {
        return mvolume;
    }
}
