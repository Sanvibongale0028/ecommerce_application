package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.AddressRequest;
import com.ecommerce.ecommerce.dto.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse addAddress(AddressRequest request);
    List<AddressResponse> getMyAddresses();
    AddressResponse updateAddress(Long addressId, AddressRequest request);
    void deleteAddress(Long addressId);
    AddressResponse setDefault(Long addressId);
}