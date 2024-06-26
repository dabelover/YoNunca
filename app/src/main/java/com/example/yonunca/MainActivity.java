package com.example.yonunca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Array inicial de frases
    private List<String> phrases = new ArrayList<>();
    private HashSet<String> phrasesSet = new HashSet<>(phrases);
    private Button btnLogout; // Botón para cerrar sesión
    private FirebaseAuth mAuth; // Instancia de FirebaseAuth
    // Variables para los componentes de la UI
    private TextView tvPhrase;
    private TextView tvAuthor; // TextView para mostrar el autor
    private Button btnNewPhrase;
    private Button btnUpdatePhrases;

    private Button btnAddPhrase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar los componentes de la UI
        tvPhrase = findViewById(R.id.tvPhrase);
        tvAuthor = findViewById(R.id.tvAuthor); // Inicializar el TextView del autor
        btnNewPhrase = findViewById(R.id.btnNewPhrase);
        btnUpdatePhrases = findViewById(R.id.btnUpdatePhrases);
        btnAddPhrase  = findViewById(R.id.btnAddPhrase);

        // Listener para el botón de nueva frase
        btnNewPhrase.setOnClickListener(v -> displayRandomPhraseAndAuthor());

        // Listener para el botón de actualizar frases
        btnUpdatePhrases.setOnClickListener(v -> updatePhrases(db));

        // Inicializar el botón de salir
        btnLogout = findViewById(R.id.btnLogout);

        // Listener para el botón de añadir frase
        btnAddPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPhrase.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Cerrar sesión en Firebase
            goToLoginScreen(); // Método para ir a la pantalla de login
        });
    }

    // Método para mostrar una frase y autor aleatorios
    private void displayRandomPhraseAndAuthor() {
        if (phrases.isEmpty()) {
            tvPhrase.setText("No hay frases disponibles.");
            tvAuthor.setText(""); // Limpiar autor si no hay frases
        } else {
            int randomIndex = new Random().nextInt(phrases.size());
            String phraseAndAuthor = phrases.get(randomIndex);
            String[] parts = phraseAndAuthor.split("//");
            if (parts.length > 1) {
                tvAuthor.setText(parts[0]); // Autor
                tvPhrase.setText(parts[1]); // Frase
            }
        }
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(this, Login.class); // Asumiendo que tienes una LoginActivity para el login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia el stack de actividades
        startActivity(intent);
        finish(); // Finaliza MainActivity
    }

    // Método para actualizar frases desde Firestore
    private void updatePhrases(FirebaseFirestore db) {
        db.collection("Phrases").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> newPhrases = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String phrase = document.getString("phrase");
                    String author = document.getString("author");
                    if (phrase != null && author != null) {
                        // Limpiar el número del inicio de cada frase
                        String cleanPhrase = phrase.replaceAll("^[0-9]+\\.\\s*", "");
                        String phraseWithAuthor = author + "//" + cleanPhrase;
                        newPhrases.add(phraseWithAuthor);
                        System.out.println("Frase añadida:  " + phraseWithAuthor);
                    }
                }
                addNewPhrases(newPhrases);
            } else {
                System.out.println("Error getting documents: " + task.getException());
            }
        });
    }

    // Método para agregar nuevas frases si no existen en el conjunto actual
    private void addNewPhrases(List<String> newPhrases) {
        List<String> phrasesToAdd = new ArrayList<>();
        for (String phrase : newPhrases) {
            if (!phrasesSet.contains(phrase)) {
                phrasesToAdd.add(phrase);
                phrasesSet.add(phrase);
            }
        }
        phrases.addAll(phrasesToAdd);
        Toast.makeText(MainActivity.this, "Frases añadidas", Toast.LENGTH_SHORT).show();
    }
}

