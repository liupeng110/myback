/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andlp.back;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.util.Arrays;

public class ViewDragHelper {
    private static final String TAG = "ViewDragHelper";

    public static final int INVALID_POINTER = -1;//空/无效的指针ID。
    public static final int STATE_IDLE = 0;            //一个视图目前没有被拖动或动画作为一个结果/捕捉的结果
    public static final int STATE_DRAGGING = 1;//一个视图正在被拖动。 由于用户输入或模拟用户输入，该位置当前正在改变。
    public static final int STATE_SETTLING = 2;  //当前视图是由于一次性或预定义的非交互式运动而导致的
    public static final int EDGE_LEFT = 1 << 0;   //指示左边缘应受影响的边缘标志
    public static final int EDGE_RIGHT = 1 << 1;//右边
    public static final int EDGE_TOP = 1 << 2;   //顶部
    public static final int EDGE_BOTTOM = 1 << 3;  //底部
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_TOP | EDGE_RIGHT | EDGE_BOTTOM;//全部
    public static final int DIRECTION_HORIZONTAL = 1 << 0;//水平轴进行
    public static final int DIRECTION_VERTICAL = 1 << 1;//垂直轴
    public static final int DIRECTION_ALL = DIRECTION_HORIZONTAL | DIRECTION_VERTICAL;//所有轴检查
    public static final int EDGE_SIZE = 50;                             //边缘尺寸dp
    private static final int BASE_SETTLE_DURATION = 256; // 动画持续时间ms
    private static final int MAX_SETTLE_DURATION = 600; //
    private int mDragState;//当前拖动状态; 闲置，拖拉或沉降
    private int mTouchSlop;//开始拖动之前的距离
    private int mActivePointerId = INVALID_POINTER;//最后已知的位置/指针跟踪
    private float[] mInitialMotionX;//初始触摸x坐标
    private float[] mInitialMotionY;//初始触摸y坐标
    private float[] mLastMotionX; //最后的x坐标
    private float[] mLastMotionY;//最后的y坐标
    private int[] mInitialEdgeTouched;//初始边缘触摸
    private int[] mEdgeDragsInProgress;//边缘拖动进度
    private int[] mEdgeDragsLocked;     //边缘拖动锁
    private int mPointersDown;             //多点？
    private VelocityTracker mVelocityTracker;//帮助 跟踪触摸事件的速度
    private float mMaxVelocity;  //最大速度
    private float mMinVelocity;//最小速度
    private int mEdgeSize;//边缘大小
    private int mTrackingEdges;//跟踪边缘
    private ScrollerCompat mScroller;//当前设备的首选滚动物理和投掷行为
    private final Callback mCallback; // 回调 决定了范围 和子视图的可拖动性
    private View mCapturedView;     //捕获的视图
    private boolean mReleaseInProgress;//正在发布
    private final ViewGroup mParentView;//父级视图 viewgroup

