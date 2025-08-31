package com.attendease.backend.attendanceTrackingService.repository;

import com.attendease.backend.model.records.AttendanceRecords;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class AttendanceRepository {

    private static final String COLLECTION_NAME = "attendanceRecords";

    /**
     * Save or update an attendance record in Firestore.
     */
    public void saveAttendance(AttendanceRecords record) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        String docId = record.getRecordId() != null ? record.getRecordId() : generateRecordId(record);
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(docId);
        ApiFuture<WriteResult> result = docRef.set(record);
        result.get();
    }

    /**
     * Retrieve an attendance record by student and event.
     */
    public AttendanceRecords getAttendanceByStudentAndEvent(DocumentReference studentRef, DocumentReference eventRef)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("studentNumberRefId", studentRef)
                .whereEqualTo("eventRefId", eventRef)
                .limit(1)
                .get();

        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            return snapshot.getDocuments().getFirst().toObject(AttendanceRecords.class);
        }

        return null;
    }

    /**
     * Optional helper to generate a predictable document ID from refs.
     */
    private String generateRecordId(AttendanceRecords record) {
        String studentPath = record.getStudentNumberRefId() != null ? record.getStudentNumberRefId().getId() : "unknownStudent";
        String eventPath = record.getEventRefId() != null ? record.getEventRefId().getId() : "unknownEvent";
        return studentPath + "_" + eventPath;
    }
}

