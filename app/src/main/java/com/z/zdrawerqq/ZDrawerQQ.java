package com.z.zdrawerqq;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

/*
    TODO: 左边菜单判断是 上下滑动 还是 左右滑动 一定的距离才进行两边选其一

    HorizontalScrollView 垂直滑动，不起作用

    1.去掉缩放，透明度

    2.加阴影
 */
public class ZDrawerQQ extends HorizontalScrollView {

    private static final String TAG = "ZDrawerKugou";
    private final int mMenuWidth;
    private View mMenuView;
    private View mContentView;

    /*
        手势处理类
     */
    private boolean mIntercepted;
    private boolean mMenuOpened;
    private final GestureDetector mGestureDetector;
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        /*
            快速滑动
                右边快速滑动，打开
                左边快速滑动，关闭

            左边快速滑动，关闭
                E/ZDrawerKugou: velocityX: -16000.0, velocityY: 6418.9883

            右边快速滑动，打开
                E/ZDrawerKugou: velocityX: 13991.457, velocityY: 548.2163

            分析:
                onTouchEvent

                    onGestureDetector
                        菜单状态为打开
                            左边快速滑动 -> closeMenu

                    onTouchEvent继续往下走
                        距离变化小，所以二选一，还是openMenu
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // 快速滑动才会触发
            Log.e(TAG, "velocityX: " + velocityX + ", velocityY: " + velocityY);

            if (mMenuOpened) {
                if (velocityX < 0) {
                    closeMenu();
                    return true;
                }
            } else {
                if (velocityX > 0) {
                    openMenu();
                    return true;
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };
    private View mShadowView;

    public ZDrawerQQ(Context context) {
        this(context, null);
    }

    public ZDrawerQQ(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZDrawerQQ(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /*
            1. 宽度不对?
                内容页面宽度为屏幕的宽度
                菜单页面的宽度为 屏幕宽度 - 右边一小部分的宽度(自定义属性)
         */
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZDrawerQQ);
        float menuRightSpacing = typedArray.getDimension(R.styleable.ZDrawerQQ_menuRightSpacing, dip2px(context, 50));
        mMenuWidth = (int) (getScreenWidth(context) - menuRightSpacing);
        typedArray.recycle();

        /*
            5. 处理快速滑动
         */
        mGestureDetector = new GestureDetector(context, mGestureListener);
    }


    /*
        onCreate方法中布局文件转换成View实例后会调用onFinishInflate
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);
        if (container.getChildCount() != 2) {
            throw new RuntimeException("只允许存放两个子View");
        }

        // 菜单
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuLP = mMenuView.getLayoutParams();
        menuLP.width = mMenuWidth;
        mMenuView.setLayoutParams(menuLP); // 不加这一句，7.0以下是正常的

        // 内容
        /*
            将内容布局拿出来，然后外面包一层阴影，最后放回去原来的位置
         */
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentLP = mContentView.getLayoutParams();
        container.removeView(mContentView);

        RelativeLayout contentContainer = new RelativeLayout(getContext());
        contentContainer.addView(mContentView);

        mShadowView = new View(getContext());
        mShadowView.setBackgroundColor(Color.parseColor("#99000000"));
        mShadowView.setAlpha(0f);
        contentContainer.addView(mShadowView);

        contentLP.width = getScreenWidth(getContext());
        container.addView(contentContainer, contentLP);

        // 2. 让内容完整显示在屏幕，不起作用，onFinishInflate是在setContentView之后，在onLayout之前
        //scrollTo(mMenuWidth, 0);

        // 3. 监听滑动
        //setOnScrollChangeListener();
    }

    /*
        4. 处理右边的缩放，左边的缩放和透明度
            不断获取当前滚动位置
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        //Log.e(TAG, "l: " + l);

        // 算一个梯度值
        float scale = 1f * l / mMenuWidth;  // 1 -> 0，scale == [0, 1]

        // alpha: 0 -> 1
        float maskAlpha = 1 - scale;
        mShadowView.setAlpha(maskAlpha);

//        // 内容的缩放[0.7f, 1f]，默认是按中心点缩放
//        float contentScale = 0.7f + (0.3f * scale); // 1 -> 0.7
//        mContentView.setPivotX(0);
//        mContentView.setPivotY(mContentView.getMeasuredHeight() * 1f / 2);
//        mContentView.setScaleX(contentScale);
//        mContentView.setScaleY(contentScale);
//
//        // 菜单透明度[0.3f, 1.0f]和缩放[0.7f, 1f]，默认是按中心点缩放
//        float menuAlpha = 0.3f + 0.7f * (1 - scale); // 0.7 -> 1
//        mMenuView.setAlpha(menuAlpha);
//        float menuScale = 0.7f + 0.3f * (1 - scale); // 0.5 -> 1
//        mMenuView.setScaleX(menuScale);
//        mMenuView.setScaleY(menuScale);

        // 注释掉上面所有
        //mMenuView.setTranslationX(l);

        // 退出按钮保持在左边紧靠着内容这边，平移小一点
        mMenuView.setTranslationX(0.2f * l);
    }

    /*
        onLayout是onFinishInflate之后执行的
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed) {
            /*
                2. 让内容完整显示在屏幕
             */
            scrollTo(mMenuWidth, 0);
        }
    }

    /*
        3. 手指抬起，两边选一边
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mIntercepted || mGestureDetector.onTouchEvent(ev)) {
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 根据当前滚动的距离来判断
            if (getScrollX() > mMenuWidth / 2) { // 关闭
                //scrollTo(mMenuWidth, 0); // 没有动画
                closeMenu();
            } else { // 打开
                //scrollTo(0, 0); // 没有动画
                openMenu();
            }
            /*
                不执行父类的方法
                原来的情况
                    我们的scroll
                    父类的scroll
             */
            return true;
        }

        /*
            true表示消费事件，
            若ACTION_DOWN不返回true，那么ACTION_MOVE，ACTION_UP事件是不会再进到该方法

            ACTION_DOWN和ACTION_MOVE交由父类处理
         */
        return super.onTouchEvent(ev);
    }

    private void openMenu() {
        smoothScrollTo(0, 0);
        mMenuOpened = true;
    }

    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuOpened = false;
    }

    /*
        6. 当菜单打开的时候，手指触摸右边内容时，需要关闭菜单
            菜单打开的时候，点击右边内容时，里面的控件的点击被忽略

            出现的问题: 需要点击两次
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        mIntercepted = false;

        if (mMenuOpened && ev.getX() > mMenuWidth) {

            mIntercepted = true;

            // 关闭菜单
            closeMenu();
            /*
                true表示拦截子View点击事件
                但是会执行子View父布局的onTouch事件
             */
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /*
        获得屏幕宽度
     */
    private static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /*
        Dip to pixels
        Resources.getSystem().getDisplayMetrics()
     */
    private static int dip2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /*
        Pixels to dip
     */
    private static int px2dip(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
