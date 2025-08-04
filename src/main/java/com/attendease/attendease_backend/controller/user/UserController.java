package com.attendease.attendease_backend.controller.user;

import com.attendease.attendease_backend.data.user.User;
import com.attendease.attendease_backend.enums.AccountStatus;
import com.attendease.attendease_backend.enums.UserType;
import com.attendease.attendease_backend.services.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("v1/api/auth/register")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("osa")
    public ResponseEntity<String> createOsa(@RequestBody User user) {
        user.setUserType(UserType.OSA);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PostMapping("student")
    public ResponseEntity<String> createStudent(@RequestBody User user) {
        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return ResponseEntity.ok(userService.createUser(user));
    }
}
