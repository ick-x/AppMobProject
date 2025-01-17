package com.example.tp1a;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BD {
    private static final String DATABASE_PATH = "users";

    public static void insertUser(String username, String password) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child(DATABASE_PATH);

        // Generate a unique key for the user
        String userId = reference.push().getKey();

        // Create a User object with the provided data
        User user = new User(userId, username, password, 0);

        // Insert the user into the database
        reference.child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> Log.d("BD", "Utilisateur inséré avec succès dans la base de données !"))
                .addOnFailureListener(e -> Log.e("BD", "Erreur lors de l'insertion de l'utilisateur : " + e.getMessage()));
    }

    public static CompletableFuture<Boolean> userExists(String username, String password) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(DATABASE_PATH);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> userList = new ArrayList<>();
                boolean exists = false;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Convertir chaque snapshot en objet User
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // Utiliser la liste des utilisateurs récupérés
                for (User user : userList) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        exists = true;
                        break;
                    }
                }
                future.complete(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gérer les erreurs d'accès à la base de données
                Log.e("BD", "Erreur lors de la récupération des utilisateurs : " + databaseError.getMessage());
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }
}