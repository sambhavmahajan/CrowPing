package com.github.sambhavmahajan.crowping.dto;

import com.github.sambhavmahajan.crowping.exception.PasswordMismatchException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDTO {
    private String email;
    private String password;
    private String role;
    public AppUserDTO(RegisterDTO registerDTO) {
        if(!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }
        this.email = registerDTO.getEmail();
        this.password = registerDTO.getPassword();
        this.role = "USER";
    }
}
