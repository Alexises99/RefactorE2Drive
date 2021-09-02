package com.example.refactore2drive.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Account;
import com.example.refactore2drive.models.Discapacity;
import com.example.refactore2drive.models.Disease;
import com.example.refactore2drive.models.Person;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

public class UserConfigActivity extends AppCompatActivity {

    private TextInputLayout passwordInput, nameInput, ageInput, heightInput, typeInput, degreeInput, diseaseInput;
    private TextInputEditText passwordEdit, nameEdit, ageEdit, heightEdit,
    weightEdit, typeEdit, degreeEdit, diseaseEdit;
    private TextView selectObd, selectWear;
    private RadioGroup radioGroup;
    private ArrayAdapter<String> arrayAdapter;
    private ListView listDiseases;
    private MaterialButton cancelButton, saveButton, addDisease;
    private DatabaseHelper db;
    private String username;
    private MaterialRadioButton male, female, other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_config);
        db = new DatabaseHelper(this);
        username = Helper.getUsername(this);
        initialize();
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        listDiseases.setAdapter(arrayAdapter);
        load();
        listeners();
    }

    private void initialize() {
        passwordInput = findViewById(R.id.password_input_config);
        passwordEdit = findViewById(R.id.password_edit_config);
        nameInput = findViewById(R.id.name_input_config);
        nameEdit = findViewById(R.id.name_edit_config);
        ageInput = findViewById(R.id.age_input_config);
        ageEdit = findViewById(R.id.age_edit_config);
        heightInput = findViewById(R.id.height_input_config);
        heightEdit = findViewById(R.id.height_edit_config);
        weightEdit = findViewById(R.id.weight_edit_config);
        typeInput = findViewById(R.id.discapacity_input_config);
        typeEdit = findViewById(R.id.discapacity_edit_config);
        degreeInput = findViewById(R.id.degree_input_config);
        degreeEdit = findViewById(R.id.degree_edit_config);
        diseaseInput = findViewById(R.id.disease_input_config);
        diseaseEdit = findViewById(R.id.disease_edit_config);
        radioGroup = findViewById(R.id.radioGroup_config);
        listDiseases = findViewById(R.id.list_diseases_config);
        cancelButton = findViewById(R.id.cancel_button_config);
        saveButton = findViewById(R.id.next_button_config);
        addDisease = findViewById(R.id.addDisease_config);
        male = findViewById(R.id.radio_male_config);
        female = findViewById(R.id.radio_female_config);
        other = findViewById(R.id.radio_other_config);
        Toolbar toolbar = findViewById(R.id.config_app_bar);
        selectObd = findViewById(R.id.select_obd);
        selectWear = findViewById(R.id.select_wear);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Slidr.attach(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Cargamos todos los datos de la persona y los reflejamos en la ui
     */
    private void load() {
        Person person = db.getPerson(username);
        ArrayList<Disease> diseases =  new ArrayList<>(db.getInjuries(username));
        Account account = db.getAccount(username);
        Discapacity discapacity = db.getDiscapacity(username).get(0);
        passwordEdit.setText(account.getPassword());
        nameEdit.setText(person.getName());
        Log.d("Altura1", person.toString());
        Log.d("Altura", String.valueOf(person.getHeight()));
        ageEdit.setText(String.valueOf(person.getAge()));
        heightEdit.setText(String.valueOf(person.getHeight()));
        weightEdit.setText(String.valueOf(person.getWeight()));
        typeEdit.setText(discapacity.getType());
        Log.d("Grado", ""+discapacity.getDegree());
        degreeEdit.setText(String.valueOf(discapacity.getDegree()));
        switch (person.getGenre()) {
            case "Hombre":
                male.setChecked(true);
            case "Mujer":
                female.setChecked(true);
            case "Otro":
                other.setChecked(true);
        }
        diseases.forEach(disease -> arrayAdapter.add(disease.getName()));
        arrayAdapter.notifyDataSetChanged();
    }

    private void listeners() {
        selectObd.setOnClickListener(
                view -> startActivity(new Intent(this,
                        ChangeObdActivity.class)
                            .putExtra("mode", "obd")));

        selectWear.setOnClickListener(
                view -> startActivity(new Intent(this,
                        ChangeObdActivity.class)
                            .putExtra("mode", "wear")));
        cancelButton.setOnClickListener(view -> finish());

        addDisease.setOnClickListener(view -> {
            if (!isTextValid(diseaseEdit.getText()))
                diseaseInput.setError("Campo no puede estar vacio");
            else if (arrayAdapter.getPosition(diseaseEdit.getText().toString()) != -1)
                diseaseInput.setError("No puedes introducir campos repetidos");
            else {
                diseaseInput.setError(null);
                arrayAdapter.add(diseaseEdit.getText().toString());
                arrayAdapter.notifyDataSetChanged();
            }
        });

        //Manejo de errores
        diseaseEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(diseaseEdit.getText()))
                diseaseInput.setError(null);
            if (arrayAdapter.getPosition(diseaseEdit.getText().toString()) == -1)
                diseaseInput.setError(null);
            return false;
        });

        passwordEdit.setOnKeyListener((view, i, keyEvent) -> {
            if (isTextValid(passwordEdit.getText()) && passwordEdit.getText().length() >= 8)
                passwordInput.setError(null);
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

        saveButton.setOnClickListener(view -> {
            boolean correct = true;

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
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                if (checkedRadioButtonId == R.id.radio_male_config) {
                    genre = "Hombre";
                } else if (checkedRadioButtonId == R.id.radio_female_config) {
                    genre = "Mujer";
                } else if (checkedRadioButtonId == R.id.radio_other_config) {
                    genre = "Otro";
                } else {
                    Toast.makeText(getApplicationContext(), "Seleccione un genero", Toast.LENGTH_SHORT).show();
                    correct = false;
                }
                if (correct) {
                    try {
                        //Guardamos los datos en la base de datos.
                        Account account = db.getAccount(username);
                        account.setPassword(passwordEdit.getText().toString());
                        db.updateAccount(account);
                        Person person = db.getPerson(username);
                        person.setName(nameEdit.getText().toString());
                        person.setAge(Integer.parseInt(ageEdit.getText().toString()));
                        person.setHeight(Integer.parseInt(heightEdit.getText().toString()));
                        if (isTextValid(weightEdit.getText()))
                            person.setWeight(Integer.parseInt(weightEdit.getText().toString()));
                        person.setGenre(genre);
                        db.updatePerson(person);
                        Discapacity discapacity = db.getDiscapacity(username).get(0);
                        discapacity.setType(typeEdit.getText().toString());
                        discapacity.setDegree(Integer.parseInt(degreeEdit.getText().toString()));
                        db.updateDiscapacity(discapacity);
                        db.deleteInjury(username);
                        for (int i = 0; i < arrayAdapter.getCount(); i++) {
                            db.createInjury(new Disease(arrayAdapter.getItem(i), username));
                        }
                        Toast.makeText(this, "Datos guardados con exito", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (CursorIndexOutOfBoundsException e) {
                        Log.e("error al guardar", "los nuevos datos");
                    }
                }
            }
        });
    }

    private boolean isTextValid(@Nullable Editable text) {
        return text != null && text.length() > 0;
    }
}