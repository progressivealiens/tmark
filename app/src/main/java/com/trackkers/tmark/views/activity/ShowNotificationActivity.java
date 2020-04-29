package com.trackkers.tmark.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowNotificationActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.tv_subtitle)
    MyTextview tvSubtitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_notification_title)
    TextView tvNotificationTitle;
    @BindView(R.id.tv_notification_subject)
    TextView tvNotificationSubject;
    @BindView(R.id.tv_notification_body)
    TextView tvNotificationBody;
    @BindView(R.id.iv_company_logo)
    RoundedImageView ivCompanyLogo;
    @BindView(R.id.root_notification)
    RelativeLayout rootNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notification);
        ButterKnife.bind(this);

        initialize();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("NotificationTitle");
            String subject = extras.getString("NotificationSubject");
            String msg = extras.getString("NotificationMessage");


            tvNotificationTitle.setText("Title :-" + " " + title);
            tvNotificationSubject.setText("Subject :-" + " " + subject);
            tvNotificationBody.setText("Message :-" + " " + msg);
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void initialize() {
        setSupportActionBar(toolbar);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText("Notification Detail");
        Picasso.get().load(Utils.BASE_IMAGE_COMPANY + PrefData.readStringPref(PrefData.company_logo)).placeholder(R.drawable.progress_animation).into(ivCompanyLogo);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
