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
    private String ownerEmail;
    @Override
    public int hashCode() {
        return 131*url.hashCode() +
                31*message.hashCode() + 7*timestamp.hashCode()
                + ownerEmail.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PingLog)) return false;
        if(!(message.equals(((PingLog) obj).getOwnerEmail()))) return false;
        if(!(url.equals(((PingLog) obj).url))) return false;
        if(!(timestamp.equals(((PingLog) obj).timestamp))) return false;
        return ownerEmail.equals(((PingLog) obj).getOwnerEmail());
    }
}
