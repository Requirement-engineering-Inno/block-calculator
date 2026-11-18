package org.example.blockcalc.common;

import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserAccount;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/** Spring Security principal carrying the application user's id and role. */
public class CurrentUser extends User {
    private final String id;
    private final String displayName;
    private final Role role;

    public CurrentUser(UserAccount a) {
        super(a.username, a.passwordHash, a.active, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + a.role.name())));
        this.id = a.id;
        this.displayName = a.displayName;
        this.role = a.role;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isStaff() {
        return role.isStaff();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
