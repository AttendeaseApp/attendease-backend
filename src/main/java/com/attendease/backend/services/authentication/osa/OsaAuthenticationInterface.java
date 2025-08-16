package com.attendease.backend.services.authentication.osa;

import com.attendease.backend.data.model.users.Users;

import java.util.concurrent.ExecutionException;

public interface OsaAuthenticationInterface {
    String registerNewOsaAccount(Users user);
    String loginOsa(Users loginRequest) throws ExecutionException, InterruptedException;
}
