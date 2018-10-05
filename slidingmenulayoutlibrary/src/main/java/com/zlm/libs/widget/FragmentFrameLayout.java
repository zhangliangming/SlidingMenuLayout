package com.zlm.libs.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;


/**
 * Created by zhangliangming on 2018-06-17.
 */

public class FragmentFrameLayout extends FrameLayout {

    /**
     * 当前的布局
     */
    private FrameLayout mCurFrameLayout;

    /**
     * 下一个界面的布局
     */
    private FrameLayout mNextFrameLayout;
    /**
     * 下一个界面布局
     */
    private FragmentFrameLayout mNextFragmentFrameLayout;

    private Context mContext;

    private DisplayMetrics mDisplayMetrics;

    /**
     *
     */
    private int mCurFragmentLayoutLeftX, mNextFrameLayoutLeftX;

    public FragmentFrameLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FragmentFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        this.mContext = context;
        onAttachView();
    }

    /**
     * 初始视图
     */
    private void onAttachView() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        display.getMetrics(mDisplayMetrics);

        setId(View.generateViewId());
        //添加当前FrameLayout布局
        mCurFrameLayout = new FrameLayout(mContext);
        mCurFrameLayout.setId(View.generateViewId());
        FrameLayout.LayoutParams curFrameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        addView(mCurFrameLayout, curFrameLayoutParams);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mCurFrameLayout != null) {
            mCurFrameLayout.layout(mCurFragmentLayoutLeftX, 0, mCurFragmentLayoutLeftX + mCurFrameLayout.getWidth(), mCurFrameLayout.getHeight());
        }

        if (mNextFrameLayout != null) {
            mNextFrameLayout.layout(mNextFrameLayoutLeftX, 0, mNextFrameLayoutLeftX + mNextFrameLayout.getWidth(), mNextFrameLayout.getHeight());
        }
    }

    /**
     * @param fragmentManager
     * @param curFragment
     */
    public void setCurFragment(FragmentManager fragmentManager, Fragment curFragment) {
        fragmentManager.beginTransaction().add(mCurFrameLayout.getId(), curFragment).commit();
    }

    /**
     * 加载下一下页面
     */
    public void loadNextPage() {

        //下一个FrameLayout布局
        mNextFrameLayout = new FrameLayout(mContext);
        mNextFrameLayout.setId(View.generateViewId());
        FrameLayout.LayoutParams nextFrameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        addView(mNextFrameLayout, nextFrameLayoutParams);
        mNextFrameLayoutLeftX = mDisplayMetrics.widthPixels;

        //添加下一个界面布局
        mNextFragmentFrameLayout = new FragmentFrameLayout(mContext);
        mNextFragmentFrameLayout.setId(View.generateViewId());
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mNextFrameLayout.addView(mNextFragmentFrameLayout, frameLayoutParams);
    }

    /**
     * 重新加载
     */
    public void reloadNextPage() {
        removeView(mNextFrameLayout);
        loadNextPage();
    }

    /**
     * 获取下一个页面
     *
     * @return
     */
    public FragmentFrameLayout getNextFragmentFrameLayout() {
        return mNextFragmentFrameLayout;
    }

    public FrameLayout getNextFrameLayout() {
        return mNextFrameLayout;
    }


//    /**
//     * 更新
//     *
//     * @param leftx
//     */
//    public void updateCurFrameLayoutLeftX(int leftx) {
//        this.mCurFragmentLayoutLeftX = leftx;
//    }

    public void updateNextFrameLayoutLeftX(int leftx) {
        this.mNextFrameLayoutLeftX = leftx;
    }

}
