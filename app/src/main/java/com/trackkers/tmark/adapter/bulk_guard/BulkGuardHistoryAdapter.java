package com.trackkers.tmark.adapter.bulk_guard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BulkGuardHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ApiResponse.DataBean> bulkGuardHistoryData = new ArrayList<>();

    public BulkGuardHistoryAdapter(Context context, List<ApiResponse.DataBean> bulkGuardHistoryData) {
        this.context = context;
        this.bulkGuardHistoryData = bulkGuardHistoryData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_bulk_guard_history, viewGroup, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.tvCheckinTime.setText(PrefData.getInstance().getResources().getString(R.string.checkin_time) + " " + bulkGuardHistoryData.get(i).getCheckInTime());
        holder.tvCheckoutTime.setText(PrefData.getInstance().getResources().getString(R.string.checkout_time) + " " + bulkGuardHistoryData.get(i).getCheckOutTime());
        holder.tvCheckinAddress.setText(PrefData.getInstance().getResources().getString(R.string.checkin_address) + " " + bulkGuardHistoryData.get(i).getCheckInAddress());
        holder.tvCheckoutAddress.setText(PrefData.getInstance().getResources().getString(R.string.checkout_address) + " " + bulkGuardHistoryData.get(i).getCheckOutAddress());

        Picasso.get().load(Utils.BASE_IMAGE + bulkGuardHistoryData.get(i).getSelfieImg()).placeholder(R.drawable.progress_animation).into(holder.ivGuardImage);
    }

    @Override
    public int getItemCount() {
        return bulkGuardHistoryData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_checkin_time)
        MyTextview tvCheckinTime;
        @BindView(R.id.tv_checkout_time)
        MyTextview tvCheckoutTime;
        @BindView(R.id.tv_checkin_address)
        MyTextview tvCheckinAddress;
        @BindView(R.id.tv_checkout_address)
        MyTextview tvCheckoutAddress;
        @BindView(R.id.iv_guard_image)
        RoundedImageView ivGuardImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
