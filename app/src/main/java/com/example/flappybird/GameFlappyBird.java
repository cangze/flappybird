package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;


public class GameFlappyBird extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private static final String GAMEFLAPPYBIRD="GameFlappyBird";
    private SurfaceHolder mHolder;

    /**
     * 与SurfaceHolder绑定的Canvas;
     */
    private Canvas mCanvas;

    /**
     * 用于绘制的线程
     */
    private Thread drawThread;

    /**
     * 用于线程的控制开关
     */
    private boolean isRunning;

    /**
     * 背景相关
     */
    private int mWidth;
    private int mHeight;
    private RectF mGamePanelRect =new RectF();
    private Bitmap background_bitmap;

    /**
     * 绘制背景
     */
    private void drawBg(){
        mCanvas.drawBitmap(background_bitmap,null,mGamePanelRect,null);
    }

    /**
     * **************鸟相关*****************
     */
    private Bird mBird;
    private Bitmap mBirdBitmap;
    /**
     * 绘制鸟
     */
    private void drawBird(){
        mBird.draw(mCanvas);
    }
    /**
     * ***************地板相关****************
     */
    private Paint mPaint;
    private Floor mFloor;
    private Bitmap mFloorBg;
    private int mSpeed;


    /**
     * 绘制地板
     */
    private void drawFloor(){
        mFloor.draw(mCanvas,mPaint);
    }

    /***************管道相关************
     */
    private Bitmap mPipeTop;
    private Bitmap mPipeBottom;
    private RectF mPipeRect;
    private int mPipeWidth;
    /**
     * 管道的宽度60dp;
     */
    private static final int PIPE_WIDTH=60;
    private List<Pipe> mPipes=new ArrayList<Pipe>();

    /**
     * 画管道
     */
    private void drawPipes(){
        for(Pipe pipe:mPipes){
            pipe.setX(pipe.getX()-mSpeed);
            pipe.draw(mCanvas,mPipeRect);
        }
    }

    /**
     * *******分数相关**************
     */
    private final int[] mNums=new int[]{R.drawable.n0,R.drawable.n1,
            R.drawable.n2,R.drawable.n3,R.drawable.n4,R.drawable.n5,
            R.drawable.n6,R.drawable.n7,R.drawable.n8,R.drawable.n9};
    private Bitmap[] mNumBitmap;
    private int mGrade=0;
    /**
     * 单个数字的高度的 1/15
     */
    private static final float RADIO_SINGLE_NUM_HEIGHT=1/15f;
    /**
     * 单个数字的宽度,高度
     */
    private int mSingleGradeWidth,mSingleGradeHeight;
    /**
     * 单个数字的范围
     */
    private RectF mSingleNumRectF;
    /**
     * 绘制分数
     */
    private void drawGrades(){
        String grade=mGrade+"";
        mCanvas.save();
        mCanvas.translate(mWidth/2-grade.length()*mSingleGradeWidth/2,
                1f/8 *mHeight);
        for(int i=0;i<grade.length();i++){
            String numStr=grade.substring(i,i+1);
            int num=Integer.valueOf(numStr);
            mCanvas.drawBitmap(mNumBitmap[num],null,mSingleNumRectF,null);
            mCanvas.translate(mSingleGradeWidth,0);
        }
        mCanvas.restore();
    }
    /**
     * 声音相关
     */
    private AudioRecordDemo audioRecordDemo=new AudioRecordDemo();

    /**
     *****************游戏的状态******************
     */
    private enum GameStatus{
        //默认情况下，WAITING
        //用户触屏是进入RUNNING
        //触碰到管道或者落到地上 GAMEOVER
        //GAMEOVER时，如果触碰到是管道，则让鸟落到地上后切换为WAITING
        WAITING,RUNNING,OVER
    }
    private GameStatus gameStatus=GameStatus.WAITING;
    /**
     * 触摸上升的距离，上升在这里是负值
     */
    private static final int TOUCH_UP_SIZE=-16;
    /**
     * 将上升的距离转化为px
     */
    private final int mBirdUPDirs=Util.dp2px(getContext(),TOUCH_UP_SIZE);
    private int mTmpBirdDis;
    /**
     * 鸟自动下落的距离
     */
    private final int mAutoDownSpeed=Util.dp2px(getContext(),2);
    /**
     * 两个管道间的距离
     */
    private final int PIPE_DIS_BETWEEN_TWO=Util.dp2px(getContext(),100);
    private int mTmpMoveDistance;
    /**
     * 记录需要移除的管道
     */
    private List<Pipe> mNeedRemovePipe=new ArrayList<Pipe>();
    private int mRemovedPipe = 0;

    /**
     * 检查是否碰撞
     */
    private void checkGameOver(){
        //如果触碰地板，gameOver
        if(mBird.getY()>mFloor.getY()-mBird.getmHeight()){
            gameStatus=GameStatus.OVER;
        }
        //如果撞到管道
        for(Pipe wall:mPipes){
            //已经穿过的
            if(wall.getX()+mPipeWidth<mBird.getX()){
                continue;
            }
            if(wall.touchBird(mBird)){
                gameStatus=GameStatus.OVER;
                break;
            }
        }
    }
    /**
     * 处理逻辑上的计算
     */
    private void logic(){
        switch (gameStatus){
            case RUNNING:

                mGrade=0;
                //更新地板绘制的x坐标，地板移动
                mFloor.setX(mFloor.getX()-mSpeed);

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
                mBird.setY(mBird.getY()-(volume-40)+mTmpBirdDis);
                Log.e("TAG",volume+"");
//                //默认下落，点击时瞬间上升
//                mTmpBirdDis+=mAutoDownSpeed;
//                mBird.setY(mBird.getY()+mTmpBirdDis);

                //计算分数
                mGrade+=mRemovedPipe;
                for(Pipe pipe:mPipes){
                    if (pipe.getX() + mPipeWidth < mBird.getX()){
                        mGrade++;
                    }
                }
                checkGameOver();
                break;
            case OVER:
                //鸟落下
                //如果鸟在空中，让他掉下来
                if (mBird.getY() < mFloor.getY() - mBird.getmHeight())
                {
                    mTmpBirdDis += mAutoDownSpeed;
                    mBird.setY(mBird.getY() + mTmpBirdDis);
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
        if(mPipes.isEmpty()){
            Log.e("CLEAR","mpipes is empty");
        }
        mNeedRemovePipe.clear();
        if(mNeedRemovePipe.isEmpty()){
            Log.e("mNeedRemovePipe","mNeedRemovePipe is empty");
        }
        //重置鸟的位置
        mBird.setY(mHeight * 1 / 3);
        //重置下落速度
        mTmpBirdDis = 0;
        mTmpMoveDistance = 0 ;
        mRemovedPipe=0;
        //重置分数
        mGrade=0;

//        audioRecordDemo.stopRecord();
        audioRecordDemo=null;
    }
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
                    gameStatus=GameStatus.RUNNING;
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
     * 初始化尺寸相关
     */
    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);

        mWidth=w;
        mHeight=h;
        mGamePanelRect.set(0,0,w,h);
        mBird=new Bird(getContext(),mWidth,mHeight,mBirdBitmap);
        mFloor=new Floor(mWidth,mHeight,mFloorBg);
        mPipeRect=new RectF(0,0,mPipeWidth,mHeight);
//        Pipe pipe=new Pipe(getContext(),w,h,mPipeTop,mPipeBottom);
//        mPipes.add(pipe);
        mSingleGradeHeight=(int)(h*RADIO_SINGLE_NUM_HEIGHT);
        mSingleGradeWidth=(int)(mSingleGradeHeight*1.0F/mNumBitmap[0].getHeight()*mNumBitmap[0].getWidth());
        mSingleNumRectF=new RectF(0,0,mSingleGradeWidth,mSingleGradeHeight);

    }

    /**
     * 加载图片
     */
    private Bitmap loadImageByResId(int resId){
        return BitmapFactory.decodeResource(getResources(),resId);
    }



    /**
     * 初始化图片
     */
    private void initBitmaps(){
        background_bitmap=loadImageByResId(R.drawable.bg1);
        mBirdBitmap=loadImageByResId(R.drawable.b1);
        mFloorBg=loadImageByResId(R.drawable.floor_bg2);
        mPipeTop=loadImageByResId(R.drawable.g2);
        mPipeBottom=loadImageByResId(R.drawable.g1);
        mNumBitmap=new Bitmap[mNums.length];
        for(int i=0;i<mNumBitmap.length;i++){
            mNumBitmap[i]=loadImageByResId(mNums[i]);
        }

    }

    public GameFlappyBird(Context context){
        this(context,null);
    }

    public GameFlappyBird(Context context, AttributeSet attrs)    {
        super(context, attrs);

        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        initBitmaps();

        mSpeed=Util.dp2px(getContext(),2);
        mPipeWidth=Util.dp2px(getContext(),PIPE_WIDTH);
        mHolder = getHolder();
        mHolder.addCallback(this);

        setZOrderOnTop(true);// 设置画布 背景透明
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        audioRecordDemo=new AudioRecordDemo();
        audioRecordDemo.getNoiseLevel();
        // 设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        // 设置常亮
        this.setKeepScreenOn(true);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //开启线程
        isRunning=true;
        drawThread=new Thread(this);
        drawThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //通知关闭线程
        isRunning=false;
    }

    @Override
    public void run() {
        //如果还没有销毁
        while (isRunning){
            //开始时间
            long start=System.currentTimeMillis();
            logic();
            draw();
            //结束时间
            long end=System.currentTimeMillis();
            try {
                if(end-start<50){
                    Thread.sleep(50-(end-start));
                }
            }catch (InterruptedException e){
                Log.e(GAMEFLAPPYBIRD,e.getStackTrace().toString());
            }
        }
    }


    private void draw(){
        //画图
        try {
            //获得canvas
            mCanvas=mHolder.lockCanvas();
            if(mCanvas!=null){
                //在这里画画
                drawBg();
                drawBird();
                drawPipes();
                drawFloor();
                drawGrades();
                //更新地板绘制的x坐标
                mFloor.setX(mFloor.getX()-mSpeed);
            }
        }catch (Exception e){
            Log.e(GAMEFLAPPYBIRD,e.getMessage());
        }finally {
            if(mCanvas!=null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }

    }
}
