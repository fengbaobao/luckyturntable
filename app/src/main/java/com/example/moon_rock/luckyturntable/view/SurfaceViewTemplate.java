package com.example.moon_rock.luckyturntable.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by dreamtang860 on 10/12/15.
 */
public abstract class SurfaceViewTemplate extends SurfaceView {

    /**
     * SurfaceView中控制绘制生命周期的对象
     */
    protected SurfaceHolder mHolder;

    /**
     * 绘制图像的工具
     */
    protected Canvas mCanvas;

    /**
     * SurfaceView中进行绘制的线程
     */
    private Thread mDrawThread;

    /**
     * 控制是否绘制的标识符
     */
    private boolean isRunning;

    public SurfaceViewTemplate(Context context) {
        super(context);
        init();
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mHolder = getHolder();
        /**
         * 通过SurfaceHolder来管理绘制的生命周期
         */
        mHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                isRunning = true;
                mDrawThread = new Thread() {
                    @Override
                    public void run() {

                        super.run();

                        while (isRunning) {

                            long startDrawTime = System.currentTimeMillis();

                            try {
                                mCanvas = mHolder.lockCanvas();

                                if (null != mCanvas) {
                                    SurfaceViewTemplate.this.doDraw(mCanvas);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (null != mCanvas) {
                                    mHolder.unlockCanvasAndPost(mCanvas);
                                }
                            }

                            long endDrawTime = System.currentTimeMillis();
                            //每50毫秒绘制一次
                            if (endDrawTime - startDrawTime <= 50) {
                                try {
                                    Thread.sleep(50 - (endDrawTime - startDrawTime));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                };

                mDrawThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isRunning = false;
            }

        });

        //设置可获得交点
        setFocusable(true);
        setFocusableInTouchMode(true);

        //设置屏幕常亮
        setKeepScreenOn(true);

    }

    /**
     * User Canvas object to draw something... The Canvas object will not be null forever!
     *
     * @param mCanvas
     */
    protected abstract void doDraw(Canvas mCanvas);

}
