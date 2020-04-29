package com.trackkers.tmark.adapter.fieldofficer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.fieldofficer.AssignGuardFromRoute;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignedGuardOnRouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity context;
    List<ApiResponse.DataBean> guardsAssignedOnRoute = new ArrayList<>();

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;


    public AssignedGuardOnRouteAdapter(Activity context, List<ApiResponse.DataBean> guardsAssignedOnRoute) {
        this.context = context;
        this.guardsAssignedOnRoute = guardsAssignedOnRoute;
        prefData = new PrefData(context);
        apiInterface = ApiClient.getClient(context).create(ApiInterface.class);
        progressView = new ProgressView(context);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_guards_assigned_on_route, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ViewHolder holder = (ViewHolder) viewHolder;

        holder.tvGuardName.setText(guardsAssignedOnRoute.get(i).getName());
        holder.tvSerialNo.setText(String.valueOf(i + 1));

        holder.ivUnassignGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectApiToUnassignGuard(String.valueOf(guardsAssignedOnRoute.get(i).getEmployeeId()), i);
            }
        });
    }

    private void connectApiToUnassignGuard(String employeeId, int position) {
        if (CheckNetworkConnection.isConnection1(context, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.unassignGuardToRoute(
                    employeeId,
                    PrefData.readStringPref(PrefData.route_id)
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(context.getString(R.string.success))) {

                                Utils.showToast(context, context.getResources().getString(R.string.guard_unassigned_sucess), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorLightGreen), context.getResources().getColor(R.color.colorWhite));

                                guardsAssignedOnRoute.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, guardsAssignedOnRoute.size());

                            } else {

                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {

                                    Utils.showToast(context, context.getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                                    Utils.logout(context, LoginActivity.class);
                                } else {
                                    Utils.showToast(context, response.body().getMsg(), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(context, context.getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(context, context.getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(context, context.getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return guardsAssignedOnRoute.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_guard_name)
        MyTextview tvGuardName;
        @BindView(R.id.iv_unassign_guard)
        Button ivUnassignGuard;
        @BindView(R.id.root_unassign)
        LinearLayout rootUnassign;
        @BindView(R.id.tv_serial_no)
        MyTextview tvSerialNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
