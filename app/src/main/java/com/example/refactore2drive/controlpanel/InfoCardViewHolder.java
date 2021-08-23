package com.example.refactore2drive.controlpanel;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refactore2drive.R;

public class InfoCardViewHolder extends RecyclerView.ViewHolder {

    public TextView infoTitle;
    public TextView infoValue;
    public ImageView imageView;

    public InfoCardViewHolder(@NonNull View itemView) {
        super(itemView);
        infoTitle = itemView.findViewById(R.id.title_card_info);
        infoValue = itemView.findViewById(R.id.value_card_info);
        imageView = itemView.findViewById(R.id.image_info_card);
    }
}
