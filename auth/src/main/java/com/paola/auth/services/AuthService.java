package com.paola.auth.services;

import com.paola.auth.dto.LoginRequest;
import com.paola.auth.dto.TokenResponse;

public interface AuthService {

    TokenResponse autenticar(LoginRequest request) throws Exception;
}
