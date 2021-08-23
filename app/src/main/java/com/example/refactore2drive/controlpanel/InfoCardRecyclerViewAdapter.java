package com.example.refactore2drive.controlpanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refactore2drive.R;

import java.util.List;

public class InfoCardRecyclerViewAdapter extends RecyclerView.Adapter<InfoCardViewHolder>{
    private List<InfoEntry> infoEntryList;

    InfoCardRecyclerViewAdapter(List<InfoEntry> infoEntryList) {
        this.infoEntryList = infoEntryList;
    }

    @NonNull
    @Override
    public InfoCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_card, parent, false);
        return new InfoCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoCardViewHolder holder, int position) {
        if (infoEntryList != null && position < infoEntryList.size()) {
            InfoEntry info = infoEntryList.get(position);
            holder.infoTitle.setText(info.title);
            holder.infoValue.setText(info.value);
            holder.imageView.setImageResource(info.resourceId);
        }
    }

    @Override
    public int getItemCount() {
        return infoEntryList.size();
    }
}
