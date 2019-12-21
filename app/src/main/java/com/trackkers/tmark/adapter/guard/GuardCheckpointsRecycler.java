package com.trackkers.tmark.adapter.guard;

import android.content.Context;
import android.util.Log;
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
import com.trackkers.tmark.views.activity.guard.GCheckpoints;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GuardCheckpointsRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ApiResponse.DataBean> guardCheckpointsModels = new ArrayList<>();

    public GuardCheckpointsRecycler(Context context, List<ApiResponse.DataBean> guardCheckpointsModels) {
        this.context = context;
        this.guardCheckpointsModels = guardCheckpointsModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_guard_checkpoints, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        ViewHolder holder = (ViewHolder) viewHolder;

        holder.tvCheckpointName.setText(guardCheckpointsModels.get(i).getCheckPointName());
        holder.tvCheckpointNo.setText(String.valueOf(i + 1));

        if (guardCheckpointsModels.get(i).isIsVerified()) {
            holder.ivStatusCheckpoint.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check_circle_white_24dp));
        } else {
            holder.ivStatusCheckpoint.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cancel_white_24dp));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefData.writeStringPref(PrefData.checkpoint_id, String.valueOf(guardCheckpointsModels.get(i).getCheckPointId()));
                PrefData.writeStringPref(PrefData.checkpoint_id_position, String.valueOf(i));
                Toast.makeText(context, context.getResources().getString(R.string.volume_up_for_torch), Toast.LENGTH_LONG).show();
                Log.e("adapterCheckpointId",String.valueOf(guardCheckpointsModels.get(i).getCheckPointId()));
                GCheckpoints.qrScan.setOrientationLocked(true);
                GCheckpoints.qrScan.initiateScan();

            }
        });
    }

    @Override
    public int getItemCount() {
        return guardCheckpointsModels.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_checkpoint_name)
        TextView tvCheckpointName;
        @BindView(R.id.tv_checkpoint_no)
        TextView tvCheckpointNo;
        @BindView(R.id.iv_status_checkpoint)
        ImageView ivStatusCheckpoint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
