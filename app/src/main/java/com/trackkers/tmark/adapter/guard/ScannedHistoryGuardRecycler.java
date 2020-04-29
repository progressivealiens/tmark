package com.trackkers.tmark.adapter.guard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.webApi.ApiResponseHistoryGuard;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScannedHistoryGuardRecycler extends ExpandableRecyclerViewAdapter<ScannedHistoryGuardRecycler.ParentViewHolder, ScannedHistoryGuardRecycler.ChildrenViewHolder> {

    private Context context;

    public ScannedHistoryGuardRecycler(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        this.context = context;
    }

    @Override
    public ParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_assigned_history, parent, false);

        return new ParentViewHolder(view);
    }

    @Override
    public ChildrenViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_guard_history, parent, false);

        return new ChildrenViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ChildrenViewHolder viewHolder, int flatPosition, ExpandableGroup group, int childIndex) {
        ChildrenViewHolder holder = viewHolder;

        ApiResponseHistoryGuard.DataBean.CheckPointsScanDetailsBean checkPointsScanDetailsBean =
                ((ApiResponseHistoryGuard.DataBean) group).getItems().get(childIndex);

        holder.setChildDetails(checkPointsScanDetailsBean, group);
    }

    @Override
    public void onBindGroupViewHolder(ParentViewHolder viewHolder, int flatPosition, ExpandableGroup group) {
        ParentViewHolder holder = viewHolder;

        holder.setSiteDetails(group);
    }

    public class ParentViewHolder extends GroupViewHolder {
        @BindView(R.id.tv_site_name)
        TextView tvSiteName;
        @BindView(R.id.tv_route_name)
        TextView tvRouteName;
        @BindView(R.id.iv_selfie)
        RoundedImageView ivSelfie;
        @BindView(R.id.tv_checkin_time)
        MyTextview tvCheckinTime;
        @BindView(R.id.tv_checkout_time)
        MyTextview tvCheckoutTime;

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
        void setSiteDetails(ExpandableGroup parent) {
            tvSiteName.setText(PrefData.getInstance().getResources().getString(R.string.site) + " " + ((ApiResponseHistoryGuard.DataBean) parent).getSiteName());
            tvRouteName.setText(PrefData.getInstance().getResources().getString(R.string.route) + " " + ((ApiResponseHistoryGuard.DataBean) parent).getRouteName());
            tvCheckinTime.setText(PrefData.getInstance().getResources().getString(R.string.checkin_time) + " " + ((ApiResponseHistoryGuard.DataBean) parent).getCheckInTime());
            tvCheckoutTime.setText(PrefData.getInstance().getResources().getString(R.string.checkout_time) + " " + ((ApiResponseHistoryGuard.DataBean) parent).getCheckOutTime());
            Picasso.get().load(Utils.BASE_IMAGE + ((ApiResponseHistoryGuard.DataBean) parent).getStartImageName()).into(ivSelfie);

        }
    }

    public class ChildrenViewHolder extends ChildViewHolder {
        @BindView(R.id.tv_checkpoint_name)
        TextView tvCheckpointName;
        @BindView(R.id.tv_checkpoint_date)
        TextView tvCheckpointDate;
        @BindView(R.id.tv_checkpoint_time)
        TextView tvCheckpointTime;
        @BindView(R.id.tv_checkpoint_round_no)
        TextView tvCheckpointRoundNo;

        ChildrenViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        void setChildDetails(ApiResponseHistoryGuard.DataBean.CheckPointsScanDetailsBean chkDetails, ExpandableGroup group) {
            tvCheckpointName.setText(PrefData.getInstance().getResources().getString(R.string.checkpoint_name) + " " + chkDetails.getCheckpointName());
            tvCheckpointDate.setText(PrefData.getInstance().getResources().getString(R.string.date) + " " + chkDetails.getScanDate());
            tvCheckpointTime.setText(PrefData.getInstance().getResources().getString(R.string.time) + " " + chkDetails.getScanTime());
            tvCheckpointRoundNo.setText(PrefData.getInstance().getResources().getString(R.string.round_number) + " " + chkDetails.getTrip());
        }
    }

}