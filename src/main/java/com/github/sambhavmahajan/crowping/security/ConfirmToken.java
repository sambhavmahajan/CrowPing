package com.github.sambhavmahajan.crowping.security;

import com.github.sambhavmahajan.crowping.entity.AppUser;
import com.github.sambhavmahajan.crowping.exception.ConfirmTokenExpiredException;
import com.github.sambhavmahajan.crowping.exception.UserAlreadyVerifiedException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConfirmToken {
    @Id
    private String token;
    private String userEmail;
    private LocalDateTime expiryDate;
    private boolean verified;
    public  ConfirmToken(String token, String userEmail) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiryDate = LocalDateTime.now().plusDays(1);
        verified = false;
    }
    public boolean confirm() throws RuntimeException {
        if(verified) {
            throw new UserAlreadyVerifiedException();
        }
        if(LocalDateTime.now().isAfter(expiryDate)) {
            throw new ConfirmTokenExpiredException();
        }
        return verified = true;
    }
}
