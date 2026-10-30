package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.AddressRequest;
import in.anurag.CreatorStore.entities.AddressType;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.UserAddress;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.UserAddressRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {
  private final UserAddressRepository addressRepository;

  public List<UserAddress> getAddressesByUser(User user) {
    return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
  }

  public UserAddress getAddressById(Long id, User user) {
    UserAddress address =
        addressRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    if (!address.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You do not have permission to access this address");
    }
    return address;
  }

  @Transactional
  public UserAddress createAddress(AddressRequest request, User user) {
    UserAddress address =
        UserAddress.builder()
            .user(user)
            .fullName(request.getFullName())
            .addressLine1(request.getAddressLine1())
            .addressLine2(request.getAddressLine2())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .phoneNumber(request.getPhoneNumber())
            .addressType(request.getAddressType())
            .isDefault(request.getIsDefault())
            .build();

    if (Boolean.TRUE.equals(request.getIsDefault())) {
      unsetOtherDefaults(user, request.getAddressType());
    }

    return addressRepository.save(address);
  }

  @Transactional
  public UserAddress updateAddress(Long id, AddressRequest request, User user) {
    UserAddress address = getAddressById(id, user);

    address.setFullName(request.getFullName());
    address.setAddressLine1(request.getAddressLine1());
    address.setAddressLine2(request.getAddressLine2());
    address.setCity(request.getCity());
    address.setState(request.getState());
    address.setPostalCode(request.getPostalCode());
    address.setCountry(request.getCountry());
    address.setPhoneNumber(request.getPhoneNumber());
    address.setAddressType(request.getAddressType());

    if (Boolean.TRUE.equals(request.getIsDefault())) {
      unsetOtherDefaults(user, request.getAddressType());
    }
    address.setIsDefault(request.getIsDefault());

    return addressRepository.save(address);
  }

  @Transactional
  public void deleteAddress(Long id, User user) {
    UserAddress address = getAddressById(id, user);
    addressRepository.delete(address);
  }

  @Transactional
  public UserAddress setDefaultAddress(Long id, User user) {
    UserAddress address = getAddressById(id, user);
    unsetOtherDefaults(user, address.getAddressType());
    address.setIsDefault(true);
    return addressRepository.save(address);
  }

  private void unsetOtherDefaults(User user, AddressType type) {
    List<UserAddress> userAddresses = addressRepository.findByUserAndAddressType(user, type);
    for (UserAddress addr : userAddresses) {
      if (Boolean.TRUE.equals(addr.getIsDefault())) {
        addr.setIsDefault(false);
        addressRepository.save(addr);
      }
    }
  }
}
