package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.AuthResponse;
import in.anurag.CreatorStore.dto.ForgotPasswordRequest;
import in.anurag.CreatorStore.dto.LoginRequest;
import in.anurag.CreatorStore.dto.RegisterRequest;
import in.anurag.CreatorStore.dto.ResetPasswordRequest;
import in.anurag.CreatorStore.services.AuthService;
import in.anurag.CreatorStore.services.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = "Endpoints for user registration, login, and password management")
public class AuthController {
  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account and returns a JWT token.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully registered"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error or username/email already exists")
      })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully logged in"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
      })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Operation(
      summary = "Forgot Password",
      description = "Sends a password reset link to the user's email.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Reset link sent successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @PostMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.processForgotPassword(request);
    // Generic message to prevent email enumeration attacks
    return ResponseEntity.ok(
        "If an account with that email exists, a password reset link has been sent.");
  }

  @Operation(
      summary = "Reset Password",
      description = "Resets the user's password using a valid token.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
      })
  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request);
    return ResponseEntity.ok("Password has been reset successfully.");
  }
}
