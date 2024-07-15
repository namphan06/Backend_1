package com.example.movie.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MailBody {
    private String to;
    private String subject;
    private String text;
}
