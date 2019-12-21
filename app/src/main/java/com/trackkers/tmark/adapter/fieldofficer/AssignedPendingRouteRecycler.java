package com.trackkers.tmark.adapter.fieldofficer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.views.activity.fieldofficer.AssignGuardFromRoute;
import com.trackkers.tmark.views.activity.fieldofficer.FOCheckpoints;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AssignedPendingRouteRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<ApiResponse.DataBean> assignedPendingRouteModel = new ArrayList<>();
    private String calledClass;

    public AssignedPendingRouteRecycler(Context context, List<ApiResponse.DataBean> assignedPendingRouteModel, String calledClass) {
        this.context = context;
        this.assignedPendingRouteModel = assignedPendingRouteModel;
        this.calledClass = calledClass;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_assigned_pending_route, parent, false);

        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {

        ViewHolder holder = (ViewHolder) viewHolder;

        holder.tvSiteName.setText(context.getString(R.string.site) + assignedPendingRouteModel.get(i).getSiteName());
        holder.tvRouteName.setText(context.getString(R.string.route) + assignedPendingRouteModel.get(i).getRouteName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefData.writeStringPref(PrefData.route_id, String.valueOf(assignedPendingRouteModel.get(i).getRouteId()));
                PrefData.writeStringPref(PrefData.route_name, assignedPendingRouteModel.get(i).getRouteName());

                if (calledClass.equalsIgnoreCase("assignment")) {
                    context.startActivity(new Intent(context, AssignGuardFromRoute.class));
                } else if (calledClass.equalsIgnoreCase("checkpoints")) {
                    context.startActivity(new Intent(context, FOCheckpoints.class));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return assignedPendingRouteModel.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_site_name)
        TextView tvSiteName;
        @BindView(R.id.tv_route_name)
        TextView tvRouteName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


        }
    }

}
