package com.attendease.attendease_backend.services.user;

import com.attendease.attendease_backend.data.user.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    private Firestore firestore;

    public String createUser(User user) {
        try {
            ApiFuture<DocumentReference> users = firestore.collection("users").add(user);
            return "Added: " + users.get().getId();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public User getUser(String userId) {
        try {
            ApiFuture<DocumentSnapshot> users = firestore.collection("users").document(userId).get();
            return users.get().toObject(User.class);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
