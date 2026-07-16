package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest  {

    @NotBlank(message = "Category name is required")
    private String name;

    private Long parentCategoryId; // nullable - null means top-level category

}