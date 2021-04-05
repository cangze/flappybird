package com.example.flappybird;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

public class Floor {
    /**
     * 地板位置游戏面板高度的4/5到底部
     */
    private static final float FLOOR_Y_POS_PADIO=4/5F;

    /**
     * x,y坐标
     */
    private int x,y;
    /**
     * 填充物
     */
    private BitmapShader mFloorShader;
    private int mGameWidth;
    private int mGameHeight;

    public Floor(int mGameWidth, int mGameHeight, Bitmap floorBg){
        this.mGameWidth=mGameWidth;
        this.mGameHeight=mGameHeight;
        y=(int)(mGameHeight*FLOOR_Y_POS_PADIO);
        mFloorShader=new BitmapShader(floorBg, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
    }

    /**
     * 绘制自己
     */
    public void draw(Canvas mCanvas, Paint mPaint){
        if(-x>mGameWidth){
            x=x%mGameWidth;
        }
        mCanvas.save();
        //移动到指定的位置
        mCanvas.translate(x,y);
        mPaint.setShader(mFloorShader);
        mCanvas.drawRect(x,0,-x+mGameWidth,mGameHeight-y,mPaint);
        mCanvas.restore();
        mPaint.setShader(null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
