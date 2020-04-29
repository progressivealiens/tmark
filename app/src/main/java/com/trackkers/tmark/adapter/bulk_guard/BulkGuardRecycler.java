package com.trackkers.tmark.adapter.bulk_guard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.bulk_guard.BulkGuardCheckin;
import com.trackkers.tmark.views.activity.bulk_guard.BulkGuardMainActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.trackkers.tmark.views.activity.bulk_guard.BulkGuardMainActivity.REQUEST_CODE_FOR_BACK_CAMERA;
import static com.trackkers.tmark.views.activity.bulk_guard.BulkGuardMainActivity.currentLatitude;
import static com.trackkers.tmark.views.activity.bulk_guard.BulkGuardMainActivity.currentLongitude;

public class BulkGuardRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity context;
    boolean isDataSearched;
    private List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails = new ArrayList<>();
    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    public BulkGuardRecycler(AppCompatActivity context, List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails,boolean isDataSearched) {
        this.context = context;
        this.employeeDetails = employeeDetails;
        prefData = new PrefData(context);
        apiInterface = ApiClient.getClient(context).create(ApiInterface.class);
        progressView = new ProgressView(context);
        this.isDataSearched=isDataSearched;
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
        holder.tvSerialNo.setText(""+(i + 1));
        holder.tvEmployeeName.setText(PrefData.getInstance().getResources().getString(R.string.employee_name) + "\n" + employeeDetails.get(i).getEmployeeName());
        holder.tvEmployeeId.setText(PrefData.getInstance().getResources().getString(R.string.emp_code) + "\n" + employeeDetails.get(i).getEmpCode());


        Log.e("employeeName",employeeDetails.get(i).getEmployeeName());
        Log.e("isCheckedIn",employeeDetails.get(i).isIsCheckedIn()+"");

        if (employeeDetails.get(i).isIsCheckedIn()){
            holder.tvSerialNo.setBackground(context.getResources().getDrawable(R.drawable.normal_red_left_round));
            holder.btnCheckin.setText(context.getResources().getString(R.string.checkout));
            holder.btnCheckin.setBackground(context.getResources().getDrawable(R.drawable.selector_button_checkout));
        }else{
            holder.btnCheckin.setText(context.getResources().getString(R.string.checkin));
            holder.tvSerialNo.setBackground(context.getResources().getDrawable(R.drawable.normal_green_left_round));
            holder.btnCheckin.setBackground(context.getResources().getDrawable(R.drawable.selector_button_checkin));
        }

        holder.btnCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isDataSearched){
                    PrefData.writeStringPref(PrefData.employee_id, String.valueOf(employeeDetails.get(i).getEmployeeId()));
                    PrefData.writeStringPref(PrefData.e_register_checkin_position,"");
                }else{
                    PrefData.writeStringPref(PrefData.employee_name,employeeDetails.get(i).getEmployeeName());
                    PrefData.writeStringPref(PrefData.employee_id, String.valueOf(employeeDetails.get(i).getEmployeeId()));
                    PrefData.writeStringPref(PrefData.employee_code,employeeDetails.get(i).getEmpCode());
                    PrefData.writeStringPref(PrefData.e_register_checkin_position,String.valueOf(i));
                }

                if (holder.btnCheckin.getText().toString().equalsIgnoreCase(context.getResources().getString(R.string.checkin))){
                    checkIn();
                }else{
                    connectApiToCheckOutGuard();
                }
            }
        });

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

    private void checkIn() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFileCheckin = null;
        try {
            pictureFileCheckin = getPictureFileCheckin();
            if (pictureFileCheckin != null) {
                Uri photoURI = FileProvider.getUriForFile(context, "com.trackkers.tmark.fileprovider", pictureFileCheckin);
                cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                context.startActivityForResult(cameraIntent, REQUEST_CODE_FOR_BACK_CAMERA);
            }
        } catch (IOException ex) {
            Utils.showToast(context, "Photo file can't be created, please try again", Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
        }
    }

    private File getPictureFileCheckin() throws IOException {
        String timeStamp = Utils.currentTimeStamp();
        File storageDir = context.getExternalFilesDir(null);
        File image = File.createTempFile(timeStamp, ".png", storageDir);
        BulkGuardMainActivity.pictureFilePathCheckin = image.getAbsolutePath();
        return image;
    }

    private void connectApiToCheckOutGuard() {

        if (CheckNetworkConnection.isConnection1(context, true)) {
            progressView.showLoader();

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                ((BulkGuardMainActivity)context).getLastLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    String batterPercentage = String.valueOf(Utils.getBatteryPercentage(context));

                    Call<ApiResponseOperations> call = apiInterface.guardMultipleCheckOut(
                            PrefData.readStringPref(PrefData.employee_id),
                            PrefData.readStringPref(PrefData.route_id),
                            currentLatitude + "",
                            currentLongitude + "",
                            batterPercentage);

                    call.enqueue(new Callback<ApiResponseOperations>() {
                        @Override
                        public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                            progressView.hideLoader();

                            try {
                                if (response.body() != null && response.body().getStatus() != null) {
                                    if (response.body().getStatus().equalsIgnoreCase("success")) {

                                        Utils.showToast(context, context.getResources().getString(R.string.checkout_sucessfull), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorLightGreen), context.getResources().getColor(R.color.colorWhite));
                                        ((BulkGuardMainActivity)context).onRefresh();
                                        if (!((BulkGuardMainActivity)context).etSearchName.getText().toString().equalsIgnoreCase("")){
                                            ((BulkGuardMainActivity)context).etSearchName.setText("");
                                        }
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
                                    Utils.showToast(context, context.getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink),context.getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 404) {
                                    Utils.showToast(context, context.getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, context.getResources().getColor(R.color.colorPink), context.getResources().getColor(R.color.colorWhite));
                                }
                                e.printStackTrace();

                            }

                        }

                        @Override
                        public void onFailure(Call<ApiResponseOperations> call, Throwable t) {
                            progressView.hideLoader();
                            t.printStackTrace();
                        }
                    });
                }
            }, 200);
        }
    }

    @Override
    public int getItemCount() {
        return employeeDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_serial_no)
        MyTextview tvSerialNo;
        @BindView(R.id.tv_employee_name)
        MyTextview tvEmployeeName;
        @BindView(R.id.tv_employee_id)
        MyTextview tvEmployeeId;
        @BindView(R.id.btn_checkin)
        MyButton btnCheckin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
