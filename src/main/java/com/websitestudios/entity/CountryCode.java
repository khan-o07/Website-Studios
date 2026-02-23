package com.websitestudios.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores country calling codes for the phone number dropdown.
 *
 * NOTE: dial_code is NOT unique because multiple countries
 * share the same code (e.g., +1 = US, Canada, Jamaica, etc.)
 * iso_code IS unique (each country has a unique ISO 3166 code)
 */
@Entity
@Table(name = "country_codes", uniqueConstraints = {
                @UniqueConstraint(name = "uk_country_iso_code", columnNames = "iso_code")
}, indexes = {
                @Index(name = "idx_country_name", columnList = "country_name"),
                @Index(name = "idx_country_dial_code", columnList = "dial_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryCode {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "country_name", nullable = false, length = 100)
        private String countryName;

        @Column(name = "dial_code", nullable = false, length = 10)
        private String dialCode;

        @Column(name = "iso_code", nullable = false, length = 5)
        private String isoCode;

        @Column(name = "flag_emoji", length = 10)
        private String flagEmoji;
}