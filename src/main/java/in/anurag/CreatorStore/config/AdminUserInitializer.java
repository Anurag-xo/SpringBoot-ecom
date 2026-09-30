package in.anurag.CreatorStore.config;

import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  CommandLineRunner createAdminUser() {
    return args -> {
      if (!userRepository.existsByUsername("admin")) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");

        User admin =
            User.builder()
                .username("admin")
                .email("admin@creatorstore.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(roles)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created successfully!");
        log.info("Email: admin@creatorstore.com");
        log.info("Password: admin123");
      } else {
        log.info("Admin user already exists, skipping creation");
      }
    };
  }
}
