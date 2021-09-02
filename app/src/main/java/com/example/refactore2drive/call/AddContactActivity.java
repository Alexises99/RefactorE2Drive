package com.example.refactore2drive.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;

import com.example.refactore2drive.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddContactActivity extends AppCompatActivity {

    public static final String ACTION_ADD_CONTACT = "com.example_ACTION_ADD_CONTACT";

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
                String editTextString = Objects.requireNonNull(textInputLayout.getEditText()).getText().toString();
                if (editTextString.isEmpty()) {
                    textInputLayout.setError("No puede estar vacio");
                    noErrors = false;
                } else {
                    textInputLayout.setError(null);
                }
            }
            /*
             * Se envia el contacto al fragmento para que el lo guarde y muestre por pantalla
             */
            if (noErrors) {
                TextInputEditText e1 = findViewById(R.id.edit_name);
                TextInputEditText e2 = findViewById(R.id.edit_number);
                Bundle bundle = new Bundle();
                Intent intent = new Intent(ACTION_ADD_CONTACT);
                Contact contact = new Contact(Objects.requireNonNull(e1.getText()).toString(), Objects.requireNonNull(e2.getText()).toString());
                bundle.putSerializable("contact", contact);
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(AddContactActivity.this).sendBroadcast(intent);
                finish();
            }
        });
    }

    /**
     * Implementación de Contact de manera serializable para poder ser enviado al fragmento
     */

    public static class Contact implements Serializable {
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