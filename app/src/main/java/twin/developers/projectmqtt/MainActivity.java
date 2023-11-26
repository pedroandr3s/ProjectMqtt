package twin.developers.projectmqtt;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

    // Elimina la variable radioGroup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pregunta = findViewById(R.id.pregunta);
        btnEnviar = findViewById(R.id.btnPublish);
        agregar = findViewById(R.id.agregar);
        quitar = findViewById(R.id.quitar);
        final TextView textoSeleccionado = findViewById(R.id.textoSeleccionado);
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
                    mostrarOpciones();
                }
            }
        });

        quitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cantidadOpciones > 1) {
                    cantidadOpciones--;
                    if (opcionSeleccionada > cantidadOpciones) {
                        opcionSeleccionada = cantidadOpciones;
                    }
                    mostrarOpciones();
                }
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoSeleccionadoString = textoSeleccionado.getText().toString();
                if (!textoSeleccionadoString.isEmpty()) {
                    // Publicar el mensaje en el tópico MQTT
                    mqttManager.publishMessage("encuesta/01", textoSeleccionadoString);
                } else {
                    Toast.makeText(MainActivity.this, "Selecciona una opción", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for (int i = 1; i <= 5; i++) {
            final int index = i;
            Button btnEdit = findViewById(getResources().getIdentifier("btnEdit" + i, "id", getPackageName()));
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoEditar(index);
                }
            });

            // Asociar OnClickListener para los botones de opción
            Button opcionButton = findViewById(getResources().getIdentifier("opcion" + i, "id", getPackageName()));
            opcionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOpcionButtonClick(index);
                }
            });
        }
    }

// Elimina el método onCheckedChanged

    private void mostrarOpciones() {
        for (int i = 1; i <= 5; i++) {
            int opcionButtonId = getResources().getIdentifier("opcion" + i, "id", getPackageName());
            Button opcionButton = findViewById(opcionButtonId);
            Button btnEdit = findViewById(getResources().getIdentifier("btnEdit" + i, "id", getPackageName()));
            if (i <= cantidadOpciones) {
                opcionButton.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                opcionButton.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }
    }


    private void onOpcionButtonClick(int index) {
        // Cambiar el color del botón de opción (puedes personalizar esto según tus necesidades)
        Button opcionButton = findViewById(getResources().getIdentifier("opcion" + index, "id", getPackageName()));

        // Actualizar el textoSeleccionado
        TextView textoSeleccionado = findViewById(R.id.textoSeleccionado);
        textoSeleccionado.setText(opcionButton.getText().toString());
    }
    private void mostrarOpcion() {
        for (int i = 1; i <= 5; i++) {
            int opcionButtonId = getResources().getIdentifier("opcion" + i, "id", getPackageName());
            Button opcionButton = findViewById(opcionButtonId);
            Button btnEdit = findViewById(getResources().getIdentifier("btnEdit" + i, "id", getPackageName()));
            if (i == opcionVisible) {
                opcionButton.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                opcionButton.setVisibility(View.GONE);
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
                    // Actualizar el texto del botón de opción
                    Button opcionButton = findViewById(getResources().getIdentifier("opcion" + index, "id", getPackageName()));
                    opcionButton.setText(respuesta);
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

}
