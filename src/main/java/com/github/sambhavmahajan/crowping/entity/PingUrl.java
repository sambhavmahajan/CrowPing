package com.github.sambhavmahajan.crowping.entity;

import com.github.sambhavmahajan.crowping.dto.PingDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class PingUrl {
    @Id
    @GeneratedValue
    private long id;
    private String ownerEmail;
    private String url;
    private boolean active = true;
    public PingUrl(PingDTO dto, String ownerEmail) {
        this.url = dto.getUrl();
        this.ownerEmail =  ownerEmail;
    }
}
