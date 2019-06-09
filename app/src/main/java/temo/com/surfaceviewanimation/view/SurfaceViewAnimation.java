package temo.com.surfaceviewanimation.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.RotateAnimation;

import temo.com.surfaceviewanimation.R;


public class SurfaceViewAnimation extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private String TAG = this.getClass().getSimpleName();
    private final SurfaceHolder mHolder;
    private Thread mThread;
    private long mFrameSpaceTime = 0;  // 每帧图片的间隔时间
    private boolean mIsDraw = true;     // 画图开关
    private int mCurrentIndex = 0;     // 当前正在播放的png
    public int mBitmapResourceIds[];
    private int mBitmapResourceId;
    private Bitmap mBitmap;
    private boolean isEnd = false;  // 动画是否结束
    private boolean isLoop = false;

    private OnAnimationStatusListener mAnimationStatusListener; // 动画监听

    public SurfaceViewAnimation(Context context) {
        this(context, null);
    }

    public SurfaceViewAnimation(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceViewAnimation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = this.getHolder();       // 获得surfaceholder
        mHolder.addCallback(this);        // 添加回调，这样三个方法才会执行
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明
        initFromAttributes(context, attrs, defStyleAttr);//获取布局中设置的信息
    }


    /**
     * 获取布局中设置的信息
     *
     * @param context      上下文
     * @param attrs        属性
     * @param defStyleAttr 默认属性
     */
    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SurfaceViewAnimation, defStyleAttr, 0);
        mBitmapResourceId = a.getResourceId(R.styleable.SurfaceViewAnimation_animationDrawable, View.NO_ID);
        mFrameSpaceTime = a.getInteger(R.styleable.SurfaceViewAnimation_duration, 0);
        isLoop = a.getBoolean(R.styleable.SurfaceViewAnimation_isLoop, false);
        a.recycle();
    }


    /**
     * 首先继承SurfaceView，并实现SurfaceHolder.Callback接口，实现它的三个方法：
     * surfaceCreated(SurfaceHolder holder)：surface创建的时候调用，一般在该方法中启动绘图的线程。
     * surfaceChanged(SurfaceHolder holder, int format, int width,int height)：surface尺寸发生改变的时候调用，如横竖屏切换。
     * surfaceDestroyed(SurfaceHolder holder) ：surface被销毁的时候调用，如退出游戏画面，一般在该方法中停止绘图线程。
     * 还需要获得SurfaceHolder，并添加回调函数，这样这三个方法才会执行。
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        mThread = new Thread(this);
//                mThread.start();
        if (mBitmapResourceIds == null) {
            Log.e(TAG, "surfaceCreated: 图片资源为空");
            return;
        }
        mIsDraw = true;
    }

    public void setBitmapResourceIds(int[] mBitmapResourceIds) {
        this.mBitmapResourceIds = mBitmapResourceIds;
    }

    public void setThread(Thread thread) {
        this.mThread = thread;
    }

    public void setDuration(long duration) {
        this.mFrameSpaceTime = duration;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDraw = false;
        try {
            Thread.sleep(mFrameSpaceTime);
            Log.d(TAG, "surfaceDestroyed: Thread " + mThread.getState());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        synchronized (mHolder) {         // 这里加锁为了以后控制这个绘制线程的wait与notify
            while (mIsDraw) {
                try {
                    drawView();
                    Thread.sleep(mFrameSpaceTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void rotateAnimation() {
        resize(getWidth(), getHeight());
        RotateAnimation rotateAnimation = new RotateAnimation(
                0.0f, 15.0f, 0.5f, 0.5f);
        rotateAnimation.setFillAfter(false);
        rotateAnimation.setDuration(200);
        startAnimation(rotateAnimation);
    }


    public void resize(int width, int height) {
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }

    // 开始
    public void start() {
        if (mThread == null) {
            mThread = new Thread(this);
        }
        Log.e("hhj", mThread.getName() + "==" + mThread.getState().name());

        isEnd = false;
        mIsDraw = true;
        mCurrentIndex = 0;

        if (mThread.getState() == Thread.State.TIMED_WAITING
                || mThread.getState() == Thread.State.BLOCKED) {
            mThread = new Thread(this);
            mThread.start();
        } else if (mThread.getState() == Thread.State.NEW) {
            mThread.start();
        }
    }

    private void drawView() {
        Log.i(TAG, "drawView: ");
        if (mAnimationStatusListener != null) {
            mAnimationStatusListener.onAnimationStart();
        }
        Canvas mCanvas = mHolder.lockCanvas();      // 锁定画布
        try {
            if (!isEnd) { // 未结束
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);        // 清除屏幕
                mBitmap = BitmapFactory.decodeResource(getResources(),
                        mBitmapResourceIds[mCurrentIndex]);

                int x = (getWidth() - mBitmap.getWidth()) / 2;
                int y = (getHeight() - mBitmap.getHeight()) / 2;
                mCanvas.drawBitmap(mBitmap, x, y, null);

                if (mCurrentIndex == mBitmapResourceIds.length - 1) {
                    mCurrentIndex = 0;

                    if (!isLoop) isEnd = true; // 只播放一次

                    if (mAnimationStatusListener != null) {
                        mAnimationStatusListener.onAnimationStart();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCurrentIndex++;
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);       // 提交画布
            }
            recycle(mBitmap);  // 这里回收资源非常重要！
        }
    }

    private void recycle(Bitmap mBitmap) {
        if (mBitmap != null)
            mBitmap.recycle();
    }


    public void setOnAnimationStatusListener(OnAnimationStatusListener listener) {
        this.mAnimationStatusListener = listener;
    }


    public interface OnAnimationStatusListener {
        void onAnimationStart();

        void onAnimationEnd();
    }
}