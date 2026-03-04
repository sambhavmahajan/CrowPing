package com.github.sambhavmahajan.crowping.entity;


import com.github.sambhavmahajan.crowping.dto.AppUserDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class AppUser implements UserDetails {
    @Id
    @Column(unique = true)
    private String email;
    private String password;
    private String role;
    @OneToMany(mappedBy = "owner",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PingUrl> pingUrls;
    public AppUser(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;

    }
    public AppUser(AppUserDTO userDTO) {
        this.email = userDTO.getEmail();
        this.password = userDTO.getPassword();
        this.role = userDTO.getRole();
    }
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public @NonNull String getUsername() {
        return this.email;
    }
}