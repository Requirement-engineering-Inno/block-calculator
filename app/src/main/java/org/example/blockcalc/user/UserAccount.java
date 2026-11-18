package org.example.blockcalc.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("users")
public class UserAccount {
    @Id
    public String id;
    public String username;
    public String passwordHash;
    public String displayName;
    public Role role;
    public boolean active = true;
    public String createdBy;
    public Instant createdAt;
}
