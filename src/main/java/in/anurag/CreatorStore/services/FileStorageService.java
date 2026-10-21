package in.anurag.CreatorStore.services;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
  private final Path fileStorageLocation;

  public FileStorageService() {
    this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(this.fileStorageLocation);
      Files.createDirectories(
          this.fileStorageLocation.resolve("digital")); // ✅ NEW: Ensure digital folder exists
    } catch (IOException ex) {
      throw new RuntimeException(
          "Could not create the directory where the uploaded files will be stored.", ex);
    }
  }

  public String storeFile(MultipartFile file) {
    String originalFileName = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }
    String fileName = UUID.randomUUID().toString() + fileExtension;
    try {
      if (!isAllowedExtension(fileExtension)) {
        throw new RuntimeException("Sorry! Only JPG, JPEG, PNG, and GIF files are allowed.");
      }
      Path targetLocation = this.fileStorageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
      return "uploads/" + fileName;
    } catch (IOException ex) {
      throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
    }
  }

  // ✅ NEW: Dedicated method for digital files (allows ZIP, PDF, etc.)
  public String storeDigitalFile(MultipartFile file) {
    String originalFileName = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }
    String fileName = UUID.randomUUID().toString() + fileExtension;

    Path digitalStorageLocation =
        this.fileStorageLocation.resolve("digital").toAbsolutePath().normalize();
    try {
      Files.createDirectories(digitalStorageLocation);
      Path targetLocation = digitalStorageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
      return "uploads/digital/" + fileName;
    } catch (IOException ex) {
      throw new RuntimeException(
          "Could not store digital file " + fileName + ". Please try again!", ex);
    }
  }

  private boolean isAllowedExtension(String extension) {
    if (extension == null) return false;
    String ext = extension.toLowerCase();
    return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".gif");
  }
}
