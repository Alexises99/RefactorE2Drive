package com.example.refactore2drive.sessions;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refactore2drive.R;
import com.example.refactore2drive.models.SessionModel;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SessionFragment extends Fragment {

    private SessionListAdapter sessionListAdapter;
    private ListView listSessions;
    private MaterialButton startSession, endSession;
    private SessionModel sessionModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        sessionListAdapter = new SessionListAdapter();
        listSessions = view.findViewById(R.id.list_sessions);
        listSessions.setAdapter(sessionListAdapter);
        startSession = view.findViewById(R.id.start_session);
        endSession = view.findViewById(R.id.end_session);
        listeners();
        return view;
    }

    private void listeners() {
        startSession.setOnClickListener(view -> {
            startSession.setEnabled(false);
            endSession.setEnabled(true);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String username = preferences.getString("username", "error");
            String iniTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            sessionModel = new SessionModel();
            sessionModel.setName("test"+iniTime+".csv");
            sessionModel.settIni(iniTime);
            sessionModel.setUsername(username);

        });
        endSession.setOnClickListener(view -> {
            startSession.setEnabled(true);
            endSession.setEnabled(false);
            sessionModel.settFin(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            sessionListAdapter.addSession(sessionModel);
            sessionListAdapter.notifyDataSetChanged();
        });
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