package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public class Bird {

    /**
     * 鸟在屏幕高度2/3的位置
     */
    private static final float RADIO_POS_HEIGHT= 1/3F;
    /**
     * 鸟的宽度30dp
     */
    private static final int BIRD_SIZE=30;
    /**
     * 鸟的横坐标,纵坐标
     */
    private int x,y;
    /**
     * 鸟的宽度
     */
    private int mWidth;
    /**
     * 鸟的高度
     */
    private int mHeight;
    /**
     * 鸟的画
     */
    private Bitmap bitmap;
    /**
     * 鸟绘制的范围
     */
    private RectF rect=new RectF();

    public Bird(Context context,int gameWidth,int gameHeight,Bitmap bitmap){
        this.bitmap=bitmap;
        //鸟的位置
        x=(gameWidth/2)-(bitmap.getWidth()/2);
        y=(int)(gameHeight*RADIO_POS_HEIGHT);

        //计算鸟的宽度和高度
        mWidth=Util.dp2px(context,BIRD_SIZE);
        mHeight=(int)(mWidth*1.0f/bitmap.getWidth()*bitmap.getHeight());

    }

    /**
     * 绘制自己
     */
    public void draw(Canvas canvas){
        rect.set(x,y,x+mWidth,y+mHeight);
        canvas.drawBitmap(bitmap,null,rect,null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
