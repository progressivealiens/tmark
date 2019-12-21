package com.trackkers.tmark.adapter.guard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class AssignedRouteRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<ApiResponse.DataBean> assignedRouteModels = new ArrayList<>();


    public AssignedRouteRecycler(Context context, List<ApiResponse.DataBean> assignedRouteModels) {
        this.context = context;
        this.assignedRouteModels = assignedRouteModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_assigned_route, parent, false);

        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {

        ViewHolder holder = (ViewHolder) viewHolder;

        holder.tvSiteName.setText(context.getString(R.string.site) + assignedRouteModels.get(i).getSiteName());
        holder.tvRouteName.setText(context.getString(R.string.route) + assignedRouteModels.get(i).getRouteName());


        if (assignedRouteModels.size() == 1) {
            PrefData.writeStringPref(PrefData.route_id, String.valueOf(assignedRouteModels.get(i).getRouteId()));
            PrefData.writeStringPref(PrefData.route_name, assignedRouteModels.get(i).getRouteName());
            Log.e("fromAdapter", assignedRouteModels.get(i).getRouteName());
            context.startActivity(new Intent(context, GCheckpoints.class));
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefData.writeStringPref(PrefData.route_id, String.valueOf(assignedRouteModels.get(i).getRouteId()));
                PrefData.writeStringPref(PrefData.route_name, assignedRouteModels.get(i).getRouteName());
                Log.e("fromAdapter", assignedRouteModels.get(i).getRouteName());
                context.startActivity(new Intent(context, GCheckpoints.class));
            }
        });

    }

    @Override
    public int getItemCount() {
        return assignedRouteModels.size();
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
