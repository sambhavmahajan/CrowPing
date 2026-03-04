package com.github.sambhavmahajan.crowping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class PingLog {
    @Id
    @GeneratedValue
    private Long id;
    private String message;
    @Column(columnDefinition = "TEXT")
    private String url;
    private LocalDateTime timestamp;
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser owner;
}
