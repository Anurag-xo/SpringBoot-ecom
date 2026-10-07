package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.RevenueDto;
import in.anurag.CreatorStore.dto.TopProductDto;
import in.anurag.CreatorStore.dto.UserGrowthDto;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.OrderItemRepository;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;

  public RevenueDto getRevenueAnalytics(int days) {
    LocalDateTime startDate = LocalDateTime.now().minusDays(days);
    Object[] result = orderRepository.getRevenueStats(startDate);

    // Safe casting for H2 vs Postgres compatibility
    BigDecimal totalRevenue = BigDecimal.ZERO;
    if (result[0] != null) {
      totalRevenue =
          result[0] instanceof BigDecimal
              ? (BigDecimal) result[0]
              : BigDecimal.valueOf(((Number) result[0]).doubleValue());
    }

    Long totalOrders = result[1] != null ? ((Number) result[1]).longValue() : 0L;

    return new RevenueDto(totalRevenue, totalOrders, days);
  }

  public List<TopProductDto> getTopSellingProducts(int days, int limit) {
    LocalDateTime startDate = LocalDateTime.now().minusDays(days);
    List<Object[]> results =
        orderItemRepository.findTopSellingProducts(startDate, PageRequest.of(0, limit));

    return results.stream()
        .map(
            row -> {
              BigDecimal revenue =
                  row[2] instanceof BigDecimal
                      ? (BigDecimal) row[2]
                      : BigDecimal.valueOf(((Number) row[2]).doubleValue());

              return new TopProductDto((String) row[0], ((Number) row[1]).longValue(), revenue);
            })
        .collect(Collectors.toList());
  }

  public List<UserGrowthDto> getUserGrowth(int days) {
    LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
    List<User> users = userRepository.findByCreatedAtGreaterThanEqual(startDate);

    // Group by date in Java to ensure 100% H2 and Postgres compatibility
    Map<LocalDate, Long> grouped =
        users.stream()
            .collect(
                Collectors.groupingBy(
                    user -> user.getCreatedAt().toLocalDate(), Collectors.counting()));

    return grouped.entrySet().stream()
        .map(entry -> new UserGrowthDto(entry.getKey(), entry.getValue()))
        .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
        .collect(Collectors.toList());
  }
}
