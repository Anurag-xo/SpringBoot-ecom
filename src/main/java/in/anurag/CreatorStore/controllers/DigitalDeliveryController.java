package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.DigitalDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/digital")
@RequiredArgsConstructor
@Tag(name = "Digital Delivery", description = "Secure digital product download endpoints")
public class DigitalDeliveryController {

  private final DigitalDeliveryService digitalDeliveryService;
  private final UserRepository userRepository;

  @Operation(summary = "Download a digital product from a purchased order")
  @GetMapping("/orders/{orderId}/download/{productId}")
  public ResponseEntity<Resource> downloadDigitalProduct(
      @PathVariable Long orderId, @PathVariable Long productId) {

    User user = getCurrentUser();
    Resource resource = digitalDeliveryService.getDigitalDownloadResource(orderId, productId, user);

    // Determine content type (fallback to generic binary stream)
    String contentType = "application/octet-stream";
    try {
      contentType = Files.probeContentType(resource.getFile().toPath());
      if (contentType == null) {
        contentType = "application/octet-stream";
      }
    } catch (Exception e) {
      // Ignore, fallback to octet-stream
    }

    String filename = resource.getFilename();
    if (filename == null) {
      filename = "digital-product";
    }

    // Force download via Content-Disposition header
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(resource);
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
