package in.anurag.CreatorStore.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthDto {
  private LocalDate date;
  private Long newUsers;
}
