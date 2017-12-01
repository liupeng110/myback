package com.andlp.back;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class SwipeBackLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400; // 最小的速度 作为一个投掷检测  像素/秒
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;//纱幕 颜色？
    private static final int FULL_ALPHA = 255;

    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;     //左边
    public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT;
    public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM;
    public static final int EDGE_TOP =ViewDragHelper.EDGE_TOP;
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_RIGHT | EDGE_BOTTOM;
    private static final int[] EDGE_FLAGS = { EDGE_LEFT, EDGE_RIGHT, EDGE_BOTTOM, EDGE_ALL,EDGE_TOP  };
    private int mEdgeFlag;

    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;//一个视图目前没有被拖动或动画作为一个结果/捕捉的结果。
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;//被拖动状态
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;//当前视图是由于一次性或预定义的非交互式运动而导致的。
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.65f;//滚动的默认阈值
    private static final int OVERSCROLL_DISTANCE = 10;//反弹时距离

    private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD;//滚动的阈值，我们将关闭活动，当scrollPercent超过这个值;
    private Activity mActivity;
    private View mContentView;
    private Fragment mFragment=null;
    private ViewDragHelper mDragHelper;
    private float mScrollPercent;
    private int mContentLeft;
    private int mContentTop;
    private List<SwipeListener> mListeners;//要通过发送事件的听众的集合。


    private Drawable mShadowLeft;              //各个阴影
    private Drawable mShadowRight;
    private Drawable mShadowBottom;
    private Drawable mShadowTop;

    private boolean mEnable = true;
    private int mTrackingEdge;//边缘被拖动
    private float mScrimOpacity;//不透明度
    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private boolean mInLayout;
    private Rect mTmpRect = new Rect();


    public SwipeBackLayout(Context context) {
        this(context, null);
    }
    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SwipeBackLayoutStyle);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mDragHelper = ViewDragHelper.create(this, new ViewDragCallback());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeBackLayout, defStyle,R.style.SwipeBackLayout);

        int edgeSize = typedArray.getDimensionPixelSize(R.styleable.SwipeBackLayout_edge_size, -1);
        if (edgeSize > 0) setEdgeSize(edgeSize);
        int mode = EDGE_FLAGS[typedArray.getInt(R.styleable.SwipeBackLayout_edge_flag, 0)];
        setEdgeTrackingEnabled(mode);

        int shadowLeft = typedArray.getResourceId(R.styleable.SwipeBackLayout_shadow_left,R.drawable.shadow_left);
        int shadowRight = typedArray.getResourceId(R.styleable.SwipeBackLayout_shadow_right, R.drawable.shadow_right);
        int shadowBottom = typedArray.getResourceId(R.styleable.SwipeBackLayout_shadow_bottom,R.drawable.shadow_bottom);
        setShadow(shadowLeft, EDGE_LEFT);
        setShadow(shadowRight, EDGE_RIGHT);
        setShadow(shadowBottom, EDGE_BOTTOM);
        typedArray.recycle();
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mDragHelper.setMinVelocity(minVel);
        mDragHelper.setMaxVelocity(minVel * 2f);
    }

    public void setmContentView(View view){
        mContentView = view;
    }
    public void setmFragment(Fragment fragment){
        mFragment =fragment;
    }

    public void setSensitivity(Context context, float sensitivity) {
        mDragHelper.setSensitivity(context, sensitivity);
    }//设置边缘拖动灵敏度  0-1之间
    private void setContentView(View view) {
        mContentView = view;
    }//设置将通过用户手势移动的contentView
    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }//设置是否可以拖动
    public void setEdgeTrackingEnabled(int edgeFlags) {
        mEdgeFlag = edgeFlags;
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag);
    }//启用父视图的选定边的边缘跟踪  参数 左上右下
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }//设置用于在抽屉打开时遮挡主要内容的稀松布的颜色。  颜色以0xA
    // RRGGBB格式使用。
    public void setEdgeSize(int size) {
        mDragHelper.setEdgeSize(size);
    }//设置边缘大小
    @Deprecated public void setSwipeListener(SwipeListener listener) {
        addSwipeListener(listener);
    }//将滑动事件发送到此视图时，注册要调用的回调。
    public void addSwipeListener(SwipeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<SwipeListener>();
        }
        mListeners.add(listener);
    }//在向此视图发送滑动事件时添加要调用的回调。
    public void removeSwipeListener(SwipeListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }//从一组侦听器中移除一个侦听器

    public interface SwipeListener {
          void onScrollStateChange(int state, float scrollPercent);//当状态改变时调用，state 标志来描述滚动状态 ，scrollper滚动这个视图的百分比
          void onEdgeTouch(int edgeFlag);//触摸边缘时调用  左上右下
          void onScrollOverThreshold();//当第一次滚动百分比超过阈值时调用
    }

    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollThreshold = threshold;
    }//设置滚动阈值，当滚动百分比超过此值时 将关闭
    public void setShadow(Drawable shadow, int edgeFlag) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        } else if ((edgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight = shadow;
        } else if ((edgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom = shadow;
        }else if ((edgeFlag & EDGE_TOP) != 0) {
            mShadowTop = shadow;
        }

        invalidate();
    }//设置用于边缘阴影的drawable。
    public void setShadow(int resId, int edgeFlag) {
        setShadow(getResources().getDrawable(resId), edgeFlag);
    }
    public void scrollToFinishActivity() {
        final int childWidth = mContentView.getWidth();
        final int childHeight = mContentView.getHeight();

        int left = 0, top = 0;
        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            left = childWidth + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_LEFT;
        } else if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            left = -childWidth - mShadowRight.getIntrinsicWidth() - OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_RIGHT;
        } else if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            top = -childHeight - mShadowBottom.getIntrinsicHeight() - OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_BOTTOM;
        }else if ((mEdgeFlag & EDGE_TOP) != 0) {
            top = -childHeight - mShadowTop.getIntrinsicHeight() - OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_TOP;
        }

        mDragHelper.smoothSlideViewTo(mContentView, left, top);
        invalidate();
    }//滚动contentView并完成活动



    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
