package com.example.refactore2drive.call;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refactore2drive.R;
import com.example.refactore2drive.controlpanel.InfoCardViewHolder;
import com.example.refactore2drive.controlpanel.InfoEntry;

import java.util.List;

public class ContactCardRecyclerViewAdapter extends RecyclerView.Adapter<ContactCardViewHolder> {
    public List<ContactEntry> contactEntryList;
    private final OnItemClickListener listener;

    ContactCardRecyclerViewAdapter(List<ContactEntry> contactEntryList, OnItemClickListener listener) {
        this.contactEntryList = contactEntryList;
        this.listener =  listener;
    }

    @NonNull
    @Override
    public ContactCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card, parent, false);
        return new ContactCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactCardViewHolder holder, int position) {
        //if (contactEntryList != null && position < contactEntryList.size()) {
            holder.bind(contactEntryList.get(position), listener);
       // }
    }

    @Override
    public int getItemCount() {
        return contactEntryList.size();
    }

    public List<ContactEntry> getContactEntryList() {
        return contactEntryList;
    }
}
