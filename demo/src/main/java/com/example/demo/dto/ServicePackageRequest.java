package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicePackageRequest(
        @NotBlank(message = "Mã gói dịch vụ không được để trống")
        @Size(max = 30, message = "Mã gói dịch vụ tối đa 30 ký tự")
        String code,

        @NotBlank(message = "Tên gói dịch vụ không được để trống")
        @Size(max = 150, message = "Tên gói dịch vụ tối đa 150 ký tự")
        String name,

        @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
        @Digits(integer = 13, fraction = 2, message = "Giá không hợp lệ")
        BigDecimal price,

        @Size(max = 1_000, message = "Mô tả tối đa 1000 ký tự")
        String description,

        @Size(max = 80, message = "Danh mục tối đa 80 ký tự")
        String category,

        @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
        String imageUrl,

        @Min(value = 0, message = "Giảm giá không được âm")
        @Max(value = 100, message = "Giảm giá tối đa 100%")
        Integer discountPercent,

        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "Trạng thái chỉ nhận ACTIVE hoặc INACTIVE")
        String status
) {
}
