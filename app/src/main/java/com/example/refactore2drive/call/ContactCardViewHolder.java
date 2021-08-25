package com.example.refactore2drive.call;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.example.refactore2drive.R;

public class ContactCardViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView number;
    public ImageView icon;
    public Button button;


    public ContactCardViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.title_card_call);
        number = itemView.findViewById(R.id.value_card_call);
        icon = itemView.findViewById(R.id.image_call_card);
        button = itemView.findViewById(R.id.but_call);
    }

    public void bind(final ContactEntry contactEntry, final OnItemClickListener listener) {
        name.setText(contactEntry.name);
        number.setText(contactEntry.number);
        icon.setImageResource(contactEntry.resourceId);
        itemView.setOnClickListener(view -> listener.onItemClick(contactEntry));
    }




}
