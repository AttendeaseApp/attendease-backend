package com.attendease.attendease_backend.services.authentication.osa;

import com.attendease.attendease_backend.data.user.User;

import java.util.concurrent.ExecutionException;

public interface OsaAuthenticationInterface {
    String registerNewOsaAccount(User user);
    String loginOsa(User loginRequest) throws ExecutionException, InterruptedException;
}
