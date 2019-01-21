package com.wang.baseadapter.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;

import androidx.core.content.ContextCompat;

import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.wang.baseadapter.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaveSideBarView extends View {

    // 计算波浪贝塞尔曲线的角弧长值
    private static final double ANGLE = Math.PI * 45 / 180;
    private static final double ANGLE_R = Math.PI * 90 / 180;
    private OnTouchLetterChangeListener listener;

    // 渲染字母表
    private List<String> mLetters;

    // 当前选中的位置
    private int mChoose = -1;
    private int oldChoose;
    private int newChoose;

    // 字母列表画笔
    private Paint mLettersPaint = new Paint();

    // 提示字母画笔
    private Paint mTextPaint = new Paint();
    // 波浪画笔
    private Paint mWavePaint = new Paint();

    private float mTextSize;
    private float mLargeTextSize;
    private int mTextColor;
    private int mWaveColor;
    private int mTextColorChoose;
    private int mBarColor;
    private int mWidth;
    private int mHeight;
    private int mItemHeight;
    private int mPadding;
    private int mLettersMaxLength;

    private boolean mAutoHide = false;
    private int mShowDuration = 3000;

    // 波浪路径
    private Path mWavePath = new Path();

    // 圆形路径
    private Path mBallPath = new Path();

    // 手指滑动的Y点作为中心点
    private int mCenterY; //中心点Y

    // 贝塞尔曲线的分布半径
    private int mRadius;

    // 圆形半径
    private int mBallRadius;
    // 用于过渡效果计算
    ValueAnimator mRatioAnimator;
    //显示和隐藏动画
    ValueAnimator mShowHideAnimator;
    //是否显示
    private boolean mShowing = true;
    //x偏移量
    private float mTranslateX = 0;

    private RectF mLettersRect = new RectF();

    // 用于绘制贝塞尔曲线的比率
    private float mRatio;

    // 选中字体的坐标
    private float mPosX, mPosY;

    // 圆形中心点X
    private float mBallCentreX;

    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            hide(true);
        }
    };

    public WaveSideBarView(Context context) {
        this(context, null);
    }

    public WaveSideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedValue primaryColorTypedValue = new TypedValue();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.getTheme().resolveAttribute(android.R.attr.colorAccent, primaryColorTypedValue, true);
                mTextColor = primaryColorTypedValue.data;
                mTextColorChoose = mTextColor;
            } else {
                throw new RuntimeException("SDK_INT less than LOLLIPOP");
            }
        } catch (Exception e) {
            try {
                int colorAccentId = getResources().getIdentifier("colorAccent", "attr", getContext().getPackageName());
                if (colorAccentId != 0) {
                    context.getTheme().resolveAttribute(colorAccentId, primaryColorTypedValue, true);
                    mTextColor = primaryColorTypedValue.data;
                    mTextColorChoose = mTextColor;
                } else {
                    throw new RuntimeException("colorPrimary not found");
                }
            } catch (Exception e1) {
                mTextColor = ContextCompat.getColor(context, R.color.side_bar_text_color);
                mTextColorChoose = Color.WHITE;
            }
        }

        mWaveColor = ContextCompat.getColor(context, R.color.side_bar_wave_color);
        mBarColor = ContextCompat.getColor(context, R.color.side_bar_background);

        mTextSize = context.getResources().getDimensionPixelSize(R.dimen.textSize_sidebar);
        mLargeTextSize = context.getResources().getDimensionPixelSize(R.dimen.large_textSize_sidebar);
        mPadding = context.getResources().getDimensionPixelSize(R.dimen.textSize_sidebar_padding);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WaveSideBarView);
            mTextColor = a.getColor(R.styleable.WaveSideBarView_sidebarTextColor, mTextColor);
            mTextColorChoose = a.getColor(R.styleable.WaveSideBarView_sidebarChooseTextColor, mTextColorChoose);
            mTextSize = a.getDimension(R.styleable.WaveSideBarView_sidebarTextSize, mTextSize);
            mLargeTextSize = a.getDimension(R.styleable.WaveSideBarView_sidebarLargeTextSize, mLargeTextSize);
            mWaveColor = a.getColor(R.styleable.WaveSideBarView_sidebarWaveColor, mWaveColor);
            mBarColor = a.getColor(R.styleable.WaveSideBarView_sidebarBackgroundColor, mBarColor);
            mRadius = a.getDimensionPixelSize(R.styleable.WaveSideBarView_sidebarRadius, context.getResources().getDimensionPixelSize(R.dimen.radius_sidebar));
            mBallRadius = a.getDimensionPixelSize(R.styleable.WaveSideBarView_sidebarBallRadius, context.getResources().getDimensionPixelSize(R.dimen.ball_radius_sidebar));
            mLettersMaxLength = a.getInteger(R.styleable.WaveSideBarView_sidebarLettersMaxLength, 1);
            mAutoHide = a.getBoolean(R.styleable.WaveSideBarView_sidebarAutoHide, mAutoHide);
            mShowDuration = a.getInteger(R.styleable.WaveSideBarView_sidebarShowDuration, mShowDuration);
            mShowing = a.getBoolean(R.styleable.WaveSideBarView_sidebarShow, mShowing);
            CharSequence[] letters = a.getTextArray(R.styleable.WaveSideBarView_sidebarLetters);
            mLetters = new ArrayList<>();
            if (letters != null) {
                for (CharSequence letter : letters) {
                    mLetters.add(letter.toString());
                }
            } else {
                mLetters.addAll(Arrays.asList(context.getResources().getStringArray(R.array.waveSideBarLetters)));
            }
            a.recycle();
        }

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mWaveColor);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColorChoose);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mLargeTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        if (mShowing && mAutoHide) {
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    if (mShowing){
                        removeCallbacks(mHide);
                        postDelayed(mHide, mShowDuration);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        final float y = event.getY();
        final float x = event.getX();

        oldChoose = mChoose;
        newChoose = (int) (y / mHeight * mLetters.size());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x < mWidth - 2 * mRadius) {
                    return false;
                }
                removeCallbacks(mHide);
                if (!mShowing) {
                    if (mRatioAnimator != null && mRatioAnimator.isRunning() && mTranslateX == 0f) {
                        mShowing = true;
                    } else {
                        show(true);
                    }
                }
                mCenterY = (int) y;
                startRatioAnimator(mRatio, 1.0f);
                break;
            case MotionEvent.ACTION_MOVE:
                removeCallbacks(mHide);

                mCenterY = (int) y;
                if (oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < mLetters.size()) {
                        mChoose = newChoose;
                        if (listener != null && mTranslateX == 0f) {
                            listener.onLetterChange(mLetters.get(newChoose));
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startRatioAnimator(mRatio, 0f);
                mChoose = -1;
                break;
            default:
                break;
        }

        return true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        mItemHeight = (mHeight - mPadding) / mLetters.size();
        mPosX = mWidth - (1.1f + (float) mLettersMaxLength / 2) * mTextSize;

        mLettersRect.left = mPosX - mTextSize * ((float) mLettersMaxLength + 1) / 2;
        mLettersRect.right = mPosX + mTextSize * ((float) mLettersMaxLength + 1) / 2;
        mLettersRect.top = mTextSize / 2;
        mLettersRect.bottom = mHeight - mTextSize / 2;

        if (!mShowing) {
            mRatio = 0f;
            mTranslateX = mWidth - mLettersRect.left + mTextSize * 0.6f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制字母列表
        drawLetters(canvas);

        //绘制波浪
        drawWavePath(canvas);

        //绘制圆
        drawBallPath(canvas);

        //绘制选中的字体
        drawChooseText(canvas);

    }

    private void drawLetters(Canvas canvas) {
        canvas.save();
        canvas.translate(mTranslateX, 0);
        mLettersPaint.reset();
        mLettersPaint.setStyle(Paint.Style.FILL);
        mLettersPaint.setColor(mBarColor);
        mLettersPaint.setAntiAlias(true);
        canvas.drawRoundRect(mLettersRect, mTextSize, mTextSize, mLettersPaint);

        mLettersPaint.reset();
        mLettersPaint.setStyle(Paint.Style.STROKE);
        mLettersPaint.setColor(mTextColor);
        mLettersPaint.setAntiAlias(true);
        canvas.drawRoundRect(mLettersRect, mTextSize, mTextSize, mLettersPaint);

        for (int i = 0; i < mLetters.size(); i++) {
            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColor);
            mLettersPaint.setAntiAlias(true);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetrics fontMetrics = mLettersPaint.getFontMetrics();
            float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);

            float posY = mItemHeight * i + baseline / 2 + mPadding;

            if (i == mChoose) {
                mPosY = posY;
            } else {
                canvas.drawText(mLetters.get(i), mPosX, posY, mLettersPaint);
            }
        }
        canvas.restore();
    }

    private void drawChooseText(Canvas canvas) {
        if (mChoose != -1) {
            // 绘制右侧选中字符
            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColorChoose);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mLetters.get(mChoose), mPosX, mPosY, mLettersPaint);

            // 绘制提示字符
            if (mRatio >= 0.9f) {
                String target = mLetters.get(mChoose);
                mTextPaint.setTextSize(mLargeTextSize / target.length());
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);
                float x = mBallCentreX;
                float y = mCenterY + baseline / 2;
                canvas.drawText(target, x, y, mTextPaint);
            }
        }
    }

    /**
     * 绘制波浪
     *
     * @param canvas
     */
    private void drawWavePath(Canvas canvas) {
        mWavePath.reset();
        // 移动到起始点
        mWavePath.moveTo(mWidth, mCenterY - 3 * mRadius);
        //计算上部控制点的Y轴位置
        int controlTopY = mCenterY - 2 * mRadius;

        //计算上部结束点的坐标
        int endTopX = (int) (mWidth - mRadius * Math.cos(ANGLE) * mRatio);
        int endTopY = (int) (controlTopY + mRadius * Math.sin(ANGLE));
        mWavePath.quadTo(mWidth, controlTopY, endTopX, endTopY);

        //计算中心控制点的坐标
        int controlCenterX = (int) (mWidth - 1.8f * mRadius * Math.sin(ANGLE_R) * mRatio);
        int controlCenterY = mCenterY;
        //计算下部结束点的坐标
        int controlBottomY = mCenterY + 2 * mRadius;
        int endBottomX = endTopX;
        int endBottomY = (int) (controlBottomY - mRadius * Math.cos(ANGLE));
        mWavePath.quadTo(controlCenterX, controlCenterY, endBottomX, endBottomY);

        mWavePath.quadTo(mWidth, controlBottomY, mWidth, controlBottomY + mRadius);

        mWavePath.close();
        canvas.drawPath(mWavePath, mWavePaint);
    }

    private void drawBallPath(Canvas canvas) {
        //x轴的移动路径
        mBallCentreX = (mWidth + mBallRadius) - (2.0f * mRadius + 2.0f * mBallRadius) * mRatio;

        mBallPath.reset();
        mBallPath.addCircle(mBallCentreX, mCenterY, mBallRadius, Path.Direction.CW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBallPath.op(mWavePath, Path.Op.DIFFERENCE);
        } else {
            canvas.clipPath(mWavePath, Region.Op.DIFFERENCE);
        }

        mBallPath.close();
        canvas.drawPath(mBallPath, mWavePaint);

    }

    public void hideAfterTime() {
        removeCallbacks(mHide);
        postDelayed(mHide, mShowDuration);
    }

    public void hide() {
        hide(true);
    }

    public void hide(boolean anim) {
        removeCallbacks(mHide);
        if (mShowing) {
            if (anim) {
                if ((mRatioAnimator == null || !mRatioAnimator.isRunning()) && mTranslateX != mWidth - mLettersRect.left + mTextSize * 0.6f) {
                    startShowHideAnimator(mTranslateX, mWidth - mLettersRect.left + mTextSize * 0.6f);
                }
            } else {
                if (mRatioAnimator != null && mRatioAnimator.isRunning()) {
                    mRatioAnimator.cancel();
                }
                if (mShowHideAnimator != null && mShowHideAnimator.isRunning()) {
                    mShowHideAnimator.cancel();
                }
                if (mRatio != 0f || mTranslateX != mWidth - mLettersRect.left + mTextSize * 0.6f) {
                    mRatio = 0f;
                    mTranslateX = mWidth - mLettersRect.left + mTextSize * 0.6f;
                    invalidate();
                }
            }
        }
        mShowing = false;
    }

    public void showAfterHide() {
        show(true);
        postDelayed(mHide, mShowDuration);
    }

    public void show() {
        show(true);
    }

    public void show(boolean anim) {
        removeCallbacks(mHide);
        if (!mShowing) {
            if (anim) {
                if (mTranslateX != 0f) {
                    startShowHideAnimator(mTranslateX, 0);
                }
            } else {
                if (mRatioAnimator != null && mRatioAnimator.isRunning()) {
                    mRatioAnimator.cancel();
                }
                if (mShowHideAnimator != null && mShowHideAnimator.isRunning()) {
                    mShowHideAnimator.cancel();
                }
                if (mRatio != 1f || mTranslateX != 0f) {
                    mRatio = 1f;
                    mTranslateX = 0f;
                    invalidate();
                }
            }
        }
        mShowing = true;
    }


    private void startRatioAnimator(float... value) {
        if (mRatioAnimator == null) {
            mRatioAnimator = new ValueAnimator();
        }
        mRatioAnimator.cancel();
        mRatioAnimator.setFloatValues(value);
        mRatioAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                mRatio = (float) value.getAnimatedValue();
                //球弹到位的时候，并且点击的位置变了，即点击的时候显示当前选择位置
                if (mRatio == 1f && oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < mLetters.size()) {
                        mChoose = newChoose;
                        if (listener != null) {
                            listener.onLetterChange(mLetters.get(newChoose));
                        }
                    }
                } else if (mRatio == 0f) {
                    if (!mShowing && mShowHideAnimator != null && !mShowHideAnimator.isRunning()) {
                        startShowHideAnimator(mTranslateX, mWidth - mLettersRect.left + mTextSize * 0.6f);
                    } else if (mAutoHide) {
                        removeCallbacks(mHide);
                        postDelayed(mHide, mShowDuration);
                    }
                }
                invalidate();
            }
        });
        mRatioAnimator.start();
    }


    private void startShowHideAnimator(float... value) {
        if (mShowHideAnimator == null) {
            mShowHideAnimator = new ValueAnimator();
        }
        mShowHideAnimator.cancel();
        mShowHideAnimator.setFloatValues(value);
        mShowHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                mTranslateX = (float) value.getAnimatedValue();
                invalidate();
            }
        });
        mShowHideAnimator.start();
    }


    public void setOnTouchLetterChangeListener(OnTouchLetterChangeListener listener) {
        this.listener = listener;
    }

    public List<String> getLetters() {
        return mLetters;
    }

    public void setLetters(List<String> letters) {
        this.mLetters = letters;
        invalidate();
    }

    public void add(String letters) {
        mLetters.add(letters);
        requestLayout();
    }

    public void remove(String letters) {
        mLetters.remove(letters);
        requestLayout();
    }

    public interface OnTouchLetterChangeListener {
        void onLetterChange(String letter);
    }
}