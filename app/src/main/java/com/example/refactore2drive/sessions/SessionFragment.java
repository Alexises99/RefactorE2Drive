package com.example.refactore2drive.sessions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.activities.MoreInfoActivity;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Discapacity;
import com.example.refactore2drive.models.Disease;
import com.example.refactore2drive.models.Person;
import com.example.refactore2drive.models.SessionModel;
import com.example.refactore2drive.obd.BluetoothServiceOBD;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SessionFragment extends Fragment {

    private SessionListAdapter sessionListAdapter;
    private ListView listSessions;
    private MaterialButton startSession, endSession;
    private SessionModel sessionModel;
    private TextInputLayout commentInput;
    private TextInputEditText commentEdit;
    private String username;
    private DatabaseHelper db;
    public static final String ACTION_SESSION_START = "com.example_ACTION_SESSION_START";
    public static final String ACTION_SESSION_END = "com.example_ACTION_SESSION_END";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        setUpToolbar(view);
        sessionListAdapter = new SessionListAdapter();
        listSessions = view.findViewById(R.id.list_sessions);
        listSessions.setAdapter(sessionListAdapter);
        startSession = view.findViewById(R.id.start_session);
        endSession = view.findViewById(R.id.end_session);
        commentEdit = view.findViewById(R.id.comment_edit);
        commentInput = view.findViewById(R.id.comment_input);
        listeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        db = new DatabaseHelper(getActivity());
        username = Helper.getUsername(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        db.closeDB();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    private void listeners() {
        startSession.setOnClickListener(view -> {
            startSession.setEnabled(false);
            endSession.setEnabled(true);
            Intent intent = new Intent(getActivity(), BluetoothServiceOBD.class);
            intent.putExtra("deviceAddress",db.getObd(username).getAddress());
            intent.putExtra("mode", true);
            getActivity().startService(intent);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_SESSION_START));
            String iniTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            sessionModel = new SessionModel();
            sessionModel.setName("test"+iniTime+".csv");
            sessionModel.settIni(iniTime);
            sessionModel.setUsername(username);
            String init = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            String[] comment = new String[1];
            comment[0] = commentEdit.getText().toString();
            Intent intent1 = new Intent(getActivity(), TransferDataService.class);
            intent1.putExtra("name", username+"-"+init+".csv");
            intent1.putExtra("comment", comment);
            intent1.putExtra("data", loadDataPerson());
            getActivity().startService(intent1);
        });
        endSession.setOnClickListener(view -> {
            startSession.setEnabled(true);
            endSession.setEnabled(false);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_SESSION_END));
            sessionModel.settFin(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            sessionListAdapter.addSession(sessionModel);
            sessionListAdapter.notifyDataSetChanged();
            getActivity().stopService(new Intent(getActivity(), TransferDataService.class));
            getActivity().stopService(new Intent(getActivity(), BluetoothServiceOBD.class));

        });
    }

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

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more_info:
                startActivity(new Intent(getActivity(), MoreInfoActivity.class));
                return true;
            default:
                return false;
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