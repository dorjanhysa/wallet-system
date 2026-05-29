package com.wallet.auth.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User  extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String roles;

    @Column(nullable = false)
    private boolean enabled;

    public User(String username, String hashedPassword, String roles) {
        this.username = username;
        this.password = hashedPassword;
        this.roles = roles;
        this.enabled = true;
    }
}
