package com.example.refactore2drive.sessions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;

import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Discapacity;
import com.example.refactore2drive.models.Disease;
import com.example.refactore2drive.models.Person;
import com.example.refactore2drive.models.SessionModel;
import com.example.refactore2drive.obd.BluetoothServiceOBD;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class SessionFragment extends Fragment {

    private SessionListAdapter sessionListAdapter;
    private ListView listSessions;
    private MaterialButton startSession, endSession;
    private TextInputEditText commentEdit;
    private String username;
    private DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        //setUpToolbar(view);
        sessionListAdapter = new SessionListAdapter();
        listSessions = view.findViewById(R.id.list_sessions);
        listSessions.setAdapter(sessionListAdapter);
        startSession = view.findViewById(R.id.start_session);
        endSession = view.findViewById(R.id.end_session);
        commentEdit = view.findViewById(R.id.comment_edit);
        listeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        db = new DatabaseHelper(getActivity());
        username = Helper.getUsername(getActivity());
        //Recogemos todas las sesiones y las ponemos
        ArrayList<SessionModel> arrayList = new ArrayList<>(db.getSessions(username));
        arrayList.forEach(sessionModel1 -> sessionListAdapter.addSession(sessionModel1));
        sessionListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.closeDB();
        sessionListAdapter.clear();
    }

    private void listeners() {
        startSession.setOnClickListener(view -> {
            //Comprobamos permisos de escritura
            if (ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showMessageOKCancel("Escribir es necesario para guardar el csv en su dispositivo",
                            ((dialogInterface, i) -> requestPermission()));
                } else {
                    requestPermission();
                }
            }
            //Deshabilitamos botones
            startSession.setEnabled(false);
            endSession.setEnabled(true);
            Intent intent = new Intent(getActivity(), BluetoothServiceOBD.class);
            intent.putExtra("deviceAddress",db.getObd(username).getAddress());
            intent.putExtra("mode", true);
            requireActivity().startService(intent);
            //Configuramos la sesión
            MainActivity.sessionStarted = true;
            String iniTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            MainActivity.sessionModel = new SessionModel();
            MainActivity.sessionModel.setName(username+iniTime+".csv");
            MainActivity.sessionModel.settIni(iniTime);
            MainActivity.sessionModel.setUsername(username);
            String init = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            String[] comment = new String[1];
            comment[0] = Objects.requireNonNull(commentEdit.getText()).toString();
            //Lanzamos el servicio de transferencia con los datos adecuados
            Intent intent1 = new Intent(getActivity(), TransferDataService.class);
            intent1.putExtra("name", username+"-"+init);
            intent1.putExtra("comment", comment);
            intent1.putExtra("data", loadDataPerson());
            requireActivity().startService(intent1);
        });
        endSession.setOnClickListener(view -> {
            startSession.setEnabled(true);
            endSession.setEnabled(false);
            MainActivity.sessionModel.settFin(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            MainActivity.sessionStarted = false;
            db.createSession(MainActivity.sessionModel);
            sessionListAdapter.addSession(MainActivity.sessionModel);
            sessionListAdapter.notifyDataSetChanged();
            MainActivity.sessionModel = null;
            //Detenemos ambos servicios para ahorrar recursos
            requireActivity().stopService(new Intent(getActivity(), TransferDataService.class));
            requireActivity().stopService(new Intent(getActivity(), BluetoothServiceOBD.class));
        });
        listSessions.setOnItemClickListener((adapterView, view, i, l) -> {
            SessionModel session = sessionListAdapter.getSession(i);
            createAlertDialog(session, i);
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.REQUET_STORAGE);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(requireActivity())
                .setMessage(message)
                .setPositiveButton("Aceptar", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }


    private void createAlertDialog(SessionModel session, int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("¿Desea borrar la sesión?").setTitle("Borrar sesión");
// Add the buttons
        builder.setPositiveButton("Ok", (dialog, id) -> {
            // User clicked OK button
            db.deleteSession(session.getId());
            sessionListAdapter.delete(i);
            sessionListAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog
            Toast.makeText(getActivity(), "No has borrado nada", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Carga los datos de la persona para ser escritos en el csv
     * @return todos los datos de la persona en un String de una unica posición
     */
    private String[] loadDataPerson() {
        try {
            Person person = db.getPerson(username);
            Discapacity discapacity = db.getDiscapacity(username).get(0);
            ArrayList<Disease> diseases = new ArrayList<>(db.getInjuries(username));
            StringBuilder list = new StringBuilder();
            list.append("Nombre: ").append(person.getName()).append("\n");
            list.append("Edad: ").append(person.getAge()).append("\n");
            list.append("Genero: ").append(person.getGenre()).append("\n");
            list.append("Altura: ").append(person.getHeight()).append("\n");
            list.append("Peso: ").append(person.getWeight()).append("\n");
            list.append("Tipo discapcidad: ").append(discapacity.getType()).append("\n");
            list.append("Grado discapacidad: ").append(discapacity.getDegree()).append("\n");
            for (Disease disease : diseases) list.append("Enfermedad: ").append(disease.getName()).append("\n");
            String[] res = new String[1];
            res[0] = list.toString();
            return res;
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }
    }

    private class SessionListAdapter extends BaseAdapter {
        private final ArrayList<SessionModel> mSessions;
        private final LayoutInflater mInflator;

        public SessionListAdapter() {
            super();
            mSessions = new ArrayList<>();
            mInflator = getLayoutInflater();
        }

        public void addSession(SessionModel session) {
            if(!mSessions.contains(session)) {
                mSessions.add(session);
            }
        }

        public SessionModel getSession(int position) {
            return mSessions.get(position);
        }
        public void delete(int position) { mSessions.remove(position); }

        public void clear() {
            mSessions.clear();
        }
        @Override
        public int getCount() {
            return mSessions.size();
        }
        @Override
        public Object getItem(int i) {
            return mSessions.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @SuppressLint("InflateParams")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.list_item_session, null);
                viewHolder = new ViewHolder();
                viewHolder.nameSession = view.findViewById(R.id.name_session);
                viewHolder.startHour = view.findViewById(R.id.start_hour);
                viewHolder.endHour = view.findViewById(R.id.end_hour);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            SessionModel session = mSessions.get(i);
            final String sessionName = session.getName();
            if (sessionName != null && sessionName.length() > 0)
                viewHolder.nameSession.setText(sessionName);
            else
                viewHolder.nameSession.setText("Sesion desconocida");
            viewHolder.startHour.setText("Inicio: " + session.gettIni());
            viewHolder.endHour.setText("Fin: " + session.gettFin());
            return view;
        }
    }

    static class ViewHolder {
        TextView nameSession;
        TextView startHour;
        TextView endHour;
    }
}