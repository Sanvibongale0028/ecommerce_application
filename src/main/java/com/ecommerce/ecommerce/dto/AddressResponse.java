// dto/AddressResponse.java
package com.ecommerce.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String pincode;

    @JsonProperty("isDefault")
    private boolean defaultAddress;
}