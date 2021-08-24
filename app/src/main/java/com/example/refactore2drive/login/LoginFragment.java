package com.example.refactore2drive.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Account;
import com.example.refactore2drive.obd.SelectOBD;
import com.example.refactore2drive.NavigationHost;
import com.example.refactore2drive.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final TextInputLayout usernameInput = view.findViewById(R.id.username_input);
        final TextInputEditText usernameEdit = view.findViewById(R.id.username_edit);
        final TextInputLayout passwordInput = view.findViewById(R.id.password_input);
        final TextInputEditText passwordEdit = view.findViewById(R.id.password_edit);
        MaterialButton nextButton = view.findViewById(R.id.next_button_login);
        MaterialButton regButton = view.findViewById(R.id.cancel_button_login);

        regButton.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), SignupActivity.class));
        });

        nextButton.setOnClickListener(view1 -> {
            if (! (usernameEdit.getText().length() > 0 && usernameEdit.getText() != null)) {
                usernameInput.setError("Campo requerido");
            }
            if (!isPasswordValid(passwordEdit.getText())) {
                passwordInput.setError("Contraseña con minimo 8 caracteres");
            } else {
                passwordInput.setError(null);
                DatabaseHelper db = new DatabaseHelper(getActivity());
                try{
                    Account account = db.getAccount(usernameEdit.getText().toString());
                    if (!account.getPassword().equals(passwordEdit.getText().toString())) {
                        Toast.makeText(getActivity(), "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("username", usernameEdit.getText().toString());
                        editor.commit();
                        ((NavigationHost) getActivity()).navigateTo(new SelectOBD(), false);
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    Toast.makeText(getActivity(), "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
                finally {
                    db.closeDB();
                }
            }
        });
        usernameEdit.setOnKeyListener((view1, i, keyEvent) -> {
            if (usernameEdit.getText().length() > 0 && usernameEdit.getText() != null) usernameInput.setError(null);
            return false;
        });
        passwordEdit.setOnKeyListener((view1, i, keyEvent) -> {
            if (isPasswordValid(passwordEdit.getText())) {
                passwordInput.setError(null);
            }
            return false;
        });
        return view;
    }

    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }
}