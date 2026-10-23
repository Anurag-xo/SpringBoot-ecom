package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.UserDataExportDto;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
    name = "User Profile & GDPR",
    description = "User data export and account deletion (GDPR compliance)")
public class UserProfileController {

  private final UserProfileService userProfileService;
  private final UserRepository userRepository;

  @Operation(
      summary = "Export my data (GDPR)",
      description = "Downloads a JSON file containing all user data.")
  @GetMapping("/me/export")
  public ResponseEntity<UserDataExportDto> exportMyData() {
    User user = getCurrentUser();
    UserDataExportDto data = userProfileService.exportUserData(user);

    // Force browser to download as a JSON file
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_creatorstore_data.json")
        .contentType(MediaType.APPLICATION_JSON)
        .body(data);
  }

  @Operation(
      summary = "Delete my account (GDPR)",
      description = "Anonymizes PII and soft-deletes the user account.")
  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteMyAccount() {
    User user = getCurrentUser();
    userProfileService.deleteAccount(user);
    return ResponseEntity.noContent().build();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
