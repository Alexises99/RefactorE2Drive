package com.example.refactore2drive.call;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;

public class ContactCardViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView number;
    public ImageView icon;
    public Button button;

    public static int REQUEST_CALL=1;

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
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(contactEntry);
            }
        });
    }




}
