package com.example.refactore2drive.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Account;
import com.example.refactore2drive.models.Discapacity;
import com.example.refactore2drive.models.Disease;
import com.example.refactore2drive.models.Person;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    private MaterialButton cancelButton, nextButton, addDisease;
    private ListView listDiseases;
    private TextInputLayout diseaseInput, usernameInput, passwordInput, nameInput, ageInput;
    private TextInputLayout heightInput, weightInput, typeInput, degreeInput;
    private TextInputEditText diseaseEdit, usernameEdit, passwordEdit, nameEdit, ageEdit;
    private TextInputEditText heightEdit, weightEdit, typeEdit, degreeEdit;
    private ArrayAdapter<String> arrayAdapter;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initialize();
        listeners();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Guardar", "estado guardado");
        outState.putString("username", Objects.requireNonNull(usernameEdit.getText()).toString());
        outState.putString("password", Objects.requireNonNull(passwordEdit.getText()).toString());
        outState.putString("name", Objects.requireNonNull(nameEdit.getText()).toString());
        outState.putString("age", Objects.requireNonNull(ageEdit.getText()).toString());
        outState.putString("height", Objects.requireNonNull(heightEdit.getText()).toString());
        outState.putString("weight", Objects.requireNonNull(weightEdit.getText()).toString());
        outState.putString("type", Objects.requireNonNull(typeEdit.getText()).toString());
        outState.putString("degree", Objects.requireNonNull(degreeEdit.getText()).toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("holaa", "restaurando");
        usernameEdit.setText(savedInstanceState.getString("username"));
        passwordEdit.setText(savedInstanceState.getString("password"));
        nameEdit.setText(savedInstanceState.getString("name"));
        ageEdit.setText(savedInstanceState.getString("age"));
        heightEdit.setText(savedInstanceState.getString("height"));
        weightEdit.setText(savedInstanceState.getString("weight"));
        typeEdit.setText(savedInstanceState.getString("type"));
        degreeEdit.setText(savedInstanceState.getString("degree"));
    }

    private void initialize() {
        //Inicialización de componentes
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
        //Si cancela se finaliza la actividad
        cancelButton.setOnClickListener(view -> finish());
        /*
        Comprueba que la enfermedad no este en blanco en el caso de añadir y que no haya ninguna
        repetida
         */
        addDisease.setOnClickListener(view -> {
            if (!isTextValid(diseaseEdit.getText())) diseaseInput.setError("Campo no puede estar vacio");
            else if (arrayAdapter.getPosition(diseaseEdit.getText().toString()) != -1) diseaseInput.setError("No puedes introducir campos repetidos");
            else {
                diseaseInput.setError(null);
                arrayAdapter.add(diseaseEdit.getText().toString());
                arrayAdapter.notifyDataSetChanged();
            }
        });

        //Manejo de errores
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
            if (!isTextValid(passwordEdit.getText()) || passwordEdit.getText().length() < 8) {
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
            //Si todos los campos estan bien se genera el string con el genero del usuario
            if (correct) {
                String genre = null;
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                if (checkedRadioButtonId == R.id.radio_male) {
                    genre = "Hombre";
                } else if (checkedRadioButtonId == R.id.radio_female) {
                    genre = "Mujer";
                } else if (checkedRadioButtonId == R.id.radio_other) {
                    genre = "Otro";
                } else {
                    Toast.makeText(getApplicationContext(), "Seleccione un genero", Toast.LENGTH_SHORT).show();
                    correct = false;
                }
                if (correct) {
                    //Cuando es correcto se guardan en la base de datos
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    //TODO Que no se pueda repetir el username.
                    try {
                        db.createPerson(new Person(nameEdit.getText().toString(), usernameEdit.getText().toString(), Integer.parseInt(ageEdit.getText().toString()), genre, Integer.parseInt(heightEdit.getText().toString())));
                        Log.d("Persona", db.getPerson(usernameEdit.getText().toString()).toString());
                        db.createAccount(new Account(usernameEdit.getText().toString(), passwordEdit.getText().toString()));
                        db.createDiscapacity(new Discapacity(typeEdit.getText().toString(), usernameEdit.getText().toString(), Integer.parseInt(degreeEdit.getText().toString())));
                        for (int i = 0; i < arrayAdapter.getCount(); i++) {
                            db.createInjury(new Disease(arrayAdapter.getItem(i), usernameEdit.getText().toString()));
                        }
                        Toast.makeText(this, "Registrado con exito", Toast.LENGTH_SHORT).show();
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

    /**
     * Comprueba si un texto es valido
     * @param text texto a comprobar
     * @return true si es valido, false si no lo es
     */
    private boolean isTextValid(@Nullable Editable text) {
        return text != null && text.length() > 0;
    }
}