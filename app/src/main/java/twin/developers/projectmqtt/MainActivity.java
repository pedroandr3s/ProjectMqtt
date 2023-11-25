package twin.developers.projectmqtt;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Mqtt mqttManager;
    private TextView texto;
    private RadioGroup radioGroup;
    private Button btnEnviar;
    private Button pregunta;
    private Button agregar;
    private Button quitar;
    private int cantidadOpciones = 1;
    private int opcionVisible = 1; // Índice de la opción actualmente visible
    private int opcionSeleccionada = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pregunta = findViewById(R.id.pregunta);
        texto = findViewById(R.id.textView5);
        radioGroup = findViewById(R.id.radioGroup);
        btnEnviar = findViewById(R.id.btnPublish);
        agregar = findViewById(R.id.agregar);
        quitar = findViewById(R.id.quitar);

        mqttManager = new Mqtt(getApplicationContext());
        mqttManager.connectToMqttBroker();

        // Inicialmente, solo mostrar la opción 1
        mostrarOpcion();

        // Definir el OnClickListener para el botón pregunta
        pregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogo();
            }
        });

        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cantidadOpciones < 5) {
                    cantidadOpciones++;
                    opcionVisible = cantidadOpciones;
                    mostrarOpciones();
                }
            }
        });


        // Definir el OnClickListener para el botón quitar
        quitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cantidadOpciones > 1) {
                    cantidadOpciones--;
                    mostrarOpciones();
                }
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texto.setText("");

                RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());

                if (radioButton != null) {
                    // Deshabilitar todos los RadioButtons excepto el seleccionado
                    for (int i = 1; i <= cantidadOpciones; i++) {
                        int radioButtonId = getResources().getIdentifier("radioOption" + i, "id", getPackageName());
                        int btnEditId = getResources().getIdentifier("btnEdit" + i, "id", getPackageName());

                        RadioButton currentRadioButton = findViewById(radioButtonId);
                        Button currentBtnEdit = findViewById(btnEditId);

                        currentRadioButton.setEnabled(false);
                        currentBtnEdit.setEnabled(false);
                    }

                    // Habilitar el RadioButton y su botón de edición seleccionados
                    radioButton.setEnabled(true);

                    int btnEditId = getResources().getIdentifier("btnEdit" + opcionSeleccionada, "id", getPackageName());
                    Button btnEdit = findViewById(btnEditId);
                    btnEdit.setEnabled(true);

                    opcionSeleccionada = Integer.parseInt(radioButton.getTag().toString());
                    opcionVisible = opcionSeleccionada;

                    // Se eliminó la línea que no se estaba utilizando
                    // String textoRadioSeleccionado = radioButton.getText().toString();

                    mqttManager.publishMessage("encuesta/01", obtenerRespuestaSeleccionada());
                } else {
                    Toast.makeText(MainActivity.this, "Selecciona una opción", Toast.LENGTH_SHORT).show();
                }
            }
        });







        // Asociar el OnClickListener para los botones "Editar"
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            Button btnEdit = findViewById(getResources().getIdentifier("btnEdit" + i, "id", getPackageName()));
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoEditar(index);
                }
            });
            mostrarOpciones();
        }
    }

    private String obtenerRespuestaSeleccionada() {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        if (radioButtonId != -1) {
            RadioButton radioButton = findViewById(radioButtonId);
            return radioButton.getText().toString();
        }
        return null;
    }

    private void mostrarOpcion() {
        for (int i = 1; i <= 5; i++) {
            int radioButtonId = getResources().getIdentifier("radioOption" + i, "id", getPackageName());
            RadioButton radioButton = findViewById(radioButtonId);
            Button btnEdit = findViewById(getResources().getIdentifier("btnEdit" + i, "id", getPackageName()));
            if (i == opcionVisible) {
                radioButton.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                radioButton.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }
    }

    private void mostrarDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_pregunta, null);
        final EditText input = view.findViewById(R.id.editTextRespuesta);
        builder.setView(view);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String respuesta = input.getText().toString();
                if (!respuesta.isEmpty()) {
                    pregunta.setText(respuesta);
                } else {
                    Toast.makeText(MainActivity.this, "La respuesta no puede estar vacía", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void mostrarDialogoEditar(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_pregunta, null);
        final EditText input = view.findViewById(R.id.editTextRespuesta);
        builder.setView(view);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String respuesta = input.getText().toString();
                if (!respuesta.isEmpty()) {
                    RadioButton radioButton = findViewById(getResources().getIdentifier("radioOption" + index, "id", getPackageName()));
                    radioButton.setText(respuesta);

                    // Actualizar el texto del radio seleccionado también
                    pregunta.setText(respuesta);
                } else {
                    Toast.makeText(MainActivity.this, "La respuesta no puede estar vacía", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void mostrarOpciones() {
        for (int i = 1; i <= 5; i++) {
            int radioButtonId = getResources().getIdentifier("radioOption" + i, "id", getPackageName());
            RadioButton radioButton = findViewById(radioButtonId);
            Button btnEdit = findViewById(getResources().getIdentifier("btnEdit" + i, "id", getPackageName()));
            if (i <= cantidadOpciones) {
                radioButton.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                radioButton.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }

    }

}
