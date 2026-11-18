package org.example.blockcalc.user;

public enum Role {
    ADMIN, MANAGER, WORKER;

    /** Admin and manager share all management rights. */
    public boolean isStaff() {
        return this != WORKER;
    }
}
