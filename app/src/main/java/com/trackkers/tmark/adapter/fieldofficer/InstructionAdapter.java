package com.trackkers.tmark.adapter.fieldofficer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstructionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    Context context;
    List<ApiResponse.SiteInstructionBean> siteInstruction = new ArrayList<>();


    public InstructionAdapter(Context context, List<ApiResponse.SiteInstructionBean> siteInstruction) {
        this.context = context;
        this.siteInstruction = siteInstruction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_instructions, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder holder1 = (MyViewHolder) holder;

        holder1.tvInstructions.setText((position+1)+":- "+siteInstruction.get(position).getInstruction());

    }

    @Override
    public int getItemCount() {
        return siteInstruction.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_instructions)
        MyTextview tvInstructions;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

}
