package temo.com.surfaceviewanimation;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import temo.com.surfaceviewanimation.view.SurfaceViewAnimation;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {


    private List<View> views;

    private SparseArray<SurfaceViewAnimation> animationViewSparseArray;

    private List<int[]> mBitmapResourceIds;

    private List mDurations;

    // 当前viewpager位置
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置Adapter
        initView();
        initAnimation(0);
    }


    private void initView() {
        mBitmapResourceIds = new ArrayList<>();
        mBitmapResourceIds.add(Const.guide_animation_1);
        mBitmapResourceIds.add(Const.guide_animation_2);
        mBitmapResourceIds.add(Const.guide_animation_3);

        mDurations = new ArrayList();// 三个动画分别的时长
        mDurations.add(34);
        mDurations.add(28);
        mDurations.add(30);

        views = new ArrayList<>();
        animationViewSparseArray = new SparseArray<>();
        ViewPager vp = findViewById(R.id.vp);
        for (int i = 0; i < mBitmapResourceIds.size(); i++) {
            View inflate = LayoutInflater.from(this).inflate(R.layout.activity_vp_layout, null);
            SurfaceViewAnimation imgLoad = inflate.findViewById(R.id.img_load);
            animationViewSparseArray.put(i, imgLoad);
            views.add(inflate);
        }
        //设置Adapter
        vp.setAdapter(new ViewPagerAdapter(views));
        vp.addOnPageChangeListener(this);
        initAnimation(0);
    }


    private void initAnimation(int position) {
        SurfaceViewAnimation surfaceViewAnimation = animationViewSparseArray.get(position);
        surfaceViewAnimation.setBitmapResourceIds(mBitmapResourceIds.get(position));
        surfaceViewAnimation.setDuration((int) mDurations.get(position));
        surfaceViewAnimation.start();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // 开始动画
        initAnimation(position);
        currentIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 1) {// 划走动画（这里有问题）
            SurfaceViewAnimation surfaceViewAnimation = animationViewSparseArray.get(currentIndex);
            surfaceViewAnimation.rotateAnimation();
        }
    }


    // 适配器类为焦点图适配数据
    public class ViewPagerAdapter extends PagerAdapter {

        //界面列表
        private List<View> views;

        public ViewPagerAdapter(List<View> views) {
            this.views = views;
        }

        //销毁arg1位置的界面
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));
        }

        //获得当前界面数
        @Override
        public int getCount() {
            if (views != null) {
                return views.size();
            }
            return 0;
        }

        //初始化arg1位置的界面
        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1), 0);
            return views.get(arg1);
        }

        //判断是否由对象生成界面
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (arg0 == arg1);
        }

    }
}
