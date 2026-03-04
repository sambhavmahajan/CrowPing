package com.github.sambhavmahajan.crowping.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    @NonNull
    private String email;
    @NonNull
    private String password;
    @NonNull
    private String confirmPassword;
}
