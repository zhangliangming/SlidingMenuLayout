package com.zlm.libs.widget;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zlm.libs.register.RegisterHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: SlidingMenu布局，该界面的view是一层一层的，所以这里使用FrameLayout布局
 * @author: zhangliangming
 * @date: 2018-05-26 15:07
 **/
public class SlidingMenuLayout extends FrameLayout {

    /**
     * 左到右
     */
    public static final int LEFT_TO_RIGHT = 0;
    /**
     * 右到左
     */
    public static final int RIGHT_TO_LEFT = 1;
    /**
     * 不允许滑动
     */
    public static final int NONE = -1;
    /**
     * 左边的布局
     */
    private LinearLayout mMenuLinearLayout;
    /**
     * 记录控件的位置
     */
    private int mMenuLeftX = 0, mMainLeftX = 0, mFrameLayoutLeftX = 0;
    /**
     * 主界面布局
     */
    private LinearLayout mMainLinearLayout;
    /**
     * 用于左边菜单打开时，遮住mainview
     */
    private View mMainMaskView;
    /**
     * 右边的布局
     */
    private FrameLayout mFrameLayout;
    /**
     * Fragment 列表
     */
    private ArrayList<FragmentFrameLayout> mFragmentFrameLayouts = new ArrayList<>();
    /**
     *
     */
    private ArrayList<FrameLayout> mFrameLayouts = new ArrayList<>();
    /**
     * 屏幕宽度
     */
    private int mScreensWidth;
    private Context mContext;
    /**
     * 动画时间
     */
    private int mDuration = 250;
    /**
     * 最小缩放比例
     */
    private float mMinScaleY = 0.8f;
    /**
     * 最大缩放比例
     */
    private float mMaxScaleY = 1.0f;
    /**
     * menu的缩放比例
     */
    private float mMenuScaleY = mMinScaleY;
    /**
     * main的缩放比例
     */
    private float mMainScaleY = mMaxScaleY;
    /**
     * xy轴移动动画
     */
    private ValueAnimator mValueAnimator;
    /**
     * 是否允许菜单移动
     */
    private boolean isAllowMenuTranslation = true;
    /**
     * 最小的透明度
     */
    private int mMinAlpha = 200;
    /**
     * 是否绘画主界面阴影
     */
    private boolean isMainMaskPaintFade = false;
    /**
     * 拦截的X轴和Y最后的位置
     */
    private float mLastInterceptX = 0, mLastInterceptY = 0, mLastX = 0;
    /**
     * 是否是触摸移动
     */
    private boolean isTouchMove = false;
    /**
     * 拖动类型
     */
    private int mDragType = LEFT_TO_RIGHT;
    /**
     * 记录上一次的拖动类型
     */
    private int mOldDragType = mDragType;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;
    /**
     * 判断Fragment是否正在打开
     */
    private boolean isFragmentOpening = false;
    /**
     *
     */
    private FragmentFrameLayout mFragmentFrameLayout;

    /**
     * 是否绘画Fragment阴影
     */
    private boolean isFragmentPaintFade = false;

    /**
     * 阴影画笔
     */
    private Paint mFragmentFadePaint;

    /**
     * 不拦截水平视图
     */
    private List<View> mIgnoreHorizontalViews;

    private OnPageChangeListener mOnPageChangeListener;

    /**
     * 是否是打开frag
     */
    private boolean mIsAddOpenFragment = false;

    public SlidingMenuLayout(Context context) {
        super(context);
        init(context);
    }

    public SlidingMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @throws
     * @Description: 初始化
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-05-26 16:24
     */
    private void init(Context context) {

        RegisterHelper.verify();

        this.mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mFragmentFadePaint = new Paint();
        mFragmentFadePaint.setAntiAlias(true);
        mFragmentFadePaint.setColor(Color.argb(255, 0, 0, 0));
    }

