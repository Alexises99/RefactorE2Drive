package com.example.refactore2drive.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.refactore2drive.NavigationHost;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Account;
import com.example.refactore2drive.models.Discapacity;
import com.example.refactore2drive.models.Disease;
import com.example.refactore2drive.models.Person;
import com.example.refactore2drive.obd.SelectOBD;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {
    private MaterialButton cancelButton, nextButton, addDisease;
    private ListView listDiseases;
    private TextInputLayout diseaseInput, usernameInput, passwordInput, nameInput, ageInput;
    private TextInputLayout heightInput, weightInput, typeInput, degreeInput;
    private TextInputEditText diseaseEdit, usernameEdit, passwordEdit, nameEdit, ageEdit;
    private TextInputEditText heightEdit, weightEdit, typeEdit, degreeEdit;
    private ArrayAdapter<String> arrayAdapter;
    private MaterialRadioButton maleButton, femaleButton, otherButton;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initialize();
        listeners();
    }

    private void initialize() {
        cancelButton = findViewById(R.id.cancel_button_reg);
        nextButton = findViewById(R.id.next_button_reg);
        listDiseases = findViewById(R.id.list_diseases_reg);
        addDisease = findViewById(R.id.addDisease);
        diseaseInput = findViewById(R.id.disease_input_reg);
        diseaseEdit = findViewById(R.id.disease_edit_reg);
        usernameInput = findViewById(R.id.username_input_reg);
        usernameEdit = findViewById(R.id.username_edit_reg);
        passwordInput = findViewById(R.id.password_input_reg);
        passwordEdit = findViewById(R.id.password_edit_reg);
        nameInput = findViewById(R.id.name_input_reg);
        nameEdit = findViewById(R.id.name_edit_reg);
        ageInput = findViewById(R.id.age_input_reg);
        ageEdit = findViewById(R.id.age_edit_reg);
        heightInput = findViewById(R.id.height_input_reg);
        heightEdit = findViewById(R.id.height_edit_reg);
        weightInput = findViewById(R.id.weight_input_reg);
        weightEdit = findViewById(R.id.weight_edit_reg);
        typeInput = findViewById(R.id.discapacity_input_reg);
        typeEdit = findViewById(R.id.discapacity_edit_reg);
        degreeInput = findViewById(R.id.degree_input_reg);
        degreeEdit = findViewById(R.id.degree_edit_reg);
        radioGroup = findViewById(R.id.radioGroup);
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        listDiseases.setAdapter(arrayAdapter);
    }

    private void listeners() {
        cancelButton.setOnClickListener(view -> finish());
        addDisease.setOnClickListener(view -> {
            if (!isTextValid(diseaseEdit.getText())) diseaseInput.setError("Campo no puede estar vacio");
            else if (arrayAdapter.getPosition(diseaseEdit.getText().toString()) != -1) diseaseInput.setError("No puedes introducir campos repetidos");
            else {
                arrayAdapter.add(diseaseEdit.getText().toString());
                arrayAdapter.notifyDataSetChanged();
            }
        });
        diseaseEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(diseaseEdit.getText())) diseaseInput.setError(null);
            if (arrayAdapter.getPosition(diseaseEdit.getText().toString()) == -1) diseaseInput.setError(null);
            return false;
        });

        usernameEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(usernameEdit.getText())) usernameInput.setError(null);
            return false;
        });
        passwordEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(passwordEdit.getText()) && passwordEdit.getText().length() >= 8) passwordInput.setError(null);
            return false;
        });
        nameEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(nameEdit.getText())) nameInput.setError(null);
            return false;
        });
        ageEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(ageEdit.getText())) ageInput.setError(null);
            return false;
        });
        heightEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(heightEdit.getText())) heightInput.setError(null);
            return false;
        });
        typeEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(typeEdit.getText())) typeInput.setError(null);
            return false;
        });
        degreeEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(degreeEdit.getText())) degreeInput.setError(null);
            return false;
        });

        listDiseases.setOnItemClickListener((adapterView, view, i, l) -> {
            arrayAdapter.remove(arrayAdapter.getItem(i));
            arrayAdapter.notifyDataSetChanged();
        });

        nextButton.setOnClickListener(view -> {
            boolean correct = true;
            if (!isTextValid(usernameEdit.getText())) {
                usernameInput.setError("Campo requerido");
                correct = false;
            }
            if (!isTextValid(passwordEdit.getText()) && passwordEdit.getText().length() < 8) {
                passwordInput.setError("Campo requerido con longuitud minima de 8");
                correct = false;
            }
            if (!isTextValid(nameEdit.getText())) {
                nameInput.setError("Campo requerido");
                correct = false;
            }
            if (!isTextValid(ageEdit.getText())) {
                ageInput.setError("Campo requerido");
                correct = false;
            }
            if (!isTextValid(heightEdit.getText())) {
                heightInput.setError("Campo requerido");
                correct = false;
            }
            if (!isTextValid(typeEdit.getText())) {
                typeInput.setError("Campo requerido");
                correct = false;
            }
            if (!isTextValid(degreeEdit.getText())) {
                degreeInput.setError("Campo requerido");
                correct = false;
            }

            if (correct) {
                String genre = null;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_male:
                        genre = "Hombre";
                        break;
                    case R.id.radio_female:
                        genre = "Mujer";
                        break;
                    case R.id.radio_other:
                        genre = "Otro";
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Seleccione un genero", Toast.LENGTH_SHORT).show();
                        correct = false;
                }
                if (correct) {
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    //TODO Que no se pueda repetir el username.
                    /*db.clearDB();
                    db.initDB();*/
                    try {
                        db.createPerson(new Person(nameEdit.getText().toString(), usernameEdit.getText().toString(), Integer.parseInt(ageEdit.getText().toString()), genre, Float.parseFloat(heightEdit.getText().toString())));
                        db.createAccount(new Account(usernameEdit.getText().toString(), passwordEdit.getText().toString()));
                        db.createDiscapacity(new Discapacity(typeEdit.getText().toString(), usernameEdit.getText().toString(), Integer.parseInt(degreeEdit.getText().toString())));
                        for (int i = 0; i < arrayAdapter.getCount(); i++) {
                            db.createInjury(new Disease(arrayAdapter.getItem(i), usernameEdit.getText().toString()));
                        }
                        finish();
                    } catch (Exception e) {
                        Log.e(SignupActivity.class.getName(), "Error al guardar datos en bd");
                    } finally {
                        db.closeDB();
                    }

                }
            }
        });
    }

    private boolean isTextValid(@Nullable Editable text) {
        return text != null && text.length() > 0;
    }
}