package org.example.blockcalc.calc;

/** Access to one calculation given to one user. */
public class Grant {
    public String userId;
    public AccessLevel level;

    public Grant() {
    }

    public Grant(String userId, AccessLevel level) {
        this.userId = userId;
        this.level = level;
    }
}
