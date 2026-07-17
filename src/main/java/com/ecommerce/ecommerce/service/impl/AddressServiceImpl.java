// service/impl/AddressServiceImpl.java
package com.ecommerce.ecommerce.service.impl;

import com.ecommerce.ecommerce.dto.AddressRequest;
import com.ecommerce.ecommerce.dto.AddressResponse;
import com.ecommerce.ecommerce.entity.Address;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce.repository.AddressRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.security.SecurityUtil;
import com.ecommerce.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    private User getCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public AddressResponse addAddress(AddressRequest request) {
        User user = getCurrentUser();

        // If this new address is being marked default, un-mark the old one first
        if (request.isDefaultAddress()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        addressRepository.save(existing);
                    });
        }

        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setDefault(request.isDefaultAddress());

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    @Override
    public List<AddressResponse> getMyAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = getCurrentUser();

        // Ownership check happens at the query level, not fetch-then-check
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (request.isDefaultAddress() && !address.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        addressRepository.save(existing);
                    });
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setDefault(request.isDefaultAddress());

        Address updated = addressRepository.save(address);
        return mapToResponse(updated);
    }

    @Override
    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepository.delete(address);
    }

    @Override
    public AddressResponse setDefault(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(existing -> {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                });

        address.setDefault(true);
        Address updated = addressRepository.save(address);
        return mapToResponse(updated);
    }

    private AddressResponse mapToResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.isDefault()); // entity accessor unchanged — only DTO accessors were renamed
    }
}