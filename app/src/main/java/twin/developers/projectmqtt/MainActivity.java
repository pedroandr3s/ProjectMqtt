    package twin.developers.projectmqtt;

    import android.content.DialogInterface;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.RadioButton;
    import android.widget.RadioGroup;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.HashMap;
    import java.util.Map;

    public class MainActivity extends AppCompatActivity {
        private Mqtt mqttManager;
        private TextView textoSeleccionado;
        private Button btnEnviar,Restaurar;
        private Button pregunta;
        private Button opcion1, opcion2, opcion3, opcion4, opcion5;
        private Button agregar;
        private Button quitar;
        private int cantidadOpciones = 1;
        private int opcionVisible = 1; // Índice de la opción actualmente visible
        private int opcionSeleccionada = 1;
        private int contadorOpcion1 = 0;
        private int contadorOpcion2 = 0;
        private int contadorOpcion3 = 0;
        private int contadorOpcion4 = 0;
        private int contadorOpcion5 = 0;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            opcion1 = findViewById(R.id.opcion1);
            opcion2 = findViewById(R.id.opcion2);
            opcion3 = findViewById(R.id.opcion3);
            opcion4 = findViewById(R.id.opcion4);
            opcion5 = findViewById(R.id.opcion5);
            pregunta = findViewById(R.id.pregunta);
            btnEnviar = findViewById(R.id.btnPublish);
            Restaurar = findViewById(R.id.btnRestaurar);
            agregar = findViewById(R.id.agregar);
            quitar = findViewById(R.id.quitar);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference Pregunta = database.getReference("Pregunta");
            textoSeleccionado = findViewById(R.id.textoSeleccionado);

            mqttManager = new Mqtt(getApplicationContext());
            mqttManager.connectToMqttBroker();
            escucharCambiosEnFirebase();
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
            Restaurar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restaurarTextosOriginales();
                    guardarInformacionEnFirebase();
                }
            });
            btnEnviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String textoSeleccionadoString = textoSeleccionado.getText().toString();
                    guardarInformacionEnFirebase();
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

            // Incrementar el contador correspondiente
            switch (index) {
                case 1:
                    contadorOpcion1++;
                    break;
                case 2:
                    contadorOpcion2++;
                    break;
                case 3:
                    contadorOpcion3++;
                    break;
                case 4:
                    contadorOpcion4++;
                    break;
                case 5:
                    contadorOpcion5++;
                    break;
            }

            actualizarTextoSeleccionado();
         }

        private void restaurarTextosOriginales() {
            // Restaurar pregunta
            pregunta.setText("Pregunta");

            // Restaurar opciones
            opcion1.setText("Opción 1");
            opcion2.setText("Opción 2");
            opcion3.setText("Opción 3");
            opcion4.setText("Opción 4");
            opcion5.setText("Opción 5");

            // Restaurar contadores
            contadorOpcion1 = 0;
            contadorOpcion2 = 0;
            contadorOpcion3 = 0;
            contadorOpcion4 = 0;
            contadorOpcion5 = 0;

            // Actualizar el textoSeleccionado
            actualizarTextoSeleccionado();
        }

        private void actualizarTextoSeleccionado() {
            // Actualizar el textoSeleccionado con la información de los contadores
            String textoSeleccionadoString = "Opción 1: " + contadorOpcion1 +
                    ", Opción 2: " + contadorOpcion2 +
                    ", Opción 3: " + contadorOpcion3 +
                    ", Opción 4: " + contadorOpcion4 +
                    ", Opción 5: " + contadorOpcion5;

            TextView textoSeleccionado = findViewById(R.id.textoSeleccionado);
            textoSeleccionado.setText(textoSeleccionadoString);
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

        private void guardarInformacionEnFirebase() {
            String nuevaPregunta = pregunta.getText().toString();
            String nuevaOpcion1 = opcion1.getText().toString();
            String nuevaOpcion2 = opcion2.getText().toString();
            String nuevaOpcion3 = opcion3.getText().toString();
            String nuevaOpcion4 = opcion4.getText().toString();
            String nuevaOpcion5 = opcion5.getText().toString();

            if (textoSeleccionado != null) {
                String textoSeleccionadoString = textoSeleccionado.getText().toString();

                // Inicializa la referencia a la base de datos Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("Pregunta");

                // Sobrescribe el valor en el nodo "Pregunta" con la nueva pregunta, las opciones y el textoSeleccionado
                Map<String, Object> datos = new HashMap<>();
                datos.put("Pregunta", nuevaPregunta);
                datos.put("Opcion1", nuevaOpcion1);
                datos.put("Opcion2", nuevaOpcion2);
                datos.put("Opcion3", nuevaOpcion3);
                datos.put("Opcion4", nuevaOpcion4);
                datos.put("Opcion5", nuevaOpcion5);
                datos.put("TextoSeleccionado", textoSeleccionadoString);

                ref.setValue(datos, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        // Handle the error here, e.g., Log.e("FirebaseError", databaseError.getMessage());
                        Toast.makeText(MainActivity.this, "Error al guardar los datos en Firebase", Toast.LENGTH_SHORT).show();
                    } else {
                        // Datos guardados con éxito
                        Toast.makeText(MainActivity.this, "Datos guardados correctamente en Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("MainActivity", "El objeto textoSeleccionado es nulo");
            }
        }




        private void escucharCambiosEnFirebase() {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("Pregunta");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Verifica si hay datos en el nodo "Pregunta"
                    if (dataSnapshot.exists()) {
                        // Obtiene la pregunta y todas las opciones directamente
                        String ultimaPregunta = dataSnapshot.child("Pregunta").getValue(String.class);
                        String opcion1Texto = dataSnapshot.child("Opcion1").getValue(String.class);
                        String opcion2Texto = dataSnapshot.child("Opcion2").getValue(String.class);
                        String opcion3Texto = dataSnapshot.child("Opcion3").getValue(String.class);
                        String opcion4Texto = dataSnapshot.child("Opcion4").getValue(String.class);
                        String opcion5Texto = dataSnapshot.child("Opcion5").getValue(String.class);
                        String textoSeleccionadoString = dataSnapshot.child("TextoSeleccionado").getValue(String.class);

                        // Actualiza el contenido de preguntaTextView
                        pregunta.setText(ultimaPregunta);

                        // Actualiza el contenido de las opciones
                        opcion1.setText(opcion1Texto);
                        opcion2.setText(opcion2Texto);
                        opcion3.setText(opcion3Texto);
                        opcion4.setText(opcion4Texto);
                        opcion5.setText(opcion5Texto);

                        // Actualiza el contenido de textoSeleccionado
                        if (textoSeleccionado != null) {
                            textoSeleccionado.setText(textoSeleccionadoString);
                        } else {
                            Log.e("MainActivity", "El objeto textoSeleccionado es nulo");
                        }

                        // Maneja las opciones según tus necesidades
                        // Por ejemplo, puedes mostrarlas en TextViews o hacer cualquier otra cosa con ellas
                    } else {
                        // Maneja el caso en el que no hay datos en el nodo "Pregunta"
                        pregunta.setText("No hay pregunta disponible.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Maneja los errores aquí
                }
            });
        }


    }
