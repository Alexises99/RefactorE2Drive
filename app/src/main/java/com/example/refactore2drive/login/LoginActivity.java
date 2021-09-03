package com.example.refactore2drive.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.widget.Toast;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Account;
import com.example.refactore2drive.obd.SelectObdActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameInput;
    private TextInputEditText usernameEdit;
    private TextInputLayout passwordInput;
    private TextInputEditText passwordEdit;
    private MaterialButton nextButton;
    private MaterialButton regButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Comprobación de permisos
        checkPermissions();

        initialize();
        listeners();
    }

    private void initialize() {
        usernameInput = findViewById(R.id.username_input);
        usernameEdit = findViewById(R.id.username_edit);
        passwordInput = findViewById(R.id.password_input);
        passwordEdit = findViewById(R.id.password_edit);
        nextButton = findViewById(R.id.next_button_login);
        regButton = findViewById(R.id.cancel_button_login);
    }

    private void listeners() {
        //Listener para escuchar cuando un usuario quiere registrarse
        regButton.setOnClickListener(view1 -> startActivity(new Intent(this, SignupActivity.class)));

        //Listener que comprueba si el usuario es valido y se logea
        nextButton.setOnClickListener(view1 -> {
            //Comprobación de campos
            if (! (Objects.requireNonNull(usernameEdit.getText()).length() > 0 && usernameEdit.getText() != null)) {
                usernameInput.setError("Campo requerido");
            }
            if (!isPasswordValid(passwordEdit.getText())) {
                passwordInput.setError("Contraseña con minimo 8 caracteres");
            } else {
                passwordInput.setError(null);
                DatabaseHelper db = new DatabaseHelper(this);
                try{
                    /*
                    Recuperamos la cuenta a traves del username de la Base de datos, luego comprobamos que la contraseña
                    introducida por el usuario coincida con la que tiene esa cuenta asociada.
                     */
                    Account account = db.getAccount(usernameEdit.getText().toString());
                    if (!account.getPassword().equals(passwordEdit.getText().toString())) {
                        //Informamos al usuario en caso de ser incorrecto
                        Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //Guardamos el username para poder recurrir a el posteriormente en la app
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("username", usernameEdit.getText().toString());
                        editor.apply();
                        startActivity(new Intent(this, SelectObdActivity.class));
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
                finally {
                    db.closeDB();
                }
            }
        });
        //Permite eliminar el error una vez corregido
        usernameEdit.setOnKeyListener((view1, i, keyEvent) -> {
            if (Objects.requireNonNull(usernameEdit.getText()).length() > 0 && usernameEdit.getText() != null) usernameInput.setError(null);
            return false;
        });
        passwordEdit.setOnKeyListener((view1, i, keyEvent) -> {
            if (isPasswordValid(passwordEdit.getText())) {
                passwordInput.setError(null);
            }
            return false;
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Helper.showMessageOKCancel(
                        this,
                        "La ubicación es necesaria para escanear dispositivos bluetooth",
                        ((dialogInterface, i) -> Helper.requestPermission(this, Helper.REQUEST_LOCATION)));
            } else {
                Helper.requestPermission(this, Helper.REQUEST_LOCATION);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Helper.showMessageOKCancel(
                        this,
                        "La camara es necesaria para detectar el cansancio",
                        ((dialogInterface, i) -> Helper.requestPermission(this, Helper.REQUEST_CAMERA)));
            } else {
                Helper.requestPermission(this, Helper.REQUEST_CAMERA);
            }
        }
    }

    /**
     * Comprueba longuitud de la contraseña y que no este vacio
     * @param text contraseña
     * @return boolean indicando si es correcto o no
     */
    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }
}
