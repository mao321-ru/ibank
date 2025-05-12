package com.example.ibank.front.security;

import java.util.List;

public record AuthResponse(
    String userId,
    List<String> roles
) {}
