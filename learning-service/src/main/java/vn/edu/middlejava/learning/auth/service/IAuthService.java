package vn.edu.middlejava.learning.auth.service;

import vn.edu.middlejava.learning.auth.dto.AuthResponse;
import vn.edu.middlejava.learning.auth.dto.LoginRequest;
import vn.edu.middlejava.learning.auth.dto.RefreshRequest;
import vn.edu.middlejava.learning.auth.dto.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshRequest request);
    void logout(RefreshRequest request);
}
