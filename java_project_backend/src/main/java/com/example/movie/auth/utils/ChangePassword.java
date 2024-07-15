package com.example.movie.auth.utils;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChangePassword {
    private String password;
    private String repeatPassword;
}
