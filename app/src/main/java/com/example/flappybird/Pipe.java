package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.Random;

public class Pipe {
    /**
     * 上下管道的距离
     */
    private static final float RADIO_BETWEEN_UP_DOWN=1/5F;
    private static final float RADIO_MAX_HEIGHT=5/9F;
    private static final float RASIO_MIN_HEIGHT=1/5F;

    /**
     * 管道的横坐标
     */
    private int x;
    /**
     * 上管道的高度
     */
    private int height;
    /**
     * 上下管道间的距离
     */
    private int margin;
    /**
     * 上管道图片
     */
    private Bitmap mTop;
    /**
     * 下管道图片
     */
    private Bitmap mBottom;

    private static Random random=new Random();
    /**
     * 随机生成一个高度
     */
    private void randomHeight(int gameHeight){
        height=random.nextInt((int)(gameHeight*(RADIO_MAX_HEIGHT-RASIO_MIN_HEIGHT)));
        height=(int)(height+gameHeight*RASIO_MIN_HEIGHT);
    }

    public Pipe(Context context,int gameWidth,int gameHeight,Bitmap top,Bitmap bottom){
        //间隙是默认值
        margin=(int)(gameHeight*RADIO_BETWEEN_UP_DOWN);
        //默认从最右边出现
        x=gameWidth;
        //图片参数
        mTop=top;
        mBottom=bottom;
        //随机设置上管道高度
        randomHeight(gameHeight);

    }
    /**
     * 判断和鸟是否触碰
     */
    public boolean touchBird(Bird mBird){
        if(mBird.getX()+mBird.getmWidth()>x&&(mBird.getY()<height||mBird.getY()+mBird.getmHeight()>height+margin)){
            return true;
        }
        return false;
    }
    /**
     * 画自己
     */
    public void draw(Canvas mCanvas, RectF rectF){
        mCanvas.save();
        //rect为整个管道
        mCanvas.translate(x,-(rectF.bottom-height));
        mCanvas.drawBitmap(mTop,null,rectF,null);
        //下管道
        mCanvas.translate(0,(rectF.bottom-height)+height+margin);
        mCanvas.drawBitmap(mBottom,null,rectF,null);
        mCanvas.restore();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
}
