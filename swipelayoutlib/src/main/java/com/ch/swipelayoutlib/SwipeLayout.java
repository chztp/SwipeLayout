package com.ch.swipelayoutlib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by chztp
 */

public class SwipeLayout extends ViewGroup {

    /**
     * 是否可以滑动
     */
    private boolean swipeEnable;

    /**
     * 速度追踪
     */
    private VelocityTracker velocityTracker;

    /**
     * 最大速度
     */
    private int maxVelocity;

    /**
     * 右边菜单的宽度
     */
    private int rightWidth;

    /**
     * 内容区域的宽度
     */
    private int contentWidth;

    /**
     * 控件高度
     */
    private int height;

    /**
     * 展开动画
     */
    private ValueAnimator extendAnimator;

    /**
     * 关闭动画
     */
    private ValueAnimator closeAnimator;

    /**
     * 触摸开始的点
     */
    private PointF startPoint = new PointF();


    /**
     * 触摸移动的点
     */
    private PointF movePoint = new PointF();

    /**
     * 滑动判断的标准值
     */
    private int mScaleSlop;

    /**
     * 滑动的点
     */
    private int pointId;

    /**
     * 是否是滑动
     */
    private boolean isSwiped = false;

    /**
     * 滑动临界值
     */
    private int mLimit;

    /**
     * 是否只展开一项（列表中会用到此属性）
     */
    private boolean onlyExpandOne;

    /**
     * 已打开的组件
     */
    private static SwipeLayout expandView;

    /**
     * 是否处于触摸
     */
    private boolean isTouching;

    public void setSwipeEnable(boolean swipeEnable) {
        this.swipeEnable = swipeEnable;
    }

