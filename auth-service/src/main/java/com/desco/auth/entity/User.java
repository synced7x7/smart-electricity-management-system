package com.desco.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_role")
    @ColumnTransformer(write = "?::user_role")
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private AreaName area;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum UserRole {
        USER, ADMIN
    }

    public enum AreaName {
        UTTARA, GULSHAN, BANANI, DHANMONDI,
        BASHUNDHARA, MIRPUR, BANASREE, BARIDHARA,
        BARGUNA, BARISHAL, BHOLA, JHALOKATI,
        PATUAKHALI, PIROJPUR,
        // Chattogram
        BANDARBAN, BRAHMANBARIA, CHANDPUR, CHATTOGRAM,
        CUMILLA, COXS_BAZAR, FENI, KHAGRACHHARI,
        LAKSHMIPUR, NOAKHALI, RANGAMATI,
        // Dhaka
        DHAKA, FARIDPUR, GAZIPUR, GOPALGANJ,
        KISHOREGANJ, MADARIPUR, MANIKGANJ, MUNSHIGANJ,
        NARAYANGANJ, NARSINGDI, RAJBARI, SHARIATPUR,
        TANGAIL,
        // Khulna
        BAGERHAT, CHUADANGA, JASHORE, JHENAIDAH,
        KHULNA, KUSHTIA, MAGURA, MEHERPUR,
        NARAIL, SATKHIRA,
        // Mymensingh
        JAMALPUR, MYMENSINGH, NETROKONA, SHERPUR,
        // Rajshahi
        BOGURA, CHAPAINAWABGANJ, JOYPURHAT, NAOGAON,
        NATORE, PABNA, RAJSHAHI, SIRAJGANJ,
        // Rangpur
        DINAJPUR, GAIBANDHA, KURIGRAM, LALMONIRHAT,
        NILPHAMARI, PANCHAGARH, RANGPUR, THAKURGAON,
        // Sylhet
        HABIGANJ, MOULVIBAZAR, SUNAMGANJ, SYLHET
    }
}
