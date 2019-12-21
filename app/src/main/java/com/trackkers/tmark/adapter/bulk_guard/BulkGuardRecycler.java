package com.trackkers.tmark.adapter.bulk_guard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.views.activity.bulk_guard.BulkGuardCheckin;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BulkGuardRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails = new ArrayList<>();

    public BulkGuardRecycler(Context context, List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails) {
        this.context = context;
        this.employeeDetails = employeeDetails;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_employee_details, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.tvEmployeeName.setText(PrefData.getInstance().getResources().getString(R.string.employee_name) + " " + employeeDetails.get(i).getEmployeeName());
        holder.tvEmployeeId.setText(PrefData.getInstance().getResources().getString(R.string.employee_code) + " " + employeeDetails.get(i).getEmpCode());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefData.writeStringPref(PrefData.employee_name, employeeDetails.get(i).getEmployeeName());
                PrefData.writeStringPref(PrefData.employee_id, String.valueOf(employeeDetails.get(i).getEmployeeId()));
                PrefData.writeStringPref(PrefData.employee_code, employeeDetails.get(i).getEmpCode());
                context.startActivity(new Intent(context, BulkGuardCheckin.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return employeeDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_employee_name)
        MyTextview tvEmployeeName;
        @BindView(R.id.tv_employee_id)
        MyTextview tvEmployeeId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
