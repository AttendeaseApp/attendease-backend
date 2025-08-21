package com.attendease.backend.services.authentication.osa;

import com.attendease.backend.data.model.users.Users;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class OsaAuthenticationService {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;

    public OsaAuthenticationService(Firestore firestore, FirebaseAuth firebaseAuth, PasswordEncoder passwordEncoder) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerNewOsaAccount(Users user) {
        try {
            String result = firestore.runTransaction(transaction -> {
                QuerySnapshot existingUserSnapshot = firestore.collection("users")
                        .whereEqualTo("email", user.getEmail())
                        .limit(1)
                        .get()
                        .get();

                if (!existingUserSnapshot.isEmpty()) {
                    log.warn("WARNING: An account with this email already exists.");
                    throw new IllegalStateException("An account with this email already exists.");
                }

                String hashedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(hashedPassword);

                DocumentReference userRef = firestore.collection("users").document();
                user.setUserId(userRef.getId());
                transaction.set(userRef, user);
                return "Added OSA with id: " + user.getUserId();
            }).get();

            log.info("Successfully registered new OSA account with id: {}", user.getUserId());
            return result;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to register OSA account.", e);
            if (e.getCause() instanceof IllegalStateException) {
                throw new IllegalArgumentException(e.getCause().getMessage(), e);
            }
            throw new RuntimeException("Failed to register OSA account." + e);
        }
    }

    public String loginOsa(Users loginRequest) throws ExecutionException, InterruptedException {
        QuerySnapshot osaSnapshot = firestore.collection("users")
                .whereEqualTo("email", loginRequest.getEmail()).limit(1).get()
                .get();

        if (osaSnapshot.isEmpty()) {
            log.warn("Login failed: OSA with email {} not found.", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Users OSA = osaSnapshot.getDocuments().getFirst().toObject(Users.class);

        if (!passwordEncoder.matches(loginRequest.getPassword(), OSA.getPassword())) {
            log.warn("Login failed: Invalid password for OSA {}.", OSA.getEmail());
            throw new IllegalArgumentException("Invalid Email or password.");
        }

        try {
            String customToken = firebaseAuth.createCustomToken(OSA.getUserId());
            log.info("Custom token generated successfully for OSA {}.", OSA.getEmail());
            return customToken;
        } catch (FirebaseAuthException e) {
            log.error("Failed to create custom token for users {}: {}", OSA.getUserId(), e.getMessage());
            throw new RuntimeException("Authentication failed, please try again later.");
        }
    }
}
