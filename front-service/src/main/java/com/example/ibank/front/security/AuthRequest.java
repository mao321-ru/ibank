package com.example.ibank.front.security;

public record AuthRequest(
    String login,
    String password
) {}
