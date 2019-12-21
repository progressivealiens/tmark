package com.trackkers.tmark.adapter.fieldofficer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.views.activity.fieldofficer.FOCheckpoints;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FOCheckpointsRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ApiResponse.DataBean> foCheckpointsModels = new ArrayList<>();


    public FOCheckpointsRecycler(Context context, List<ApiResponse.DataBean> foCheckpointsModels) {
        this.context = context;
        this.foCheckpointsModels = foCheckpointsModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_fo_checkpoints, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        final ViewHolder holder = (ViewHolder) viewHolder;

        holder.tvCheckpointName.setText(foCheckpointsModels.get(i).getCheckPointName());

        if (foCheckpointsModels.get(i).isAssignedToMe()) {
            holder.ivAssignedCheckpoint.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check_circle_white_24dp));
        } else {
            holder.ivAssignedCheckpoint.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cancel_white_24dp));
        }

        if (foCheckpointsModels.get(i).isCheckPointIsVerified()) {
            holder.ivStatusCheckpoint.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check_circle_white_24dp));
        } else {
            holder.ivStatusCheckpoint.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cancel_white_24dp));
        }
        holder.tvCheckpointNo.setText(String.valueOf(i + 1));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (foCheckpointsModels.get(i).isCheckPointIsVerified()) {
                    Toast.makeText(context, context.getResources().getString(R.string.checkpoint_already_verified), Toast.LENGTH_SHORT).show();
                } else {


                    PrefData.writeStringPref(PrefData.checkpoint_id, String.valueOf(foCheckpointsModels.get(i).getCheckPointId()));
                    PrefData.writeStringPref(PrefData.checkpoint_id_position, String.valueOf(i));
                    Toast.makeText(context, context.getResources().getString(R.string.volume_up_for_torch), Toast.LENGTH_LONG).show();

                    FOCheckpoints.qrScanFO.setOrientationLocked(true);
                    FOCheckpoints.qrScanFO.initiateScan();

                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return foCheckpointsModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_checkpoint_name)
        TextView tvCheckpointName;
        @BindView(R.id.iv_assigned_checkpoint)
        ImageView ivAssignedCheckpoint;
        @BindView(R.id.iv_status_checkpoint)
        ImageView ivStatusCheckpoint;
        @BindView(R.id.tv_checkpoint_no)
        TextView tvCheckpointNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
