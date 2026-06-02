package com.rishi.taskmanager.DTO;

import com.rishi.taskmanager.model.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private User.Role role; // optional — defaults to USER if null
    private String adminSecret;
}

