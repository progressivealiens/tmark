package com.trackkers.tmark.views.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.rd.PageIndicatorView;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.PrefData;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroActivity extends AppCompatActivity {

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.pageIndicatorView)
    PageIndicatorView pageIndicatorView;
    @BindView(R.id.tv_skip)
    TextView tvSkip;
    CustomViewPager customViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        customViewPager = new CustomViewPager(IntroActivity.this);
        viewPager.setAdapter(customViewPager);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PrefData.writeBooleanPref(PrefData.isFirstTimeRun,true);

                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    public class CustomViewPager extends PagerAdapter {

        Context context;

        public CustomViewPager(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }


        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View itemview = inflater.inflate(R.layout.view_pager_items, container, false);

            ImageView imageView = itemview.findViewById(R.id.iv_images);
            MyTextview heading = itemview.findViewById(R.id.tv_heading);
            MyTextview content = itemview.findViewById(R.id.tv_content);


            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                }

                @Override
                public void onPageSelected(int i) {

                    switch (i) {
                        case 0:
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.selfie_final));
                            heading.setText(getResources().getString(R.string.selfie_based_attendance));
                            content.setText(getResources().getString(R.string.unlike_the_traditional));
                            tvSkip.setText(getString(R.string.skip_introduction));
                            break;
                        case 1:
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.communication_final));
                            heading.setText(R.string.communication);
                            content.setText(R.string.post_all_issues);
                            tvSkip.setText(getString(R.string.skip_introduction));
                            break;
                        case 2:
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.third_scanning_option2));
                            heading.setText(R.string.security);
                            content.setText(R.string.meet_the_new_approach);
                            tvSkip.setText(getString(R.string.proceed));
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                }
            });

            container.addView(itemview);

            return itemview;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
