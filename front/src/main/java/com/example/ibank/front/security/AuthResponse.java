package com.example.ibank.front.security;

import java.util.List;

public record AuthResponse(
    Boolean isValid,
    String userId,
    List<String> roles
) {}
