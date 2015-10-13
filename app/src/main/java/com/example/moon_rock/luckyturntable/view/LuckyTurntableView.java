package com.example.moon_rock.luckyturntable.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.example.moon_rock.luckyturntable.R;

import java.lang.reflect.TypeVariable;

/**
 * Created by dreamtang860 on 10/12/15.
 */
public class LuckyTurntableView extends SurfaceViewTemplate {

    /**
     * 绘制的矩形区域
     */
    private RectF mRect;

    /**
     * 转盘背景
     */
    private Bitmap turntableBackground;
    private Bitmap startButton;
    private Bitmap stopButton;

    private int[] giftBitmapIDs = new int[]{R.drawable.gift_1, R.drawable.gift_2, R.drawable.gift_3, R.drawable.gift_4, R.drawable.gift_5, R.drawable.gift_6};
    private Bitmap[] giftBitmaps;
    private int lotteryIndex = 4;
    private String[] giftDesc = new String[]{"附近快递柜", "投递", "投递记录", "个人中心", "账户充值", "查询"};

    /**
     * 扇形的颜色
     */
    private final int[] sectorColors = new int[]{0xFF00FFF0, 0xFF0F0F0F};
    /**
     * 扇形的数量
     */
    private final int sectorCount = 6;

    /**
     * 转盘每个扇形的画笔
     */
    private Paint mSectorPaint;
    //图片的画笔
    private Paint mTextPaint;

    //圆盘内容半径(比背景小)
    private int radius;

    //当前转动的角度
    private double currentAngle = 0;

    //每个扇形的角度
    private int sweepAngle = 360 / sectorCount;

    //旋转速度
    private double mSpeed;
    private double stopDistance;
    private int centerWidth;

    private boolean stopLottery = false;

    //抽奖按钮的大小
    private int actionButtonSize;
    private final float height_divide_width = 1.2844f;

    public LuckyTurntableView(Context context) {
        super(context);
        init();
    }

    public LuckyTurntableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LuckyTurntableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        //转动的速度
        mSpeed = 0;

        //初始化绘制扇形的画笔
        mSectorPaint = new Paint();
        mSectorPaint.setAntiAlias(true);
        mSectorPaint.setDither(true);

        //初始化文字画笔
        mTextPaint = new Paint();
//        mTextPaint.setAntiAlias(true);
//        mTextPaint.setDither(true);
//        mTextPaint.setColor(0x00FF00);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        mTextPaint.setStrokeWidth(1);

        giftBitmaps = new Bitmap[sectorCount];

        for (int i = 0; i < sectorCount; i++) {
            giftBitmaps[i] = BitmapFactory.decodeResource(getResources(), giftBitmapIDs[i]);
        }

        startButton = BitmapFactory.decodeResource(getResources(), R.drawable.start_turn_icon);
        stopButton = BitmapFactory.decodeResource(getResources(), R.drawable.stop_turn_icon);