    public void setSwiped(boolean swiped) {
        isSwiped = swiped;
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }


    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context, attrs, defStyleAttr);
    }

    private void initData(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout, defStyleAttr, 0);
        swipeEnable = typedArray.getBoolean(R.styleable.SwipeLayout_enable, true);
        onlyExpandOne = typedArray.getBoolean(R.styleable.SwipeLayout_onlyExpandOne, true);
        typedArray.recycle();
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mScaleSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setClickable(true);
        height = 0;
        rightWidth = 0;
        int childrenCount = getChildCount();
        boolean parentMeasure = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

        //是否需要重新计算子组件的高度
        boolean isNeedMeasureChildHeight = false;
        for (int i = 0; i < childrenCount; i++) {
            View childView = getChildAt(i);
            childView.setClickable(true);
            if (childView.getVisibility() != View.GONE) {
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);

                LayoutParams layoutParams = childView.getLayoutParams();
                height = Math.max(height, childView.getMeasuredHeight());

                //如果组件的高度设置为wrap_content,子组件的高度设置为match_parent,则需要重新计算子组件的高度
                if (parentMeasure && layoutParams.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureChildHeight = true;
                }
                if (i > 0) {
                    rightWidth += childView.getMeasuredWidth();
                } else {
                    contentWidth = childView.getMeasuredWidth();
                }
            }
            mLimit = contentWidth / 5;
        }

        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentWidth, getPaddingTop() + getPaddingBottom() + height);

        if (isNeedMeasureChildHeight) {
            forceMeasureChildHeight(childrenCount, widthMeasureSpec);
        }
    }

    private void forceMeasureChildHeight(int childrenCount, int widthMeasureSpec) {
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        for (int i = 0; i < childrenCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                LayoutParams layoutParams = childView.getLayoutParams();
                if (layoutParams.height == LayoutParams.MATCH_PARENT) {

                    measureChild(childView, widthMeasureSpec, heightMeasureSpec);

                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childrenCount = getChildCount();
        int childLeft = getPaddingLeft();
        for (int i = 0; i < childrenCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                childView.layout(childLeft, getPaddingTop(),
                        childLeft + childView.getMeasuredWidth(), getPaddingTop() + childView.getMeasuredHeight());
                childLeft += childView.getMeasuredWidth();
            }
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        if (this == expandView) {
            expandView.smoothClose();
            expandView = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (swipeEnable) {
            if (null == velocityTracker) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            VelocityTracker velocityTracker1 = velocityTracker;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("ACTION_DOWN", event.getX() + "," + event.getY());
                    isSwiped = false;
                    onlyExpandOne = false;
                    if (isTouching) {
                        return false;
                    } else {
                        isTouching = true;//第一个摸的指头，赶紧改变标志，宣誓主权。
                    }
                    startPoint.set(event.getX(), event.getY());
                    movePoint.set(event.getX(), event.getY());
                    if (null != expandView) {
                        if (this != expandView) {
                            expandView.smoothClose();
                            onlyExpandOne = true;
                        }
                        //如果有一个展开，就屏蔽父组件的点击事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    pointId = event.getPointerId(0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d("ACTION_MOVE", event.getX() + "," + event.getY());
                    if (onlyExpandOne) {
                        break;
                    }

                    float moveDistance = movePoint.x - event.getX();

                    if (Math.abs(moveDistance) > 10 || Math.abs(getScrollX()) > 10) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }

                    scrollBy((int) moveDistance, 0);

                    //越界处理
                    if (getScrollX() < 0) {
                        scrollTo(0, 0);
                    }

                    if (getScrollX() > rightWidth) {
                        scrollTo(rightWidth, 0);
                    }
                    movePoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("ACTION_UP", event.getX() + "," + event.getY());

                    if (Math.abs(event.getRawX() - startPoint.x) > mScaleSlop) {
                        isSwiped = true;
                    }
                    //如果有展开项，则不再继续操作
                    if (!onlyExpandOne) {
                        velocityTracker1.computeCurrentVelocity(1000, maxVelocity);
                        float velocityX = velocityTracker1.getXVelocity(pointId);
                        Log.d("velocityX", "velocityX=" + velocityX);
                        if (Math.abs(velocityX) > 1000) {//滑动速度超过阈值
                            if (velocityX < -1000) {
                                //平滑展开Menu
                                smoothExpand();
                            } else {
                                // 平滑关闭Menu
                                smoothClose();
                            }
                        } else {
                            if ((event.getX() - startPoint.x) < 0 && Math.abs(event.getX() - startPoint.x) > mLimit / 10) {//否则就判断滑动距离
                                //平滑展开Menu
                                smoothExpand();
                            } else {
                                // 平滑关闭Menu
                                smoothClose();
                            }
                        }
                        if (null != velocityTracker) {
                            velocityTracker.clear();
                            velocityTracker.recycle();
                            velocityTracker = null;
                        }
                    }
                    isTouching = false;//没有手指在摸我了
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //禁止侧滑时，点击事件不受干扰。
        if (swipeEnable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Log.d("ACTION_MOVE1", ev.getX() + "," + ev.getY());
                    //屏蔽滑动时的事件
                    if (Math.abs(ev.getRawX() - startPoint.x) > mScaleSlop) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("ACTION_UP1", ev.getX() + "," + ev.getY());
                    if (getScrollX() > mScaleSlop) {
                        if (ev.getX() < getWidth() - getScrollX()) {
                            return true;//true表示拦截
                        }
                    }
                    // 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    if (isSwiped) {
                        return true;
                    }

                    break;
            }

            if (onlyExpandOne) {
                return true;
            }

        }
        return super.onInterceptTouchEvent(ev);
    }

    private void smoothExpand() {
        expandView = SwipeLayout.this;
        extendAnimator = ValueAnimator.ofInt(getScrollX(), rightWidth);
        extendAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int x = (int) animation.getAnimatedValue();
                scrollTo(x, 0);
            }
        });
        extendAnimator.setInterpolator(new AccelerateInterpolator());
        extendAnimator.setDuration(300).start();
    }

    private void smoothClose() {
        expandView = null;
        closeAnimator = ValueAnimator.ofInt(getScrollX(), 0);
        closeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int x = (int) animation.getAnimatedValue();
                scrollTo(x, 0);
            }
        });
        closeAnimator.setInterpolator(new AccelerateInterpolator());
        closeAnimator.setDuration(300).start();
    }

}
