package com.trackkers.tmark.adapter.operations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.squareup.picasso.Picasso;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.webApi.ApiResponseHistoryOperational;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OperationalHistoryRecycler extends ExpandableRecyclerViewAdapter<OperationalHistoryRecycler.ParentViewHolder, OperationalHistoryRecycler.ChildrenViewHolder> {

    private Context context;

    public OperationalHistoryRecycler(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        this.context = context;
    }

    @Override
    public ParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_operational_history, parent, false);

        return new ParentViewHolder(view);
    }

    @Override
    public ChildrenViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_operational_history, parent, false);

        return new ChildrenViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ChildrenViewHolder viewHolder, int flatPosition, ExpandableGroup group, int childIndex) {
        ChildrenViewHolder holder = viewHolder;


        ApiResponseHistoryOperational.DataBean.CommunicationsListDataBean communicationsListDataBean =
                ((ApiResponseHistoryOperational.DataBean) group).getItems().get(childIndex);

        holder.setPostDetails(communicationsListDataBean, group);

    }


    @Override
    public void onBindGroupViewHolder(ParentViewHolder viewHolder, int flatPosition, ExpandableGroup group) {
        ParentViewHolder holder = viewHolder;

        holder.setPostDetails(group);
    }


    public class ParentViewHolder extends GroupViewHolder {

        @BindView(R.id.tv_checkin_time)
        MyTextview tvCheckinTime;
        @BindView(R.id.tv_checkout_time)
        MyTextview tvCheckoutTime;
        @BindView(R.id.tv_message)
        MyTextview tvMessage;
        @BindView(R.id.tv_time)
        MyTextview tvTime;
        @BindView(R.id.parent_card_view)
        CardView parentCardView;
        @BindView(R.id.iv_checkin_selfie)
        ImageView ivCheckinSelfie;

        ParentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void expand() {
            super.expand();
        }

        @Override
        public void collapse() {
            super.collapse();
        }

        @SuppressLint("SetTextI18n")
        void setPostDetails(ExpandableGroup parent) {
            tvCheckinTime.setText(PrefData.getInstance().getResources().getString(R.string.checkin_time) + " " + ((ApiResponseHistoryOperational.DataBean) parent).getCheckInTime());
            tvCheckoutTime.setText(PrefData.getInstance().getResources().getString(R.string.checkout_time) + " " + ((ApiResponseHistoryOperational.DataBean) parent).getCheckOutTime());
            tvMessage.setText(PrefData.getInstance().getResources().getString(R.string.message) + " " + ((ApiResponseHistoryOperational.DataBean) parent).getMessage());
            tvTime.setText(PrefData.getInstance().getResources().getString(R.string.time) + " " + ((ApiResponseHistoryOperational.DataBean) parent).getWorkingHours());
            Picasso.get().load(Utils.BASE_IMAGE_OPERATIONS_Checkin + ((ApiResponseHistoryOperational.DataBean) parent).getStartImageName()).placeholder(R.drawable.progress_animation).into(ivCheckinSelfie);
        }
    }


    public class ChildrenViewHolder extends ChildViewHolder {
        @BindView(R.id.tv_post_text)
        MyTextview tvPostText;
        @BindView(R.id.tv_post_time)
        MyTextview tvPostTime;
        @BindView(R.id.iv_post_image)
        ImageView ivPostImage;

        ChildrenViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        void setPostDetails(ApiResponseHistoryOperational.DataBean.CommunicationsListDataBean chkDetails, ExpandableGroup group) {

            tvPostTime.setText(PrefData.getInstance().getResources().getString(R.string.posted_at) + " " + chkDetails.getTime());

            if (chkDetails.getType().equalsIgnoreCase("BOTH")) {

                tvPostText.setVisibility(View.VISIBLE);
                ivPostImage.setVisibility(View.VISIBLE);
                tvPostText.setText(PrefData.getInstance().getResources().getString(R.string.comment) + " " + chkDetails.getText());
                Picasso.get().load(Utils.BASE_IMAGE_OPERATIONS + (chkDetails).getImage()).placeholder(R.drawable.progress_animation).into(ivPostImage);

            } else if (chkDetails.getType().equalsIgnoreCase("TEXT")) {
                tvPostText.setVisibility(View.VISIBLE);
                tvPostText.setText(PrefData.getInstance().getResources().getString(R.string.comment) + " " + chkDetails.getText());
            } else {
                ivPostImage.setVisibility(View.VISIBLE);
                Picasso.get().load(Utils.BASE_IMAGE_OPERATIONS + (chkDetails).getImage()).placeholder(R.drawable.progress_animation).into(ivPostImage);
            }
        }
    }
}
