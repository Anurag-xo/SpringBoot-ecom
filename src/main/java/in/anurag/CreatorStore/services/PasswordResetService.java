package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.ForgotPasswordRequest;
import in.anurag.CreatorStore.dto.ResetPasswordRequest;
import in.anurag.CreatorStore.entities.PasswordResetToken;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.PasswordResetTokenRepository;
import in.anurag.CreatorStore.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontendUrl;

  @Transactional
  public void processForgotPassword(ForgotPasswordRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

    // Delete any existing token for this user to prevent multiple active tokens
    tokenRepository.deleteByUser(user);

    // Generate new secure token
    String token = UUID.randomUUID().toString();
    LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30); // 30 minutes validity

    PasswordResetToken resetToken =
        PasswordResetToken.builder().token(token).user(user).expiryDate(expiryDate).build();

    tokenRepository.save(resetToken);

    // Send email with reset link
    String resetLink = frontendUrl + "/reset-password?token=" + token;
    emailService.sendPasswordResetEmail(user, resetLink);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    PasswordResetToken resetToken =
        tokenRepository
            .findByToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

    if (resetToken.isExpired()) {
      tokenRepository.delete(resetToken);
      throw new RuntimeException("Password reset token has expired");
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Clean up the token after successful reset
    tokenRepository.delete(resetToken);
  }
}
