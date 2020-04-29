package com.trackkers.tmark.adapter.guard;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PictureCapturingListener;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.guard.GCheckpoints;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GuardCheckpointsRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity context;
    private List<ApiResponse.DataBean> guardCheckpointsModels = new ArrayList<>();
    PictureCapturingListener pictureCapturingListener;

    public GuardCheckpointsRecycler(AppCompatActivity context, List<ApiResponse.DataBean> guardCheckpointsModels, PictureCapturingListener pictureCapturingListener) {
        this.context = context;
        this.guardCheckpointsModels = guardCheckpointsModels;
        this.pictureCapturingListener = pictureCapturingListener;
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

                if (guardCheckpointsModels.get(i).isIsVerified()) {
                    Utils.showToast(context, context.getString(R.string.checkpoints_already_verified), Toast.LENGTH_SHORT, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                } else {
                    PrefData.writeStringPref(PrefData.checkpoint_id, String.valueOf(guardCheckpointsModels.get(i).getCheckPointId()));
                    PrefData.writeStringPref(PrefData.checkpoint_id_position, String.valueOf(i));
                    GCheckpoints.progressView.showLoader();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GCheckpoints.isSpyImageTaken = false;
                            GCheckpoints.isSpyImageFound = false;
                            GCheckpoints.pictureService.startCapturing(pictureCapturingListener);
                        }
                    }, 200);
                }
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
