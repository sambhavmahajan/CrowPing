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
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private AppUser owner;
    private String url;
    private boolean active = true;
    public PingUrl(PingDTO dto, AppUser owner) {
        this.url = dto.getUrl();
        this.owner = owner;
    }
}