    /**
     * 初始化布局
     *
     * @param menuLayoutParams
     * @param menuLinearLayout
     * @param mainLayoutParams
     * @param mainLinearLayout
     */
    public void onAttachView(FrameLayout.LayoutParams menuLayoutParams, LinearLayout menuLinearLayout, FrameLayout.LayoutParams mainLayoutParams, LinearLayout mainLinearLayout) {

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mScreensWidth = displayMetrics.widthPixels;

        if (isAllowMenuTranslation) {
            menuLayoutParams.leftMargin = mScreensWidth - menuLayoutParams.width;
            menuLinearLayout.setScaleY(mMenuScaleY);
        } else {
            mMenuScaleY = mMaxScaleY;
        }

        //添加菜单布局
        addView(menuLinearLayout, menuLayoutParams);

        //添加主菜单布局
        addView(mainLinearLayout, mainLayoutParams);

        //添加主菜单遮罩
        mMainMaskView = new View(mContext);
        mMainMaskView.setTag(0);
        mMainMaskView.setBackgroundColor(Color.TRANSPARENT);
        mMainMaskView.setVisibility(View.GONE);
        mMainMaskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
            }
        });
        addView(mMainMaskView, mainLayoutParams);

        this.mMenuLinearLayout = menuLinearLayout;
        this.mMainLinearLayout = mainLinearLayout;

        //预加载下一个fragment所在的布局
        loadNextPage();
    }

    /**
     * 加载下一个页面
     */
    private void loadNextPage() {

        //添加fragment布局
        mFrameLayout = new FrameLayout(mContext);
        mFrameLayout.setBackgroundColor(Color.WHITE);
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mFrameLayoutLeftX = mScreensWidth;
        addView(mFrameLayout, frameLayoutParams);

        //
        mFragmentFrameLayout = new FragmentFrameLayout(mContext);
        mFrameLayout.addView(mFragmentFrameLayout, frameLayoutParams);
    }

    /**
     * 重新加载
     */
    private void reloadNextPage() {
        removeView(mFrameLayout);
        loadNextPage();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mMainLinearLayout != null) {
            mMainLinearLayout.layout(mMainLeftX, 0, mMainLeftX + mMainLinearLayout.getWidth(), mMainLinearLayout.getHeight());
            mMainLinearLayout.setScaleY(mMainScaleY);
        }
        if (mMenuLinearLayout != null) {
            mMenuLinearLayout.layout(mMenuLeftX, 0, mMenuLeftX + mMenuLinearLayout.getWidth(), mMenuLinearLayout.getHeight());
            mMenuLinearLayout.setScaleY(mMenuScaleY);
        }

        if (mFrameLayout != null) {
            mFrameLayout.layout(mFrameLayoutLeftX, 0, mFrameLayoutLeftX + mFrameLayout.getWidth(), mFrameLayout.getHeight());
        }
    }

    /**
     * 显示左边菜单
     */
    public void showMenu() {
        if (isShowingMenu()) {
            //左边菜单正在显示，则关闭
            hideMenu();
            return;
        }
        int menuViewWidth = mMenuLinearLayout.getWidth();
        int from = 0;
        int to = menuViewWidth;//main移动menu的距离
        mainAndMenuScroll(from, to);
    }

    /**
     * 主界面和菜单滑动
     *
     * @param from
     * @param to
     */
    private void mainAndMenuScroll(int from, int to) {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }

        mValueAnimator = ValueAnimator.ofInt(from, to);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Number number = (Number) animation.getAnimatedValue();
                updateMainAndMenuLocation(number.intValue());
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //还原类型
                if (isShowingMenu()) {
                    mDragType = RIGHT_TO_LEFT;
                } else {
                    mDragType = mOldDragType;
                }
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(mDuration);
        mValueAnimator.start();
    }

    /**
     * 隐藏菜单
     */
    public void hideMenu() {
        if (!isShowingMenu()) {
            return;
        }
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }

        int menuViewWidth = mMenuLinearLayout.getWidth();
        int from = menuViewWidth;//main移动menu的距离
        int to = 0;
        mainAndMenuScroll(from, to);
    }

    /**
     * 隐藏Fragment
     */
    public void hideFragment() {
        if (mFrameLayouts.size() > 0) {
            int index = mFrameLayouts.size() - 1;
            FrameLayout frameLayout = mFrameLayouts.get(index);
            int leftX = frameLayout.getLeft();
            if (leftX > 0 && leftX < getWidth() / 2) {
                return;
            } else {
                frameLayoutScroll(frameLayout, leftX, getWidth(), index);
            }
        }
    }

    /**
     * 动画更新左边菜单与主界面
     *
     * @param mainCurLeftX main当前的位置
     */
    private void updateMainAndMenuLocation(int mainCurLeftX) {

        if (mainCurLeftX <= 0) {
            mainCurLeftX = 0;
        } else if (mainCurLeftX >= mMenuLinearLayout.getWidth()) {
            mainCurLeftX = mMenuLinearLayout.getWidth();
        }
        mMainLeftX = mainCurLeftX;

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onMainPageScrolled(mMainLeftX);
        }

        //设置main位置
        int mainViewWidth = mMainLinearLayout.getWidth();
        int menuViewWidth = mMenuLinearLayout.getWidth();
        mMainLinearLayout.layout(mainCurLeftX, 0, mainCurLeftX + mainViewWidth, mMainLinearLayout.getHeight());
        mMainMaskView.layout(mainCurLeftX, 0, mainCurLeftX + mainViewWidth, mMainLinearLayout.getHeight());


        //main设置缩放比例
        float dScaleY = mMaxScaleY - mMinScaleY;
        mMainScaleY = mMaxScaleY - (mainCurLeftX * dScaleY / menuViewWidth);
        mMainLinearLayout.setScaleY(mMainScaleY);

        //menu不允许移动，则直接结束
        if (isAllowMenuTranslation) {
            //获取menu移动的距离
            int menuMoveLength = Math.abs(mainViewWidth - menuViewWidth);//menu移动的距离为两个view的相差
            int menuCurMoveLength = getMenuCurMoveLengthByMain(menuMoveLength, menuViewWidth, Math.abs(mainCurLeftX));
            int menuLeftX = menuMoveLength - menuCurMoveLength;
            mMenuLeftX = menuLeftX;
            mMenuLinearLayout.layout(menuLeftX, 0, menuLeftX + mMenuLinearLayout.getWidth(), mMenuLinearLayout.getHeight());

            //menu设置缩放比例
            mMenuScaleY = mMinScaleY + (menuCurMoveLength * dScaleY / menuMoveLength);
            mMenuLinearLayout.setScaleY(mMenuScaleY);
        }


        //设置主界面阴影
        if (isMainMaskPaintFade
                && isShowingMenu()) {
            float percent = mainCurLeftX * 1.0f / getWidth();
            int alpha = (int) (mMinAlpha * percent);
            mMainMaskView.setBackgroundColor(Color.argb(Math.max(alpha, 0), 0, 0, 0));
            mMainMaskView.setTag(alpha);

            if (mMainMaskView.getVisibility() != View.VISIBLE) {
                mMainMaskView.setVisibility(View.VISIBLE);
            }

        } else {
            //设置透明
            if (Integer.parseInt(mMainMaskView.getTag() + "") != 0) {
                mMainMaskView.setBackgroundColor(Color.TRANSPARENT);
                mMainMaskView.setTag(0);
            }

            if (isShowingMenu()) {
                //如果左边菜单打开，则显示遮罩，方便点击主界面时关闭左边菜单
                if (mMainMaskView.getVisibility() != View.VISIBLE) {
                    mMainMaskView.setVisibility(View.VISIBLE);
                }
            } else {
                //隐藏
                if (mMainMaskView.getVisibility() != View.GONE) {
                    mMainMaskView.setVisibility(View.GONE);
                }

            }
        }
    }

    /**
     * 更新Fragment位置
     *
     * @param frameLayout
     * @param leftx
     * @param index
     */
    private void updateFragmentLocation(FrameLayout frameLayout, int leftx, int index) {
        if (leftx <= 0) {
            leftx = 0;
        } else if (leftx >= getWidth()) {
            leftx = getWidth();
        }
        //更新位置
        frameLayout.layout(leftx, 0, leftx + frameLayout.getWidth(), frameLayout.getHeight());
        if (index == 0) {
            mFrameLayoutLeftX = leftx;
            // mFragmentFrameLayout.updateCurFrameLayoutLeftX(leftx);
        } else if (index < mFragmentFrameLayouts.size()) {
            FragmentFrameLayout fragmentFrameLayout = mFragmentFrameLayouts.get(index);
            fragmentFrameLayout.updateNextFrameLayoutLeftX(leftx);
        }
        invalidate();
    }

    /**
     * 获取menu当前移动的距离
     *
     * @param menuMoveLength
     * @param mainMoveLength
     * @param curMainTranslationX
     * @return
     */
    private int getMenuCurMoveLengthByMain(int menuMoveLength, int mainMoveLength, int curMainTranslationX) {
        long menuCurMoveLength = (long) (menuMoveLength * curMainTranslationX) / mainMoveLength;
        return (int) menuCurMoveLength;
    }

    /**
     * 是否正在显示Fragment
     *
     * @return
     */
    public boolean isShowingFragment() {
        return mFragmentFrameLayouts.size() > 0;
    }

    /**
     * 是否正在显示菜单
     *
     * @return
     */
    public boolean isShowingMenu() {
        if (mMainLeftX > 0) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {


        //有fragment，强制只能从左到右滑动
        if (mDragType != LEFT_TO_RIGHT && mFragmentFrameLayouts.size() > 0) {
            mDragType = LEFT_TO_RIGHT;
        }

        if (mDragType == NONE) return super.onInterceptTouchEvent(event);


        boolean intercepted = false;
        float curX = event.getX();
        float curY = event.getY();

        int actionId = event.getAction();
        switch (actionId) {

            case MotionEvent.ACTION_DOWN:
                mLastInterceptX = curX;
                mLastInterceptY = curY;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastInterceptX - curX);
                int deltaY = (int) (mLastInterceptY - curY);

                if (Math.abs(deltaX) > mTouchSlop
                        && Math.abs(deltaY) < mTouchSlop) {

                    if (((deltaX < 0 && mDragType == LEFT_TO_RIGHT) || (deltaX > 0 && mDragType == RIGHT_TO_LEFT)) && !isInIgnoreHorizontalView(event)) {
                        //左右移动
                        intercepted = true;
                    }

                }
                break;
            default:

                break;
        }
        mLastX = curX;
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //有fragment，强制只能从左到右滑动
        if (mDragType != LEFT_TO_RIGHT && mFragmentFrameLayouts.size() > 0) {
            mDragType = LEFT_TO_RIGHT;
        }

        if (mDragType == NONE) return super.onTouchEvent(event);

        float curX = event.getX();
        float curY = event.getY();
        int actionId = event.getAction();
        switch (actionId) {

            case MotionEvent.ACTION_DOWN:
                mLastInterceptY = curY;
                mLastInterceptX = curX;

                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastInterceptX - curX);
                int deltaY = (int) (mLastInterceptY - curY);
                if (isTouchMove || (Math.abs(deltaX) > mTouchSlop
                        && Math.abs(deltaY) < mTouchSlop)) {

                    int dx = (int) (mLastX - curX);

                    //左右移动
                    if ((isTouchMove || (deltaX < 0 && mDragType == LEFT_TO_RIGHT) || (deltaX > 0 && mDragType == RIGHT_TO_LEFT)) && !isInIgnoreHorizontalView(event)) {
                        isTouchMove = true;

                        if (mFrameLayouts.size() > 0) {
                            int index = mFrameLayouts.size() - 1;
                            FrameLayout frameLayout = mFrameLayouts.get(index);
                            updateFragmentLocation(frameLayout, frameLayout.getLeft() - dx, index);
                        } else {
                            int mainLeftX = mMainLeftX;
                            updateMainAndMenuLocation(mainLeftX - dx);
                        }
                    }


                }

                break;
            case MotionEvent.ACTION_UP:

                if (mFrameLayouts.size() > 0) {
                    int index = mFrameLayouts.size() - 1;
                    FrameLayout frameLayout = mFrameLayouts.get(index);
                    int leftX = frameLayout.getLeft();
                    if (leftX < getWidth() / 2) {
                        frameLayoutScroll(frameLayout, leftX, 0, index);
                    } else {
                        frameLayoutScroll(frameLayout, leftX, getWidth(), index);
                    }
                } else {
                    if (mMainLinearLayout.getLeft() < mMenuLinearLayout.getWidth() / 3) {
                        mainAndMenuScroll(mMainLinearLayout.getLeft(), 0);

                    } else {
                        mainAndMenuScroll(mMainLinearLayout.getLeft(), mMenuLinearLayout.getWidth());

                    }
                }
                isTouchMove = false;

                break;
        }
        mLastX = curX;
        return true;
    }

    /**
     * framelayout滑动动画
     *
     * @param frameLayout
     * @param from
     * @param to
     * @param index
     */
    private void frameLayoutScroll(final FrameLayout frameLayout, int from, int to, final int index) {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }

        mValueAnimator = ValueAnimator.ofInt(from, to);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Number number = (Number) animation.getAnimatedValue();
                updateFragmentLocation(frameLayout, number.intValue(), index);
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Fragment fragment = null;
                //
                if (mFrameLayouts.size() > 0) {
                    if (frameLayout.getLeft() > getWidth() / 2) {

                        fragment = mFragmentFrameLayouts.get(index).getFragment();

                        mFrameLayouts.remove(index);
                        mFragmentFrameLayouts.remove(index);
                    }
                }
                if (mFrameLayouts.size() > 0) {
                    //上一个页面重新加载下一个Fragment所在的页面布局
                    mFragmentFrameLayouts.get(mFrameLayouts.size() - 1).reloadNextPage();
                } else {
                    //重新加载下一个Fragment所在的页面布局
                    reloadNextPage();
                }
                //还原手势事件
                mDragType = mOldDragType;

                //fragment关闭回调
                if(fragment != null && mOnPageChangeListener != null){
                    mOnPageChangeListener.onHideFragment(fragment);
                }
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(mDuration);
        mValueAnimator.start();
    }


    ///////////////////////////////////

    /**
     * 添加和显示Fragment
     *
     * @param fragmentManager
     * @param fragment
     */
    public void addAndShowFragment(final FragmentManager fragmentManager, final Fragment fragment) {
        if (isFragmentOpening) return;
        isFragmentOpening = true;

        if (mFragmentFrameLayouts != null && mFragmentFrameLayouts.size() == 0) {
            mFragmentFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mFragmentFrameLayout.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);

                            mFragmentFrameLayouts.add(mFragmentFrameLayout);

                            mFrameLayouts.add(mFrameLayout);
                            int from = mFrameLayout.getLeft();
                            int to = 0;
                            frameLayoutScroll(from, to, mFrameLayout, true, new UpdateLocationListener() {
                                @Override
                                public void updateLeftX(int leftx) {
                                    mFrameLayoutLeftX = leftx;
//                            mFragmentFrameLayout.updateCurFrameLayoutLeftX(leftx);
                                }
                            });
                        }
                    });
            mFragmentFrameLayout.setCurFragment(fragmentManager, fragment);


        } else {
            //
            final FragmentFrameLayout preFragmentFrameLayout = mFragmentFrameLayouts.get(mFragmentFrameLayouts.size() - 1);
            final FragmentFrameLayout curFragmentFrameLayout = preFragmentFrameLayout.getNextFragmentFrameLayout();
            if (curFragmentFrameLayout == null) return;


            curFragmentFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            curFragmentFrameLayout.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);

                            mFragmentFrameLayouts.add(curFragmentFrameLayout);

                            FrameLayout frameLayout = preFragmentFrameLayout.getNextFrameLayout();
                            mFrameLayouts.add(frameLayout);
                            int from = frameLayout.getLeft();
                            int to = 0;
                            frameLayoutScroll(from, to, frameLayout, true, new UpdateLocationListener() {
                                @Override
                                public void updateLeftX(int leftx) {
                                    preFragmentFrameLayout.updateNextFrameLayoutLeftX(leftx);
                                }
                            });
                        }
                    });

            curFragmentFrameLayout.setCurFragment(fragmentManager, fragment);
        }
    }

    /**
     * frameLayout 所在布局滑动
     *
     * @param from
     * @param to
     */
    public void frameLayoutScroll(int from, int to, final View scrollView, boolean isAddOpenFragment, final UpdateLocationListener updateLocationListener) {
        mIsAddOpenFragment = isAddOpenFragment;

        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }

        mValueAnimator = ValueAnimator.ofInt(from, to);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Number number = (Number) animation.getAnimatedValue();
                updateFrameLayoutLocation(scrollView, number.intValue(), updateLocationListener);
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAddOpenFragment = false;
                isFragmentOpening = false;
                //Fragment动画结束后，预加载下一个Fragment所在的界面布局
                int size = mFragmentFrameLayouts.size();
                if (size > 0) {
                    mFragmentFrameLayouts.get(size - 1).loadNextPage();
                }
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(mDuration);
        mValueAnimator.start();
    }

    /**
     * @param scrollView
     * @param viewLeftX
     */
    private void updateFrameLayoutLocation(View scrollView, int viewLeftX, UpdateLocationListener updateLocationListener) {
        if (updateLocationListener != null) {
            updateLocationListener.updateLeftX(viewLeftX);
        }
        scrollView.layout(viewLeftX, 0, viewLeftX + scrollView.getWidth(), scrollView.getHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isFragmentPaintFade && !mIsAddOpenFragment) {
            int size = mFrameLayouts.size();
            if (size > 0) {
                int left = mFrameLayouts.get(size - 1).getLeft();

                float percent = left * 1.0f / getWidth();
                int alpha = mMinAlpha - (int) (mMinAlpha * percent);
                mFragmentFadePaint.setColor(Color.argb(Math.max(alpha, 0), 0, 0, 0));
                canvas.drawRect(0, 0, left, getHeight(), mFragmentFadePaint);
            }
        }

    }

    /**
     * 是否在水平不处理视图中
     *
     * @param event
     * @return
     */
    private boolean isInIgnoreHorizontalView(MotionEvent event) {
        return !isShowingMenu() && mFragmentFrameLayouts.size() == 0 && isInView(mIgnoreHorizontalViews, event);
    }

    /**
     * 是否在view里面
     *
     * @param views
     * @param event
     * @return
     */
    private boolean isInView(List<View> views, MotionEvent event) {
        if (views == null || views.size() == 0)
            return false;
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + view.getWidth();
            int bottom = top + view.getHeight();
            Rect rect = new Rect(left, top, right, bottom);
            if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加不拦截水平view
     *
     * @param ignoreView
     */
    public void addIgnoreHorizontalView(View ignoreView) {
        if (mIgnoreHorizontalViews == null) {
            mIgnoreHorizontalViews = new ArrayList<View>();
        }
        if (!mIgnoreHorizontalViews.contains(ignoreView)) {
            mIgnoreHorizontalViews.add(ignoreView);
        }
    }

    public void setIgnoreHorizontalViews(List<View> ignoreHorizontalViews) {
        this.mIgnoreHorizontalViews = ignoreHorizontalViews;
    }


    /**
     * 是否允许缩放
     *
     * @param isAllow
     */
    public void setAllowScale(boolean isAllow) {
        if (isAllow) {
            mMinScaleY = 0.7f;
        } else {
            mMinScaleY = mMaxScaleY;
        }
    }

    /**
     * 是否允许菜单移动
     *
     * @param isAllow
     */
    public void setAllowMenuTranslation(boolean isAllow) {
        this.isAllowMenuTranslation = isAllow;
        setAllowScale(isAllow);
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setMinAlpha(int minAlpha) {
        this.mMinAlpha = minAlpha;
    }


    public void setMainMaskPaintFade(boolean mainMaskPaintFade) {
        isMainMaskPaintFade = mainMaskPaintFade;
    }

    public void setFragmentPaintFade(boolean fragmentPaintFade) {
        isFragmentPaintFade = fragmentPaintFade;
    }

    public void setDragType(int dragType) {
        this.mDragType = dragType;
        mOldDragType = mDragType;
    }

    /**
     * 更新位置
     */
    private interface UpdateLocationListener {
        void updateLeftX(int leftx);
    }

    public void addOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    public interface OnPageChangeListener {
        /**
         *
         */
        void onMainPageScrolled(int leftx);

        /**
         *
         */
        void onHideFragment(Fragment fragment);
    }


    /**
     *
     */
    public interface SlidingMenuOnListener {
        /**
         * 打开
         *
         * @param fragment
         */
        void addAndShowFragment(Fragment fragment);

        /**
         * 关闭
         */
        void hideFragment();
    }
}
