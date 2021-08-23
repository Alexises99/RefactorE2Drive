package com.example.refactore2drive.login;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final TextInputLayout passwordInput = view.findViewById(R.id.password_input);
        final TextInputEditText passwordEdit = view.findViewById(R.id.password_edit);
        MaterialButton nextButton = view.findViewById(R.id.next_button_login);

        nextButton.setOnClickListener(view1 -> {
            if (!isPasswordValid(passwordEdit.getText())) {
                passwordInput.setError("Contraseña con minimo 8 caracteres");
            } else {
                passwordInput.setError(null);
                ((NavigationHost) getActivity()).navigateTo(new SelectOBD(), false);
            }

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