//        Log.i("点击","进入touch事件x："+event.getRawX()+",--y："+event.getRawY()+"--"+mActivity.getClass().getSimpleName()+"--"+mFragment);
        if (!mEnable) { return false; }//如果不允许拖动直接交给上层处理
        Log.i("2点击","进入touch事件x："+event.getRawX()+",--y："+event.getRawY()+"--"+mActivity+"--"+mFragment);

        try {
            return mDragHelper.shouldInterceptTouchEvent(event);//判断是否需要进行处理
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    } //viewgroup层拦截 事件
    @Override public boolean onTouchEvent(MotionEvent event) {
        Log.i("3点击","进入touch事件x："+event.getRawX()+",--y："+event.getRawY()+"--"+mActivity+"--"+mFragment);

        if (!mEnable) { return false; }
        Log.i("4点击","进入touch事件x："+event.getRawX()+",--y："+event.getRawY()+"--"+mActivity+"--"+mFragment);

        mDragHelper.processTouchEvent(event);//分发给具体处理者
        return true;
    } //如果onInterceptTouchEvent为true则进入这里进行处理
    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        if (mContentView != null)
            mContentView.layout(mContentLeft, mContentTop,
                    mContentLeft + mContentView.getMeasuredWidth(),
                    mContentTop + mContentView.getMeasuredHeight());
        mInLayout = false;
    }//布局，参数 左上右下
    @Override public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    } //请求framework重新测量 布局 绘制
    @Override protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView;
        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (mScrimOpacity > 0 && drawContent&& mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
//            drawShadow(canvas, child);//注释去阴影
//            drawScrim(canvas, child);   //纱罩也去掉
        }
        return ret;
    }      //绘制子view

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);

        if ((mTrackingEdge & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
            canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
        } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
            canvas.clipRect(child.getLeft(), child.getBottom(), getRight(), getHeight());
        }
        canvas.drawColor(color);
    }      //绘制纱罩
    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);//获取View可点击矩形左、上、右、下边界相对于父View的左顶点的距离（偏移量）

        if ((mEdgeFlag & EDGE_LEFT) != 0) {//阴影坐标的计算  左上右下
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight.setBounds(childRect.right, childRect.top, childRect.right + mShadowRight.getIntrinsicWidth(), childRect.bottom);
            mShadowRight.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowRight.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom.setBounds(childRect.left, childRect.bottom, childRect.right,childRect.bottom + mShadowBottom.getIntrinsicHeight());
            mShadowBottom.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowBottom.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_TOP) != 0) {
            mShadowTop.setBounds(childRect.left, childRect.bottom, childRect.right, childRect.top + mShadowTop.getIntrinsicHeight());
            mShadowTop.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowTop.draw(canvas);
        }
    }  //绘制阴影

     //包裹activity最外层
    public void attachToActivity(Activity activity) {
        mActivity = activity;
        TypedArray typedArray = activity.getTheme().obtainStyledAttributes(new int[]{ android.R.attr.windowBackground  });
        int background = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();//装饰
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        addView(decorChild);
        setContentView(decorChild);
        decor.addView(this);
    }//附加到activity

     //http://www.linuxidc.com/Linux/2016-01/127276.htm
    @Override public void computeScroll() {
        mScrimOpacity = 1 - mScrollPercent;
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    } //计算滑动

    private class ViewDragCallback extends ViewDragHelper.Callback {
         private boolean mIsScrollOverValid;//滑动覆盖有效
        //滑动之前 最先进入这里  滑不滑动都进入
        @Override public boolean tryCaptureView(View view, int i) {
            boolean ret = mDragHelper.isEdgeTouched(mEdgeFlag, i);//是否触摸到了边缘
            if (ret) {
                if (mDragHelper.isEdgeTouched(EDGE_LEFT, i)) {
                    mTrackingEdge = EDGE_LEFT;
                } else if (mDragHelper.isEdgeTouched(EDGE_RIGHT, i)) {
                    mTrackingEdge = EDGE_RIGHT;
                } else if (mDragHelper.isEdgeTouched(EDGE_BOTTOM, i)) {
                    mTrackingEdge = EDGE_BOTTOM;
                }
                if (mListeners != null && !mListeners.isEmpty()) {
                    for (SwipeListener listener : mListeners) {
                        listener.onEdgeTouch(mTrackingEdge);
                    }
                }
                mIsScrollOverValid = true;
            }
            boolean directionCheck = false;
            if (mEdgeFlag == EDGE_LEFT || mEdgeFlag == EDGE_RIGHT) {
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_VERTICAL, i);
            } else if (mEdgeFlag == EDGE_BOTTOM) {
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_HORIZONTAL, i);
            } else if (mEdgeFlag == EDGE_ALL) {
                directionCheck = true;
            }else if (mEdgeFlag == EDGE_TOP){
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_HORIZONTAL, i);
            }
            return ret & directionCheck;
        }

        @Override  public int getViewHorizontalDragRange(View child) {
            return mEdgeFlag & (EDGE_LEFT | EDGE_RIGHT);
        }

        @Override public int getViewVerticalDragRange(View child) {
            return mEdgeFlag & EDGE_BOTTOM;
        }

        @Override public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                mScrollPercent = Math.abs((float) left/ (mContentView.getWidth() + mShadowLeft.getIntrinsicWidth()));
            } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
                mScrollPercent = Math.abs((float) left/ (mContentView.getWidth() + mShadowRight.getIntrinsicWidth()));
            } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                mScrollPercent = Math.abs((float) top/ (mContentView.getHeight() + mShadowBottom.getIntrinsicHeight()));
            } else if ((mTrackingEdge & EDGE_TOP) != 0) {
                mScrollPercent = Math.abs((float) top/ (mContentView.getHeight() + mShadowTop.getIntrinsicHeight()));
            }

            mContentLeft = left;
            mContentTop = top;
            invalidate();
            if (mScrollPercent < mScrollThreshold && !mIsScrollOverValid) {
                mIsScrollOverValid = true;
            }
            if (mListeners != null && !mListeners.isEmpty()
                    && mDragHelper.getViewDragState() == STATE_DRAGGING
                    && mScrollPercent >= mScrollThreshold && mIsScrollOverValid) {
                mIsScrollOverValid = false;
                for (SwipeListener listener : mListeners) {
                    listener.onScrollOverThreshold();
                }
            }

            if (mScrollPercent >= 1) {
                if (!mActivity.isFinishing()) {
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);        
                }
            }
        }

        @Override  public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();
            final int childHeight = releasedChild.getHeight();

            int left = 0, top = 0;
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                left = xvel > 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? childWidth
                        + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE : 0;
            } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
                left = xvel < 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? -(childWidth
                        + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE) : 0;
            } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                top = yvel < 0 || yvel == 0 && mScrollPercent > mScrollThreshold ? -(childHeight
                        + mShadowBottom.getIntrinsicHeight() + OVERSCROLL_DISTANCE) : 0;
            }else if ((mTrackingEdge & EDGE_TOP) != 0) {
                top = yvel < 0 || yvel == 0 && mScrollPercent > mScrollThreshold ? -(childHeight
                        + mShadowTop.getIntrinsicHeight() + OVERSCROLL_DISTANCE) : 0;
            }

            mDragHelper.settleCapturedViewAt(left, top);
            invalidate();
        }

        @Override public int clampViewPositionHorizontal(View child, int left, int dx) {
            int ret = 0;
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
                ret = Math.min(0, Math.max(left, -child.getWidth()));
            }
            return ret;
        }

        @Override public int clampViewPositionVertical(View child, int top, int dy) {
            int ret = 0;
            if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                ret = Math.min(0, Math.max(top, -child.getHeight()));
            }else  if ((mTrackingEdge & EDGE_TOP) != 0) {
                ret = Math.min(0, Math.max(top, 0));
            }
            return ret;
        }

        @Override public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (mListeners != null && !mListeners.isEmpty()) {
                for (SwipeListener listener : mListeners) {
                    listener.onScrollStateChange(state, mScrollPercent);
                }//for
            }//if
        }//函数结束
    }//callback结束

}
