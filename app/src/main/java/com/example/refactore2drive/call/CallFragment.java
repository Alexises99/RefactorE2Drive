package com.example.refactore2drive.call;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.controlpanel.InfoGridItemDecoration;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class CallFragment extends Fragment {

    private ContactCardRecyclerViewAdapter adapter;
    private static final int REQUEST_CALL = 1;
    private  DatabaseHelper db;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        setUpToolbar(view);
        RecyclerView recyclerView = view.findViewById(R.id.call_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        username = Helper.getUsername(getActivity());

        try {
            ArrayList<Contact> contacts = new ArrayList<>(db.getContacts(username));
            adapter = new ContactCardRecyclerViewAdapter(process(contacts), entry -> {
                Log.d("CALL", "HOLAA " + entry.toString());
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                    Log.d("TLF", "Not works");
                } else {
                    Log.d("TLF", "Llamada");
                    String dial = "tel:" + entry.number;
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new InfoGridItemDecoration(8,4));
            FloatingActionButton but = view.findViewById(R.id.float_call);
            but.setOnClickListener(view1 -> getActivity().startActivity(new Intent(getActivity(), AddContactActivity.class)));
        } catch (CursorIndexOutOfBoundsException e) {
            Log.d("No hay contactos", "No hay contactos");
        }
        return view;
    }

    private List<ContactEntry> process(ArrayList<Contact> contacts) {
        List<ContactEntry> contactsEntrys = new ArrayList<>();
        contacts.forEach(contact -> contactsEntrys.add(new ContactEntry(contact.getName(),String.valueOf(contact.getNumber()))));
        return contactsEntrys;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        db = new DatabaseHelper(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SEND_CONTACT");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(myContactReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        db.closeDB();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myContactReceiver);
    }

    private final BroadcastReceiver myContactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("SEND_CONTACT")) {
            Bundle bundle = intent.getExtras();
            AddContactActivity.Contact contact = (AddContactActivity.Contact) bundle.getSerializable("contact");
            Log.d("Contacto", contact.toString());
            db.createContact(new Contact(contact.getName(), Integer.parseInt(contact.getNumber()), username));
            List<ContactEntry> list = adapter.getContactEntryList();
            list.add(new ContactEntry(contact.getName(),contact.getNumber()));
            adapter.notifyDataSetChanged();
        }
        }
    };

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null)
            Log.d("HOla", "hola");
        activity.setSupportActionBar(toolbar);
    }
}