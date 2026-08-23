package com.desco.userservice.dto.request;

import com.desco.userservice.enums.AreaName;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * All fields are optional here — a caller may update just one field at a time. A
 * field left null leaves the stored value unchanged (see
 * UserServiceImpl.updateOwnProfile), it does not clear it. `fullName` and `area`
 * are still required overall (the DB column is NOT NULL), but that's only enforced
 * on first creation — see UserServiceImpl for the exact rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be 7-15 digits, optionally starting with +")
    private String phoneNumber;

    private AreaName area;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 50, message = "Meter number must be at most 50 characters")
    private String meterNumber;

    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    private String avatarUrl;
}