        setOnTouchListener(new OnLuckyTurntableTouchListener());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
        radius = (size - getPaddingLeft() * 2) / 2;
        centerWidth = size / 2;
        actionButtonSize = radius / 3;
        mRect = new RectF(getPaddingLeft(), getPaddingLeft(), getPaddingLeft() + radius * 2, getPaddingLeft() + radius * 2);
    }

    double runTotal = 0;

    @Override
    protected void doDraw(Canvas mCanvas) {

        if (null == mCanvas) {
            return;
        }

        drawBackground(mCanvas);
        drawSectors(mCanvas, currentAngle);
        drawGifts(mCanvas, currentAngle);
        drawText(mCanvas, (float) currentAngle, 360 / sectorCount);
        drawActionButton(mCanvas);

        currentAngle += mSpeed;

        runTotal += mSpeed;

        if (currentAngle >= 360) {
            currentAngle -= 360;
        }

        if (stopLottery) {
//            if (stopDistance > mSpeed) {
//                stopDistance -= mSpeed;
//            }
//
//            if (stopDistance <= mSpeed){
//                stopDistance--;
//                mSpeed--;
//            }

            mSpeed--;

            if (mSpeed <= 0) {
                Log.d("lk_test", "runTotal = " + runTotal);
                runTotal = 0;
                mSpeed = 0;
                stopDistance = 0;
                stopLottery = false;
                Log.d("lk_test", "抽奖结束了");
            }
        }

    }

    /**
     * 绘制圆盘背景
     *
     * @param mCanvas
     */
    private void drawBackground(Canvas mCanvas) {

        mCanvas.drawColor(0xFFFFFFFF);

        if (null == turntableBackground) {
            turntableBackground = BitmapFactory.decodeResource(getResources(), R.drawable.bg_turntable);
        }

        mCanvas.drawBitmap(turntableBackground, null, new Rect(0, 0, getPaddingLeft() * 2 + radius * 2, getPaddingLeft() * 2 + radius * 2), null);
    }

    private void drawSectors(Canvas mCanvas, double startAngle) {

        if (null == mCanvas) {
            return;
        }

        for (int i = 0; i < sectorCount; i++) {
            mSectorPaint.setColor(sectorColors[i % 2]);
            mCanvas.drawArc(mRect, (int) ((360 / sectorCount) * i + startAngle), 360 / sectorCount, true, mSectorPaint);
        }
    }

    private void drawGifts(Canvas mCanvas, double startAngle) {

        for (int i = 0; i < sectorCount; i++) {

            int imgSize = radius / 3;

            float angle = (float) ((startAngle + (360 / sectorCount) * i + (360 / sectorCount) / 2) * Math.PI / 180);

            double xLocate = centerWidth + Math.cos(angle) * (radius / 2);
            double yLocate = centerWidth + Math.sin(angle) * (radius / 2);

            mCanvas.drawBitmap(giftBitmaps[i], null, new Rect((int) (xLocate - imgSize / 2), (int) (yLocate - imgSize / 2), (int) (xLocate + imgSize / 2), (int) (yLocate + imgSize / 2)), null);
        }

    }

    private void drawText(Canvas mCanvas, float currentAngle, float sweepAngle) {
        for (int i = 0; null != giftDesc && i < giftDesc.length; i++) {
            float textLength = mTextPaint.measureText(giftDesc[i]);
            Path mPath = new Path();
            mPath.addArc(mRect, currentAngle + sweepAngle * i, sweepAngle);

            float hOffset = (float) (radius * 2 * Math.PI * (sweepAngle / 360));
            hOffset = hOffset / 2 - textLength / 2;
            float vOffset = radius / 6;
            mCanvas.drawTextOnPath(giftDesc[i], mPath, hOffset, vOffset, mTextPaint);
        }
    }

    private void drawActionButton(Canvas mCanvas) {

        if (mSpeed > 0) {
            //正在转动
            mCanvas.drawBitmap(stopButton, null, new Rect(centerWidth - actionButtonSize / 2, (int) (centerWidth - actionButtonSize * height_divide_width / 2), centerWidth + actionButtonSize / 2, (int) (centerWidth + actionButtonSize * height_divide_width / 2)), null);
        } else {
            //停止转动
            mCanvas.drawBitmap(startButton, null, new Rect(centerWidth - actionButtonSize / 2, (int) (centerWidth - actionButtonSize * height_divide_width / 2), centerWidth + actionButtonSize / 2, (int) (centerWidth + actionButtonSize * height_divide_width / 2)), null);
        }
    }

    private boolean isLotteryRunning() {
        return mSpeed > 0;
    }

    private class OnLuckyTurntableTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((centerWidth - actionButtonSize / 2 <= event.getX() && event.getX() <= centerWidth + actionButtonSize / 2) && (centerWidth - actionButtonSize * height_divide_width / 2 <= event.getY() && event.getY() <= centerWidth + actionButtonSize * height_divide_width / 2)) {
                    Log.d("lk_test", "lottery button click... action down");

                    Log.d("lk_test", "lottery button click... action up");
                    synchronized (LuckyTurntableView.class) {
                        if (!isLotteryRunning()) {
                            Log.d("lk_test", "stop:: stopDistance = " + stopDistance + " currentAngle = " + currentAngle + " lotteryIndex = " + lotteryIndex);

                            //lotteryIndex = Math.random() * 10 % 6;
                            double tmpAngle = currentAngle + lotteryIndex * (360 / sectorCount);
                            if (tmpAngle >= 360) {
                                tmpAngle %= 360;
                            }

                            Log.d("lk_test", "stop:: tmpAngle1 = " + tmpAngle + " currentAngle = " + currentAngle + " lotteryIndex = " + lotteryIndex);

//                            if (tmpAngle > 270 - 360 / sectorCount / 2 && tmpAngle < 270 + 360 / sectorCount / 2) {
//                                tmpAngle = tmpAngle - (270 - 360 / sectorCount / 2) ;
//                            } else if (tmpAngle >= 270 + 360 / sectorCount / 2) {
////                                tmpAngle = 270 + 360 / sectorCount / 2 + (360 - tmpAngle);
                            if (tmpAngle > 270 - 360 / sectorCount / 2) {
                                tmpAngle = 270 - 360/sectorCount/2  + 360 - tmpAngle;
                            } else {
                                tmpAngle = 270 - 360 / sectorCount / 2 - tmpAngle;
                            }

                            Log.d("lk_test", "stop:: tmpAngle2 = " + tmpAngle + " Math.randon() = " + Math.random());

                            tmpAngle += 360 * 5;
                            double randomNum = Math.random();
                            if (((int) (randomNum * 10)) % 2 == 0) {
                                Log.d("lk_test", "plus");
                                tmpAngle = tmpAngle + (360 / sectorCount / 2) * (randomNum > 0.9 ? 0.9 : randomNum
                                );
                            } else {
                                Log.d("lk_test", "minus");
                                tmpAngle = tmpAngle - (360 / sectorCount / 2) * (randomNum > 0.9 ? 0.9 : randomNum);
                            }

//                        (mSpeed + 0) * (mSpeed + 1)/2 = tmpAngle;
                            //mSpeed^2 + mSpeed - 2*tmpAngle = 0;
                            //mSpeed = (-1 + Math.sqrt(1 - 4 * 1 * (-2 * tmpAngle)))/2 //忽略负数解...该函数由 1 + 2 + 3 + ... + n = (n + 1)*n/2推导而得
                            stopDistance = (-1 + Math.sqrt(1 - 4 * 1 * (-2 * tmpAngle))) / 2;
                            Log.d("lk_test", "stopDistance = " + stopDistance + "  doubleStopDistance = " + ((-1 + Math.sqrt(1 - 4 * 1 * (-2 * tmpAngle))) / 2));

                            mSpeed = stopDistance;

                            stopLottery = true;
                        }
                    }
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
//                if ((centerWidth - actionButtonSize / 2 <= event.getX() && event.getX() <= centerWidth + actionButtonSize / 2) && (centerWidth - actionButtonSize * height_divide_width / 2 <= event.getY() && event.getY() <= centerWidth + actionButtonSize * height_divide_width / 2)) {
//                    Log.d("lk_test", "lottery button click... action up");
//                    if (!isLotteryRunning()) {
//                        mSpeed = (int) (-1 + Math.sqrt(1 - 4 * 1 * (-2 * 360 * 4))) / 2;
//                        return true;
//                    } else {
//                        if (!stopLottery) {
//                            Log.d("lk_test", "stop:: stopDistance = " + stopDistance + " currentAngle = " + currentAngle + " lotteryIndex = " + lotteryIndex);
//
//                            //lotteryIndex = Math.random() * 10 % 6;
//                            int tmpAngle = currentAngle + lotteryIndex * (360 / sectorCount);
//                            if (tmpAngle >= 360) {
//                                tmpAngle -= 360;
//                            }
//
//                            Log.d("lk_test", "stop:: tmpAngle1 = " + tmpAngle + " currentAngle = " + currentAngle + " lotteryIndex = " + lotteryIndex);
//
//                            if (tmpAngle >= 270 - 360 / sectorCount / 2 && tmpAngle <= 270 + 360 / sectorCount / 2) {
//                                tmpAngle = 0;
//                            } else if (tmpAngle >= 270 + 360 / sectorCount / 2) {
//                                tmpAngle = 270 + 360 / sectorCount / 2 + (360 - tmpAngle);
//                            } else {
//                                tmpAngle = 270 - 360 / sectorCount / 2 - tmpAngle;
//                            }
//
//                            Log.d("lk_test", "stop:: tmpAngle2 = " + tmpAngle);
//
//                            tmpAngle += 360 * 3;
//
////                        (mSpeed + 0) * (mSpeed + 1)/2 = tmpAngle;
//                            //mSpeed^2 + mSpeed - 2*tmpAngle = 0;
//                            //mSpeed = (-1 + Math.sqrt(1 - 4 * 1 * (-2 * tmpAngle)))/2 //忽略负数解...该函数由 1 + 2 + 3 + ... + n = (n + 1)*n/2推导而得
//                            stopDistance = (int) (-1 + Math.sqrt(1 - 4 * 1 * (-2 * tmpAngle))) / 2;
//                            Log.d("lk_test", "stopDistance = " + stopDistance);
//
//                            mSpeed = stopDistance;
//
//                            stopLottery = true;
//                        }
//                    }

//                }
            }
            return false;
        }

    }

}
