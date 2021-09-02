package com.example.refactore2drive.call;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.activities.DeveloperModeActivity;
import com.example.refactore2drive.activities.MoreInfoActivity;
import com.example.refactore2drive.activities.UserConfigActivity;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Aqui se indica que va a contener un menu, si no esta puesto nunca lo mostrara
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);

        setUpToolbar(view);

        RecyclerView recyclerView = view.findViewById(R.id.call_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5, GridLayoutManager.VERTICAL, false));
        username = Helper.getUsername(getActivity());
        try {
            /*Se recuperan todos los contactos y se comprueba si se tiene el permiso para realizar
             la llamada, si se poseen se genera la actividad que contiene a la llamada.
             */
            ArrayList<Contact> contacts = new ArrayList<>(db.getContacts(username));
            Log.d("Contactos", contacts.toString());
            adapter = new ContactCardRecyclerViewAdapter(process(contacts), entry -> {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CALL_PHONE)) {
                        showMessageOKCancel("Es necesario para llamar a tus contactos", ((dialogInterface, i) -> requestPermission()));
                    } else {
                        requestPermission();
                    }
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
            but.setOnClickListener(view1 -> requireActivity().startActivity(new Intent(getActivity(), AddContactActivity.class)));
        } catch (CursorIndexOutOfBoundsException e) {
            Log.d("No hay contactos", "No hay contactos");
        }
        return view;
    }

    /**
     * Petición de los permisos al usuario
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
    }

    /**
     * Muestra el dialogo para cancelar o aceptar los permisos
     * @param message mensaje a mostrar para explicar los permisos
     * @param okListener listener para ver que hacer cuando el usuario presiona en OK
     */
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(requireActivity())
                .setMessage(message)
                .setPositiveButton("Aceptar", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    /**
     * Procesa todos los contactos para convertilos a ContactEntry para poder ser representados en
     * la UI
     * @param contacts los contactos recuperados de la Base de Datos
     * @return Lista de contactos con los ContactEntry
     */
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
        intentFilter.addAction(AddContactActivity.ACTION_ADD_CONTACT);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myContactReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.closeDB();
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(myContactReceiver);
    }

    /**
     * Recividor de los eventos de añadir contacto
     */
    private final BroadcastReceiver myContactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("ACTION CONTACT", "intent: " + action);
        if (action.equals(AddContactActivity.ACTION_ADD_CONTACT)) {
            //Recupera el contacto y lo crea en la BD y se añade a la lista de estod en la UI
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

    /**
     * Permite poner el actionbar
     * @param view la vista actual
     */
    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.more_info) {
            startActivity(new Intent(getActivity(), MoreInfoActivity.class));
            return true;
        } else if (itemId == R.id.settings) {
            startActivity(new Intent(requireActivity(), UserConfigActivity.class));
            return true;
        } else if (itemId == R.id.developer_mode) {
            startActivity(new Intent(requireActivity(), DeveloperModeActivity.class));
            return true;
        }
        return false;
    }
}