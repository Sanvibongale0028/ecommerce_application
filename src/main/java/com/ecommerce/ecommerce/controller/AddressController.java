// controller/AddressController.java
package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.AddressRequest;
import com.ecommerce.ecommerce.dto.AddressResponse;
import com.ecommerce.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.addAddress(request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefault(id));
    }
}