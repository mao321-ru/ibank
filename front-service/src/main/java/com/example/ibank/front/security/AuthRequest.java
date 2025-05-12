package com.example.ibank.front.security;

public record AuthRequest(
    String username,
    String password
) {}
