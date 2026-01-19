package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDocument {
    /** Số nhà, tên đường, hẻm, thôn... */
    private String street;    // 123 Lê Lợi
    /** Phường / Xã */
    private String ward;      // Phường Bến Thành
    /** Quận / Huyện / Thị xã */
    private String district;  // Quận 1
    /** Tỉnh / Thành phố trực thuộc TW */
    private String province;  // TP. Hồ Chí Minh
    /** Quốc gia (ISO code) */
    private String country;   // VN
}