    public static abstract class Callback {//回调决定范围 和子视图的可拖动性
        public void onViewDragStateChanged(int state) {  }//拖动状态改变时调用
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {}//positon位置改变时调用  left 左新坐标,dx从最后一次调用改变X位置
        public void onViewCaptured(View capturedChild, int activePointerId) {}//捕获子视图  pointerid为指针id
        public void onViewReleased(View releasedChild, float xvel, float yvel) {}//释放view
        public void onEdgeTouched(int edgeFlags, int pointerId) {}//边缘(左上右下)被触摸
        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }//边缘被锁定时调用
        public void onEdgeDragStarted(int edgeFlags, int pointerId) { }//开始拖动前
        public int getOrderedChildIndex(int index) {
            return index;
        }//确定子视图索引顺序
        public int getViewHorizontalDragRange(View child) {
            return 0;
        }//返回水平移动的像素,不动  返回0
        public int getViewVerticalDragRange(View child) {
            return 0;
        }     //返回垂直移动的像素,不动  返回0
        public abstract boolean tryCaptureView(View child, int pointerId);//试图捕获子视图  true可捕获,false为否
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }//限制子视图沿垂直轴的运动  将被拖动的子视图  left沿x轴尝试拖动
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }//限制子视图沿水平轴的运动  将被拖动的子视图  left沿y轴尝试拖动
    }



    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };//插值器定义mScroller的动画曲线


    private final Runnable mSetIdleRunnable = new Runnable() {
        public void run() {
            setDragState(STATE_IDLE);
        }
    };//设置子view状态

    public static ViewDragHelper create(ViewGroup forParent, Callback cb) {
        return new ViewDragHelper(forParent.getContext(), forParent, cb);
    }//工厂模式创建新的实例
    public static ViewDragHelper create(ViewGroup forParent, float sensitivity, Callback cb) {
        final ViewDragHelper helper = create(forParent, cb);
        helper.mTouchSlop = (int) (helper.mTouchSlop * (1 / sensitivity));
        return helper;
    }//工厂模式同上 sensitivity 灵敏度  默认1.0f正常

    //不能用构造函数创建
    private ViewDragHelper(Context context, ViewGroup forParent, Callback cb) {
        if (forParent == null) {
            throw new IllegalArgumentException("Parent view may not be null");
        }
        if (cb == null) {
            throw new IllegalArgumentException("Callback may not be null");
        }

        mParentView = forParent;
        mCallback = cb;

        final ViewConfiguration vc = ViewConfiguration.get(context);
        final float density = context.getResources().getDisplayMetrics().density;
        mEdgeSize = (int) (EDGE_SIZE * density + 0.5f);

        mTouchSlop = vc.getScaledTouchSlop();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mMinVelocity = vc.getScaledMinimumFlingVelocity();
        mScroller = ScrollerCompat.create(context, sInterpolator);
    }
    public void setSensitivity(Context context, float sensitivity) {
        float s = Math.max(0f, Math.min(1.0f, sensitivity));
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = (int) (viewConfiguration.getScaledTouchSlop() * (1 / s));
    }//设置灵敏度
    public void setMinVelocity(float minVel) {
        mMinVelocity = minVel;
    }   //设置最小速度
    public void setMaxVelocity(float maxVel) {
        mMaxVelocity = maxVel;
    }//设置最大速度
    public float getMinVelocity() {
        return mMinVelocity;
    }                           //获取最小速度
    public int getViewDragState() {
        return mDragState;
    }                              //获取view的拖动状态
    public void setEdgeTrackingEnabled(int edgeFlags) {
        mTrackingEdges = edgeFlags;
    } //设置是否可以跟踪边缘拖动
    public int getEdgeSize() {
        return mEdgeSize;
    }                                       //获取边缘大小
    public void setEdgeSize(int size) {
        mEdgeSize = size;
    }                         //设置边缘的大小
    public void captureChildView(View childView, int activePointerId) {
        if (childView.getParent() != mParentView) {
            throw new IllegalArgumentException("captureChildView: parameter must be a descendant "
                    + "of the ViewDragHelper's tracked parent view (" + mParentView + ")");
        }

        mCapturedView = childView;
        mActivePointerId = activePointerId;
        mCallback.onViewCaptured(childView, activePointerId);
        setDragState(STATE_DRAGGING);
    }//捕获子view
    public View getCapturedView() {
        return mCapturedView;
    }//当前捕获的视图，如果没有捕获视图，则返回null。
    public int getActivePointerId() {
        return mActivePointerId;
    }//当前拖动捕获视图的指针的ID
    public int getTouchSlop() {
        return mTouchSlop;
    }//用户必须移动以启动拖动的最小距离（以像素为单位）

    public void cancel() {
        mActivePointerId = INVALID_POINTER;
        clearMotionHistory();

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }//结果等于TouchEvent的ACTION_CANCEL事件
    public void abort() {
        cancel();
        if (mDragState == STATE_SETTLING) {
            final int oldX = mScroller.getCurrX();
            final int oldY = mScroller.getCurrY();
            mScroller.abortAnimation();
            final int newX = mScroller.getCurrX();
            final int newY = mScroller.getCurrY();
            mCallback.onViewPositionChanged(mCapturedView, newX, newY, newX - oldX, newY - oldY);
        }
        setDragState(STATE_IDLE);
    }// 中止正在进行的所有动作，并捕捉到任何动画的结尾
    public boolean smoothSlideViewTo(View child, int finalLeft, int finalTop) {
        mCapturedView = child;
        mActivePointerId = INVALID_POINTER;
        return forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0);
    }//返回false 停止   否则一直执行动画 到 左上
    public boolean settleCapturedViewAt(int finalLeft, int finalTop) {
        if (!mReleaseInProgress) {
            throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to " + "Callback#onViewReleased");
        }
        return forceSettleCapturedViewAt(finalLeft, finalTop,
                (int) VelocityTrackerCompat.getXVelocity(mVelocityTracker, mActivePointerId),
                (int) VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId));
    }//捕获左上视图
    private boolean forceSettleCapturedViewAt(int finalLeft, int finalTop, int xvel, int yvel) {
        final int startLeft = mCapturedView.getLeft();
        final int startTop = mCapturedView.getTop();
        final int dx = finalLeft - startLeft;
        final int dy = finalTop - startTop;

        if (dx == 0 && dy == 0) {
            // Nothing to do. Send callbacks, be done.
            mScroller.abortAnimation();
            setDragState(STATE_IDLE);
            return false;
        }

        final int duration = computeSettleDuration(mCapturedView, dx, dy, xvel, yvel);
        mScroller.startScroll(startLeft, startTop, dx, dy, duration);

        setDragState(STATE_SETTLING);
        return true;
    }//在给定的（左，上）位置解决捕获的视图  左上目标位置   xy速度
    private int computeSettleDuration(View child, int dx, int dy, int xvel, int yvel) {
        xvel = clampMag(xvel, (int) mMinVelocity, (int) mMaxVelocity);
        yvel = clampMag(yvel, (int) mMinVelocity, (int) mMaxVelocity);
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final int absXVel = Math.abs(xvel);
        final int absYVel = Math.abs(yvel);
        final int addedVel = absXVel + absYVel;
        final int addedDistance = absDx + absDy;

        final float xweight = xvel != 0 ? (float) absXVel / addedVel : (float) absDx
                / addedDistance;
        final float yweight = yvel != 0 ? (float) absYVel / addedVel : (float) absDy
                / addedDistance;

        int xduration = computeAxisDuration(dx, xvel, mCallback.getViewHorizontalDragRange(child));
        int yduration = computeAxisDuration(dy, yvel, mCallback.getViewVerticalDragRange(child));

        return (int) (xduration * xweight + yduration * yweight);
    }
    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        if (delta == 0) {
            return 0;
        }

        final int width = mParentView.getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, (float) Math.abs(delta) / width);
        final float distance = halfWidth + halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio);

        int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float range = (float) Math.abs(delta) / motionRange;
            duration = (int) ((range + 1) * BASE_SETTLE_DURATION);
        }
        return Math.min(duration, MAX_SETTLE_DURATION);
    }
    private int clampMag(int value, int absMin, int absMax) {
        final int absValue = Math.abs(value);
        if (absValue < absMin)
            return 0;
        if (absValue > absMax)
            return value > 0 ? absMax : -absMax;
        return value;
    }//矫正最小值和最大值
    private float clampMag(float value, float absMin, float absMax) {
        final float absValue = Math.abs(value);
        if (absValue < absMin)
            return 0;
        if (absValue > absMax)
            return value > 0 ? absMax : -absMax;
        return value;
    }  //同上,精度不同
    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }//距离在通量的持续时间
    public void flingCapturedView(int minLeft, int minTop, int maxLeft, int maxTop) {
        if (!mReleaseInProgress) {
            throw new IllegalStateException("Cannot flingCapturedView outside of a call to "
                    + "Callback#onViewReleased");
        }

        mScroller.fling(mCapturedView.getLeft(), mCapturedView.getTop(),
                (int) VelocityTrackerCompat.getXVelocity(mVelocityTracker, mActivePointerId),
                (int) VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId),
                minLeft, maxLeft, minTop, maxTop);

        setDragState(STATE_SETTLING);
    }//滑动捕获的视图  视图左边缘的最小X位置
    public boolean continueSettling(boolean deferCallbacks) {
        if (mDragState == STATE_SETTLING) {
            boolean keepGoing = mScroller.computeScrollOffset();
            final int x = mScroller.getCurrX();
            final int y = mScroller.getCurrY();
            final int dx = x - mCapturedView.getLeft();
            final int dy = y - mCapturedView.getTop();

            if (dx != 0) {
                mCapturedView.offsetLeftAndRight(dx);
            }
            if (dy != 0) {
                mCapturedView.offsetTopAndBottom(dy);
            }

            if (dx != 0 || dy != 0) {
                mCallback.onViewPositionChanged(mCapturedView, x, y, dx, dy);
            }

            if (keepGoing && x == mScroller.getFinalX() && y == mScroller.getFinalY()) {
                // Close enough. The interpolator/scroller might think we're
                // still moving
                // but the user sure doesn't.
                mScroller.abortAnimation();
                keepGoing = mScroller.isFinished();
            }

            if (!keepGoing) {
                if (deferCallbacks) {
                    mParentView.post(mSetIdleRunnable);
                } else {
                    setDragState(STATE_IDLE);
                }
            }
        }

        return mDragState == STATE_SETTLING;
    }//deferCallbacks  如果状态需要延期 返回true
    private void dispatchViewReleased(float xvel, float yvel) {
        mReleaseInProgress = true;
        mCallback.onViewReleased(mCapturedView, xvel, yvel);
        mReleaseInProgress = false;

        if (mDragState == STATE_DRAGGING) {
            // onViewReleased didn't call a method that would have changed this.
            // Go idle.
            setDragState(STATE_IDLE);
        }
    }//ui线程调用
    private void clearMotionHistory() {
        if (mInitialMotionX == null) {
            return;
        }
        Arrays.fill(mInitialMotionX, 0);
        Arrays.fill(mInitialMotionY, 0);
        Arrays.fill(mLastMotionX, 0);
        Arrays.fill(mLastMotionY, 0);
        Arrays.fill(mInitialEdgeTouched, 0);
        Arrays.fill(mEdgeDragsInProgress, 0);
        Arrays.fill(mEdgeDragsLocked, 0);
        mPointersDown = 0;
    }   //清空 所有移动的点的历史记录
    private void clearMotionHistory(int pointerId) {
        if (mInitialMotionX == null) {
            return;
        }
        mInitialMotionX[pointerId] = 0;
        mInitialMotionY[pointerId] = 0;
        mLastMotionX[pointerId] = 0;
        mLastMotionY[pointerId] = 0;
        mInitialEdgeTouched[pointerId] = 0;
        mEdgeDragsInProgress[pointerId] = 0;
        mEdgeDragsLocked[pointerId] = 0;
        mPointersDown &= ~(1 << pointerId);
    }//清空 指定移动的点的历史记录
    private void ensureMotionHistorySizeForId(int pointerId) {
        if (mInitialMotionX == null || mInitialMotionX.length <= pointerId) {
            float[] imx = new float[pointerId + 1];
            float[] imy = new float[pointerId + 1];
            float[] lmx = new float[pointerId + 1];
            float[] lmy = new float[pointerId + 1];
            int[] iit = new int[pointerId + 1];
            int[] edip = new int[pointerId + 1];
            int[] edl = new int[pointerId + 1];

            if (mInitialMotionX != null) {
                System.arraycopy(mInitialMotionX, 0, imx, 0, mInitialMotionX.length);
                System.arraycopy(mInitialMotionY, 0, imy, 0, mInitialMotionY.length);
                System.arraycopy(mLastMotionX, 0, lmx, 0, mLastMotionX.length);
                System.arraycopy(mLastMotionY, 0, lmy, 0, mLastMotionY.length);
                System.arraycopy(mInitialEdgeTouched, 0, iit, 0, mInitialEdgeTouched.length);
                System.arraycopy(mEdgeDragsInProgress, 0, edip, 0, mEdgeDragsInProgress.length);
                System.arraycopy(mEdgeDragsLocked, 0, edl, 0, mEdgeDragsLocked.length);
            }

            mInitialMotionX = imx;
            mInitialMotionY = imy;
            mLastMotionX = lmx;
            mLastMotionY = lmy;
            mInitialEdgeTouched = iit;
            mEdgeDragsInProgress = edip;
            mEdgeDragsLocked = edl;
        }
    }//根据id测量 历史纪录大小

    private void saveInitialMotion(float x, float y, int pointerId) {
        ensureMotionHistorySizeForId(pointerId);
        mInitialMotionX[pointerId] = mLastMotionX[pointerId] = x;
        mInitialMotionY[pointerId] = mLastMotionY[pointerId] = y;
        mInitialEdgeTouched[pointerId] = getEdgeTouched((int) x, (int) y);
        mPointersDown |= 1 << pointerId;
    }//保存初始的移动事件的点坐标

    private void saveLastMotion(MotionEvent ev) {
        final int pointerCount = MotionEventCompat.getPointerCount(ev);
        for (int i = 0; i < pointerCount; i++) {
            final int pointerId = MotionEventCompat.getPointerId(ev, i);
            final float x = MotionEventCompat.getX(ev, i);
            final float y = MotionEventCompat.getY(ev, i);
            mLastMotionX[pointerId] = x;
            mLastMotionY[pointerId] = y;
        }
    }                     //保存最后的移动事件的点坐标
    public boolean isPointerDown(int pointerId) {
        return (mPointersDown & 1 << pointerId) != 0;
    }                       //检查给定的指针ID是否代表当前正在关闭的指针
    void setDragState(int state) {
        if (mDragState != state) {
            mDragState = state;
            mCallback.onViewDragStateChanged(state);
            if (state == STATE_IDLE) {
                mCapturedView = null;
            }
        }
    }                                                   //设置拖动状态
    boolean tryCaptureViewForDrag(View toCapture, int pointerId) {
        if (toCapture == mCapturedView && mActivePointerId == pointerId) {
            // Already done!
            return true;
        }
        if (toCapture != null && mCallback.tryCaptureView(toCapture, pointerId)) {
            mActivePointerId = pointerId;
            captureChildView(toCapture, pointerId);
            return true;
        }
        return false;
    }//尝试使用给定的指针ID捕获视图
    protected boolean canScroll(View v, boolean checkV, int dx, int dy, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance
            // first.
            for (int i = count - 1; i >= 0; i--) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft()
                        && x + scrollX < child.getRight()
                        && y + scrollY >= child.getTop()
                        && y + scrollY < child.getBottom()
                        && canScroll(child, true, dx, dy, x + scrollX - child.getLeft(), y
                        + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV
                && (ViewCompat.canScrollHorizontally(v, -dx) || ViewCompat.canScrollVertically(v,
                -dy));
    }//在给定dx的三角形的v子视图内测试可滚动性    view 查看测试水平可滚动性 ，checkV true可滚动，dx 增量沿着X轴滚动像素  ，x活动触点的X坐标

    //如果父视图应该从onInterceptTouchEvent返回true，则返回true
    public boolean shouldInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        final int actionIndex = MotionEventCompat.getActionIndex(ev);

        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel();
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                final int pointerId = MotionEventCompat.getPointerId(ev, 0);
                saveInitialMotion(x, y, pointerId);

                final View toCapture = findTopChildUnder((int) x, (int) y);

                // Catch a settling view if possible.
                if (toCapture == mCapturedView && mDragState == STATE_SETTLING) {
                    tryCaptureViewForDrag(toCapture, pointerId);
                }

                final int edgesTouched = mInitialEdgeTouched[pointerId];
                if ((edgesTouched & mTrackingEdges) != 0) {
                    mCallback.onEdgeTouched(edgesTouched & mTrackingEdges, pointerId);
                }
                break;
            }

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int pointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                final float x = MotionEventCompat.getX(ev, actionIndex);
                final float y = MotionEventCompat.getY(ev, actionIndex);

                saveInitialMotion(x, y, pointerId);

                // A ViewDragHelper can only manipulate one view at a time.
                if (mDragState == STATE_IDLE) {
                    final int edgesTouched = mInitialEdgeTouched[pointerId];
                    if ((edgesTouched & mTrackingEdges) != 0) {
                        mCallback.onEdgeTouched(edgesTouched & mTrackingEdges, pointerId);
                    }
                } else if (mDragState == STATE_SETTLING) {
                    // Catch a settling view if possible.
                    final View toCapture = findTopChildUnder((int) x, (int) y);
                    if (toCapture == mCapturedView) {
                        tryCaptureViewForDrag(toCapture, pointerId);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // First to cross a touch slop over a draggable view wins. Also
                // report edge drags.
                final int pointerCount = MotionEventCompat.getPointerCount(ev);
                for (int i = 0; i < pointerCount; i++) {
                    final int pointerId = MotionEventCompat.getPointerId(ev, i);
                    final float x = MotionEventCompat.getX(ev, i);
                    final float y = MotionEventCompat.getY(ev, i);
                    final float dx = x - mInitialMotionX[pointerId];
                    final float dy = y - mInitialMotionY[pointerId];

                    reportNewEdgeDrags(dx, dy, pointerId);
                    if (mDragState == STATE_DRAGGING) {
                        // Callback might have started an edge drag
                        break;
                    }

                    final View toCapture = findTopChildUnder((int) x, (int) y);
                    if (toCapture != null && checkTouchSlop(toCapture, dx, dy)
                            && tryCaptureViewForDrag(toCapture, pointerId)) {
                        break;
                    }
                }
                saveLastMotion(ev);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP: {
                final int pointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                clearMotionHistory(pointerId);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                cancel();
                break;
            }
        }

        return mDragState == STATE_DRAGGING;
    }


    //处理父视图接收的触摸事件。 这个方法将在返回之前根据需要调度回调事件。
    // 父视图的onTouchEvent实现应该调用它
    public void processTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        final int actionIndex = MotionEventCompat.getActionIndex(ev);

        if (action == MotionEvent.ACTION_DOWN) {//重置一个新的事件流的东西，以防万一我们没有得到整个上一个流
            cancel();
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                final int pointerId = MotionEventCompat.getPointerId(ev, 0);
                final View toCapture = findTopChildUnder((int) x, (int) y);
                saveInitialMotion(x, y, pointerId);//保存第一次按下的坐标值
                tryCaptureViewForDrag(toCapture, pointerId);  //由于父类已经处理一次 所以此处不多次判断 直接 处理

                final int edgesTouched = mInitialEdgeTouched[pointerId];//判断上下左右
                if ((edgesTouched & mTrackingEdges) != 0) {
                    mCallback.onEdgeTouched(edgesTouched & mTrackingEdges, pointerId);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mDragState == STATE_DRAGGING) {
                    final int index = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, index);
                    final float y = MotionEventCompat.getY(ev, index);
                    final int idx = (int) (x - mLastMotionX[mActivePointerId]);
                    final int idy = (int) (y - mLastMotionY[mActivePointerId]);

                    dragTo(mCapturedView.getLeft() + idx, mCapturedView.getTop() + idy, idx, idy);

                    saveLastMotion(ev);
                } else {
                    // Check to see if any pointer is now over a draggable view.
                    final int pointerCount = MotionEventCompat.getPointerCount(ev);
                    for (int i = 0; i < pointerCount; i++) {
                        final int pointerId = MotionEventCompat.getPointerId(ev, i);
                        final float x = MotionEventCompat.getX(ev, i);
                        final float y = MotionEventCompat.getY(ev, i);
                        final float dx = x - mInitialMotionX[pointerId];
                        final float dy = y - mInitialMotionY[pointerId];

                        reportNewEdgeDrags(dx, dy, pointerId);
                        if (mDragState == STATE_DRAGGING) {
                            // Callback might have started an edge drag.
                            break;
                        }

                        final View toCapture = findTopChildUnder((int) x, (int) y);
                        if (checkTouchSlop(toCapture, dx, dy)
                                && tryCaptureViewForDrag(toCapture, pointerId)) {
                            break;
                        }
                    }
                    saveLastMotion(ev);
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mDragState == STATE_DRAGGING) {
                    releaseViewForPointerUp();
                }
                cancel();
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mDragState == STATE_DRAGGING) {
                    dispatchViewReleased(0, 0);
                }
                cancel();
                break;
            }
        }
    }

    private void reportNewEdgeDrags(float dx, float dy, int pointerId) {
        int dragsStarted = 0;
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_LEFT)) {
            dragsStarted |= EDGE_LEFT;
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_TOP)) {
            dragsStarted |= EDGE_TOP;
        }
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_RIGHT)) {
            dragsStarted |= EDGE_RIGHT;
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_BOTTOM)) {
            dragsStarted |= EDGE_BOTTOM;
        }

        if (dragsStarted != 0) {
            mEdgeDragsInProgress[pointerId] |= dragsStarted;
            mCallback.onEdgeDragStarted(dragsStarted, pointerId);
        }
    }//报告新的边缘拖动


    private boolean checkNewEdgeDrag(float delta, float odelta, int pointerId, int edge) {
        final float absDelta = Math.abs(delta);
        final float absODelta = Math.abs(odelta);

        if ((mInitialEdgeTouched[pointerId] & edge) != edge || (mTrackingEdges & edge) == 0
                || (mEdgeDragsLocked[pointerId] & edge) == edge
                || (mEdgeDragsInProgress[pointerId] & edge) == edge
                || (absDelta <= mTouchSlop && absODelta <= mTouchSlop)) {
            return false;
        }
        if (absDelta < absODelta * 0.5f && mCallback.onEdgeLock(edge)) {
            mEdgeDragsLocked[pointerId] |= edge;
            return false;
        }
        return (mEdgeDragsInProgress[pointerId] & edge) == 0 && absDelta > mTouchSlop;
    }//检查新的边缘拖动

    private boolean checkTouchSlop(View child, float dx, float dy) {
        if (child == null) {
            return false;
        }
        final boolean checkHorizontal = mCallback.getViewHorizontalDragRange(child) > 0;
        final boolean checkVertical = mCallback.getViewVerticalDragRange(child) > 0;

        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > mTouchSlop * mTouchSlop;
        } else if (checkHorizontal) {
            return Math.abs(dx) > mTouchSlop;
        } else if (checkVertical) {
            return Math.abs(dy) > mTouchSlop;
        }
        return false;
    }//检查我们是否为给定的子视图穿过合理的触摸斜坡   dx 从沿着X轴的初始位置开始的运动
    // 如果孩子不能沿水平轴或垂直轴拖动，那么沿着该轴的运动将不会被计入倾斜检查。



    public boolean checkTouchSlop(int directions) {//方向
        final int count = mInitialMotionX.length;
        for (int i = 0; i < count; i++) {
            if (checkTouchSlop(directions, i)) {
                return true;
            }
        }
        return false;
    }  //检查当前手势跟踪的指针是否超过了所需的坡度阈值
    public boolean checkTouchSlop(int directions, int pointerId) {
        if (!isPointerDown(pointerId)) {
            return false;
        }

        final boolean checkHorizontal = (directions & DIRECTION_HORIZONTAL) == DIRECTION_HORIZONTAL;
        final boolean checkVertical = (directions & DIRECTION_VERTICAL) == DIRECTION_VERTICAL;

        final float dx = mLastMotionX[pointerId] - mInitialMotionX[pointerId];
        final float dy = mLastMotionY[pointerId] - mInitialMotionY[pointerId];

        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > mTouchSlop * mTouchSlop;
        } else if (checkHorizontal) {
            return Math.abs(dx) > mTouchSlop;
        } else if (checkVertical) {
            return Math.abs(dy) > mTouchSlop;
        }
        return false;
    }//检查当前手势中指定的指针是否超过了所需的坡度阈值。

    public boolean isEdgeTouched(int edges) {
        final int count = mInitialEdgeTouched.length;
        for (int i = 0; i < count; i++) {
            if (isEdgeTouched(edges, i)) {
                return true;
            }
        }
        return false;
    }//检查是否有任何指定的边缘在当前活动手势中最初被触摸
    // 如果当前没有活动手势，则此方法将返回false。


    public boolean isEdgeTouched(int edges, int pointerId) {
        return isPointerDown(pointerId) && (mInitialEdgeTouched[pointerId] & edges) != 0;
    }
    private void releaseViewForPointerUp() {
        mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
        final float xvel = clampMag(
                VelocityTrackerCompat.getXVelocity(mVelocityTracker, mActivePointerId),
                mMinVelocity, mMaxVelocity);
        final float yvel = clampMag(
                VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId),
                mMinVelocity, mMaxVelocity);
        dispatchViewReleased(xvel, yvel);
    }//释放视图的指针

    //拖动
    private void dragTo(int left, int top, int dx, int dy) {
        int clampedX = left;
        int clampedY = top;
        final int oldLeft = mCapturedView.getLeft();
        final int oldTop = mCapturedView.getTop();
        if (dx != 0) {
            clampedX = mCallback.clampViewPositionHorizontal(mCapturedView, left, dx);
            mCapturedView.offsetLeftAndRight(clampedX - oldLeft);
        }
        if (dy != 0) {
            clampedY = mCallback.clampViewPositionVertical(mCapturedView, top, dy);
            mCapturedView.offsetTopAndBottom(clampedY - oldTop);
        }

        if (dx != 0 || dy != 0) {
            final int clampedDx = clampedX - oldLeft;
            final int clampedDy = clampedY - oldTop;
            mCallback
                    .onViewPositionChanged(mCapturedView, clampedX, clampedY, clampedDx, clampedDy);
        }
    }

    //确定当前捕获的视图是否在父视图的坐标系中给定的点之下。 如果没有捕获的视图，这个方法将返回false。
    public boolean isCapturedViewUnder(int x, int y) {
        return isViewUnder(mCapturedView, x, y);
    }//X位置在父母的坐标系中测试

    //确定提供的视图是否在父视图的坐标系中给定的点之下。
    public boolean isViewUnder(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        return x >= view.getLeft() && x < view.getRight() && y >= view.getTop()
                && y < view.getBottom();
    }

// 在父视图的坐标系中查找给定点下的最顶层的子项。 子订单是使用确定的
    public View findTopChildUnder(int x, int y) {
        final int childCount = mParentView.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = mParentView.getChildAt(mCallback.getOrderedChildIndex(i));
            if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop()
                    && y < child.getBottom()) {
                return child;
            }
        }
        return null;
    }

    //判断触摸的哪部分  左上右下
    private int getEdgeTouched(int x, int y) {
        int result = 0;
        if (x < mParentView.getLeft() + mEdgeSize)
            result = EDGE_LEFT;
        if (y < mParentView.getTop() + mEdgeSize)
            result = EDGE_TOP;
        if (x > mParentView.getRight() - mEdgeSize)
            result = EDGE_RIGHT;
        if (y > mParentView.getBottom() - mEdgeSize)
            result = EDGE_BOTTOM;
        return result;
    }
}
