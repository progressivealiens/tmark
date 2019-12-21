package com.trackkers.tmark.adapter.fieldofficer;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.model.fieldofficer.HistoryChild;
import com.trackkers.tmark.model.fieldofficer.HistoryParent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FOHistoryAdapter extends ExpandableRecyclerViewAdapter<FOHistoryAdapter.ParentViewHolder, FOHistoryAdapter.ChildrenViewHolder> {

    public FOHistoryAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public ParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_assigned_history, parent, false);

        return new ParentViewHolder(view);
    }

    @Override
    public ChildrenViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_assigned_history, parent, false);


        return new ChildrenViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ChildrenViewHolder viewHolder, int flatPosition, ExpandableGroup group, int childIndex) {
        ChildrenViewHolder holder = viewHolder;

        HistoryChild historyChild = ((HistoryParent) group).getItems().get(childIndex);
        holder.setCheckpointDetails(historyChild.getName(), historyChild.getDateTime(), historyChild.getAddress());
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

        ParentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        void setSiteDetails(ExpandableGroup parent) {
            if (parent instanceof HistoryParent) {
                tvSiteName.setText(PrefData.getInstance().getResources().getString(R.string.site) + " " + ((HistoryParent) parent).getSiteName());
                tvRouteName.setText(PrefData.getInstance().getResources().getString(R.string.route) + " " + ((HistoryParent) parent).getRouteName());
            }
        }
    }

    public class ChildrenViewHolder extends ChildViewHolder {
        @BindView(R.id.tv_checkpoint_name)
        TextView tvCheckpointName;
        @BindView(R.id.tv_checkpoint_date_time)
        TextView tvCheckpointDateTime;
        @BindView(R.id.tv_checkpoint_address)
        TextView tvCheckpointAddress;

        ChildrenViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        void setCheckpointDetails(String CheckpointName, String CheckpointDateTime, String CheckpointAddress) {
            tvCheckpointName.setText(PrefData.getInstance().getResources().getString(R.string.checkpoint_name) + " " + CheckpointName);
            tvCheckpointDateTime.setText(PrefData.getInstance().getResources().getString(R.string.date_amp_time) + " " + CheckpointDateTime);
            tvCheckpointAddress.setText(PrefData.getInstance().getResources().getString(R.string.address) + " " + CheckpointAddress);
        }
    }
}
