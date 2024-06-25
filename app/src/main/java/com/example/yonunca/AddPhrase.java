package com.example.yonunca;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.List;

public class AddPhrase extends AppCompatActivity {

    private EditText editTextAuthor;
    private EditText editTextPhrase;
    private Button btnAddPhrase;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phrase);

        db = FirebaseFirestore.getInstance();

        editTextAuthor = findViewById(R.id.editTextAuthor);
        editTextPhrase = findViewById(R.id.editTextPhrase);
        btnAddPhrase = findViewById(R.id.btnAddPhrase);

        btnAddPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String author = editTextAuthor.getText().toString().trim();
                final String phrase = editTextPhrase.getText().toString().trim();

                if (!author.isEmpty() && !phrase.isEmpty()) {
                    addPhraseWithIncrementalId(author, phrase);
                } else {
                    Toast.makeText(AddPhrase.this, "Por favor, ingrese ambos campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addPhraseWithIncrementalId(final String author, final String phrase) {
        final CollectionReference phrasesCollection = db.collection("Phrases");

        // Obtener el último ID usado, ordenando por el campo "ID" descendentemente y limitando a 1
        phrasesCollection.orderBy("ID", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            long newId = 1; // Comenzar en 1 si no hay documentos aún
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot lastDocument = queryDocumentSnapshots.getDocuments().get(0);
                Number lastId = lastDocument.getLong("ID");
                if (lastId != null) {
                    newId = lastId.longValue() + 1; // Incrementar el último ID
                }
            }

            // Crear un nuevo documento con un ID incremental en un campo
            Phrase newPhrase = new Phrase(author, phrase, newId);
            phrasesCollection.add(newPhrase) // Aquí usamos add para generar un ID de documento automático
                    .addOnSuccessListener(aVoid -> Toast.makeText(AddPhrase.this, "Frase agregada con éxito.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(AddPhrase.this, "Error al agregar frase.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(AddPhrase.this, "Error al recuperar el último ID.", Toast.LENGTH_SHORT).show());
    }

    // Clase para manejar los datos de frase
    public static class Phrase {
        private String author;
        private String phrase;
        private long ID;

        public Phrase(String author, String phrase, long ID) {
            this.author = author;
            this.phrase = phrase;
            this.ID = ID;
        }

        public String getAuthor() {
            return author;
        }

        public String getPhrase() {
            return phrase;
        }

        public long getID() {
            return ID;
        }
    }


}

