package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.CouponRequest;
import in.anurag.CreatorStore.dto.CouponValidationResponse;
import in.anurag.CreatorStore.entities.Coupon;
import in.anurag.CreatorStore.entities.DiscountType;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.CouponRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;

  @Transactional
  public Coupon createCoupon(CouponRequest request) {
    if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
      throw new RuntimeException("Coupon code already exists");
    }
    Coupon coupon =
        Coupon.builder()
            .code(request.getCode().toUpperCase())
            .discountType(request.getDiscountType())
            .discountValue(request.getDiscountValue())
            .minOrderAmount(request.getMinOrderAmount())
            .maxDiscountAmount(request.getMaxDiscountAmount())
            .validFrom(request.getValidFrom())
            .validUntil(request.getValidUntil())
            .usageLimit(request.getUsageLimit())
            .isActive(request.getIsActive())
            .build();
    return couponRepository.save(coupon);
  }

  @Transactional
  public Coupon updateCoupon(Long id, CouponRequest request) {
    Coupon coupon =
        couponRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

    if (!coupon.getCode().equalsIgnoreCase(request.getCode())
        && couponRepository.existsByCodeIgnoreCase(request.getCode())) {
      throw new RuntimeException("Coupon code already exists");
    }

    coupon.setCode(request.getCode().toUpperCase());
    coupon.setDiscountType(request.getDiscountType());
    coupon.setDiscountValue(request.getDiscountValue());
    coupon.setMinOrderAmount(request.getMinOrderAmount());
    coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
    coupon.setValidFrom(request.getValidFrom());
    coupon.setValidUntil(request.getValidUntil());
    coupon.setUsageLimit(request.getUsageLimit());
    coupon.setIsActive(request.getIsActive());

    return couponRepository.save(coupon);
  }

  public List<Coupon> getAllCoupons() {
    return couponRepository.findAll();
  }

  @Transactional
  public void deleteCoupon(Long id) {
    couponRepository.deleteById(id);
  }

  public CouponValidationResponse validateCoupon(String code, BigDecimal cartTotal) {
    Coupon coupon = couponRepository.findByCodeIgnoreCase(code).orElse(null);

    if (coupon == null) {
      return new CouponValidationResponse(false, BigDecimal.ZERO, "Invalid coupon code");
    }

    if (!coupon.getIsActive()) {
      return new CouponValidationResponse(
          false, BigDecimal.ZERO, "This coupon is no longer active");
    }

    if (!coupon.isUsable()) {
      return new CouponValidationResponse(
          false, BigDecimal.ZERO, "This coupon has expired or reached its usage limit");
    }

    if (cartTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
      return new CouponValidationResponse(
          false,
          BigDecimal.ZERO,
          "Minimum order amount of $" + coupon.getMinOrderAmount() + " required");
    }

    BigDecimal discountAmount;
    if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
      discountAmount = coupon.getDiscountValue();
    } else {
      discountAmount =
          cartTotal
              .multiply(coupon.getDiscountValue())
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

      if (coupon.getMaxDiscountAmount() != null
          && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
        discountAmount = coupon.getMaxDiscountAmount();
      }
    }

    // Ensure discount doesn't exceed cart total
    if (discountAmount.compareTo(cartTotal) > 0) {
      discountAmount = cartTotal;
    }

    return new CouponValidationResponse(true, discountAmount, "Coupon applied successfully");
  }

  @Transactional
  public void incrementUsage(Coupon coupon) {
    coupon.setTimesUsed(coupon.getTimesUsed() + 1);
    couponRepository.save(coupon);
  }
}
