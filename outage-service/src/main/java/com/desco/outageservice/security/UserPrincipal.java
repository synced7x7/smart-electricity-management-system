package com.desco.outageservice.security;

import com.desco.outageservice.enums.Area;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal, Serializable {

    private UUID id;
    private String email;
    private String role;
    private Area area;

    @Override
    public String getName() {
        return email != null ? email : (id != null ? id.toString() : "");
    }
}
