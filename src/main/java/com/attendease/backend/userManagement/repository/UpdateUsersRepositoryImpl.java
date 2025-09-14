package com.attendease.backend.userManagement.repository;

import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.students.UserStudent.UserStudent;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.repository.users.UserUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UpdateUsersRepositoryImpl implements UserUpdateRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Users deactivateUser(String userId) throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            Query query = new Query(Criteria.where("userId").is(userId));
            Update update = new Update().set("accountStatus", AccountStatus.INACTIVE.name());
            mongoTemplate.updateFirst(query, update, Users.class);
            return mongoTemplate.findOne(query, Users.class);
        }).get();
    }

    @Override
    public Users reactivateUser(String userId) throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            Query query = new Query(Criteria.where("userId").is(userId));
            Update update = new Update().set("accountStatus", AccountStatus.ACTIVE.name());
            mongoTemplate.updateFirst(query, update, Users.class);
            return mongoTemplate.findOne(query, Users.class);
        }).get();
    }

    @Override
    public List<Users> bulkDeactivateUsers() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            Query query = new Query(Criteria.where("accountStatus").is(AccountStatus.ACTIVE.name()));
            Update update = new Update().set("accountStatus", AccountStatus.INACTIVE.name());
            mongoTemplate.updateMulti(query, update, Users.class);
            return mongoTemplate.find(query, Users.class);
        }).get();
    }

    @Override
    public List<Users> searchUsersByKeywords(String keyword) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("firstName").regex(keyword, "i"),
                Criteria.where("lastName").regex(keyword, "i"),
                Criteria.where("email").regex(keyword, "i"),
                Criteria.where("contactNumber").regex(keyword, "i")
        ));
        return mongoTemplate.find(query, Users.class);
    }
}
