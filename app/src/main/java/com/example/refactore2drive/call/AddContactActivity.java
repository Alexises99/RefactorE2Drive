package com.example.refactore2drive.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;

import com.example.refactore2drive.NavigationHost;
import com.example.refactore2drive.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity implements NavigationHost {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        MaterialButton but = findViewById(R.id.add_contact_but);
        TextInputLayout input1 = findViewById(R.id.input_name);
        TextInputLayout input2 = findViewById(R.id.input_number);
        List<TextInputLayout> textInputLayouts = new ArrayList<>();
        textInputLayouts.add(input1);
        textInputLayouts.add(input2);
        but.setOnClickListener(view -> {
            boolean noErrors = true;
            for (TextInputLayout textInputLayout : textInputLayouts) {
                String editTextString = textInputLayout.getEditText().getText().toString();
                if (editTextString.isEmpty()) {
                    textInputLayout.setError("No puede estar vacio");
                    noErrors = false;
                } else {
                    textInputLayout.setError(null);
                }
            }

            if (noErrors) {
                TextInputEditText e1 = findViewById(R.id.edit_name);
                TextInputEditText e2 = findViewById(R.id.edit_number);
                Bundle bundle = new Bundle();
                Intent intent = new Intent("SEND_CONTACT");
                Contact contact = new Contact(e1.getText().toString(), e2.getText().toString());
                bundle.putSerializable("contact", contact);
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(AddContactActivity.this).sendBroadcast(intent);
                finish();
            }
        });
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public class Contact implements Serializable {
        private final String name, number;
        public Contact(String name, String number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        @NonNull
        @Override
        public String toString() {
            return "Contact{" +
                    "name='" + name + '\'' +
                    ", number='" + number + '\'' +
                    '}';
        }
    }
}