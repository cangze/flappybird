package com.example.flappybird;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class Gamebird extends SurfaceView implements Callback, Runnable
{

    private SurfaceHolder mHolder;
    /**
     * 与SurfaceHolder绑定的Canvas
     */
    private Canvas mCanvas;
    /**
     * 用于绘制的线程
     */
    private Thread t;
    /**
     * 线程的控制开关
     */
    private boolean isRunning;

    //背景相关
    private Bitmap mBackgroud_bitmap;
    private int mWidth,mHeight;
    private RectF mGamePanelRect=new RectF();

    //鸟相关
    private Bird bird;
    private Bitmap mBird_bitmap;

    //管道相关
    private Bitmap mPipeTop;
    private Bitmap mPipeBottom;
    private RectF mPipeRect;
    private int mPipeWidth;
    private int mSpeed;
    //管道图像宽度
    private static final int PIPE_WIDTH = 60;

    private List<Pipe> mPipes = new ArrayList<Pipe>();

    //加载图片
    private Bitmap loadBitmapByResId(int resId){
        return BitmapFactory.decodeResource(getResources(), resId);
    }
    //初始化图片
    private void initBitmap(){
        mBackgroud_bitmap=loadBitmapByResId(R.drawable.bg1);
        mBird_bitmap=loadBitmapByResId(R.drawable.b1);
        mPipeTop=loadBitmapByResId(R.drawable.g2);
        mPipeBottom=loadBitmapByResId(R.drawable.g1);
    }
    //画背景
    private void drawBg(){
        mCanvas.drawBitmap(mBackgroud_bitmap,null,mGamePanelRect,null);
    }
    //画鸟
    private void drawBird(){
        bird.draw(mCanvas);
    }
    //画管道
    private void drawPipes(){
        //画出管道List中的每一个
        for(Pipe pipe:mPipes){
            pipe.setX(pipe.getX()-mSpeed);
            pipe.draw(mCanvas,mPipeRect);
        }
    }

    //游戏状态枚举变量
    private enum GameStatus
    {
        WAITING, RUNNING, OVER
    }
    //记录游戏的状态*/
    private GameStatus gameStatus = GameStatus.WAITING;

    //触摸上升的距离，因为是上升，所以为负值
    private static final int TOUCH_UP_SIZE = -16;
    //将上升的距离转化为px；这里多存储一个变量，变量在run中计算
    private final int mBirdUPDirs = Util.dp2px(getContext(), TOUCH_UP_SIZE);
    //用于计算的变量
    private int mTmpBirdDis;
    // 鸟自动下落的距离
    private final int mAutoDownSpeed = Util.dp2px(getContext(), 2);
    // 记录需要移除的管道
    private List<Pipe> mNeedRemovePipe=new ArrayList<Pipe>();
    private int mRemovedPipe = 0;
    // 两个管道间的距离
    private final int PIPE_DIS_BETWEEN_TWO=Util.dp2px(getContext(),100);
    //管道移动的距离
    private int mTmpMoveDistance;

    //声音
    private AudioRecordDemo audioRecordDemo;
    /**
     * 重写TouchEvent
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action=event.getAction();

        if(action==MotionEvent.ACTION_DOWN){
            switch (gameStatus){
                //触屏时如果是WAITTING转为RUNNING
                case WAITING:
                    //声音开始加载
                    gameStatus= GameStatus.RUNNING;
                    audioRecordDemo=new AudioRecordDemo();
                    audioRecordDemo.getNoiseLevel();
                    break;
                //触屏时如果是RUNNING让鸟的高度升高
                case RUNNING:
                    mTmpBirdDis=mBirdUPDirs;
                    break;
            }
        }
        return true;
    }

    /**
     * 处理逻辑上的计算
     */
    private void logic(){
        switch (gameStatus){
            case RUNNING:

                for(Pipe pipe:mPipes){
                    //当管道移出屏幕时，移除管道
                    if(pipe.getX()<-mPipeWidth){
                        mNeedRemovePipe.add(pipe);
                        mRemovedPipe++;
                        continue;
                    }
                    //管道移动
                    pipe.setX(pipe.getX()-mSpeed);
                }
                //现存管道中出去可以删除的管道
                mPipes.removeAll(mNeedRemovePipe);
                Log.e("TAG","现存管道数量"+mPipes.size());

                //管道移动的距离
                mTmpMoveDistance+=mSpeed;

                //生成一个管道
                if(mTmpMoveDistance>=PIPE_DIS_BETWEEN_TWO){
                    Pipe pipe=new Pipe(getContext(),getWidth(),getHeight(),
                            mPipeTop,mPipeBottom);
                    mPipes.add(pipe);
                    mTmpMoveDistance=0;
                }
                //在这里处理鸟的高度
                int volume=audioRecordDemo.getMvolume();
//                mTmpBirdDis+=mAutoDownSpeed;
                bird.setY(bird.getY()-(volume-40)+mTmpBirdDis);
                Log.e("TAG",volume+"");
                //默认下落，点击时瞬间上升
//                mTmpBirdDis+=mAutoDownSpeed;
//                bird.setY(bird.getY()+mTmpBirdDis);
                checkGameOver();
                break;
            case OVER:
                //鸟落下
                //如果鸟在空中，让他掉下来
                if (bird.getY() < mHeight)
                {
                    mTmpBirdDis += mAutoDownSpeed;
                    bird.setY(bird.getY() + mTmpBirdDis);
                } else
                {
                    gameStatus = GameStatus.WAITING;
                    initPos();
                }
                break;
            default:
                break;
        }
    }
    /**
     * 重置鸟的位置等数据
     */
    private void initPos()
    {
        mPipes.clear();
        mNeedRemovePipe.clear();
        //重置鸟的位置
        bird.setY(mHeight * 1 / 3);
        //重置下落速度
        mTmpBirdDis = 0;
        mTmpMoveDistance = 0 ;
        mRemovedPipe=0;
        audioRecordDemo=null;

    }
    private void checkGameOver()
    {

        // 如果触碰地板，gg
        if (bird.getY() > mHeight)
        {
            gameStatus = GameStatus.OVER;
        }
        // 如果撞到管道
        for (Pipe wall : mPipes)
        {
            //已经穿过的
            if (wall.getX() + mPipeWidth < bird.getX())
            {
                continue;
            }
            if (wall.touchBird(bird))
            {
                gameStatus = GameStatus.OVER;
                break;
            }
        }
    }
    public Gamebird(Context context)
    {
        this(context, null);
    }

    public Gamebird(Context context, AttributeSet attrs){
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);
        setZOrderOnTop(true);// 设置画布 背景透明
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        audioRecordDemo=new AudioRecordDemo();
        audioRecordDemo.getNoiseLevel();
        initBitmap();
        //初始化管道宽度
        mPipeWidth=Util.dp2px(getContext(),PIPE_WIDTH);
        // 设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        // 设置常亮
        this.setKeepScreenOn(true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
        mGamePanelRect.set(0, 0, w, h);
        // 初始化mBird
        bird = new Bird(getContext(), mWidth, mHeight, mBird_bitmap);
        // 初始化速度
        mSpeed = Util.dp2px(getContext(), 2);
        // 初始化管道范围
        mPipeRect = new RectF(0, 0, mPipeWidth, mHeight);
//        Pipe pipe = new Pipe(getContext(), w, h, mPipeTop, mPipeBottom);
//        mPipes.add(pipe);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {

        // 开启线程
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // 通知关闭线程
        isRunning = false;
    }

    @Override
    public void run()
    {
        while (isRunning)
        {
            long start = System.currentTimeMillis();
            draw();
            logic();
            long end = System.currentTimeMillis();

            try
            {
                if (end - start < 50)
                {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }

    }

    private void draw()
    {
        try
        {
            // 获得canvas
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null)
            {
                // drawSomething..
                drawBg();
                drawBird();
                drawPipes();
            }
        } catch (Exception e)
        {
        } finally
        {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }
}