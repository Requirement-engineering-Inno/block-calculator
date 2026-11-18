package org.example.blockcalc.calc;

import org.example.blockcalc.common.CurrentUser;

/** Who may do what with a calculation. Admin and manager have the same rights. */
public final class AccessPolicy {

    private AccessPolicy() {
    }

    public static boolean isAuthor(CurrentUser me, Calculation c) {
        return me.getId().equals(c.createdBy);
    }

    private static AccessLevel grantOf(CurrentUser me, Calculation c) {
        return c.grants.stream()
                .filter(g -> me.getId().equals(g.userId))
                .map(g -> g.level)
                .findFirst()
                .orElse(null);
    }

    public static boolean canView(CurrentUser me, Calculation c) {
        return me.isStaff() || isAuthor(me, c) || grantOf(me, c) != null;
    }

    /** Archive and restore. */
    public static boolean canWrite(CurrentUser me, Calculation c) {
        return me.isStaff() || isAuthor(me, c) || grantOf(me, c) == AccessLevel.READ_WRITE;
    }

    /** Change the inputs. A frozen calculation must be unfrozen first. */
    public static boolean canEdit(CurrentUser me, Calculation c) {
        return canWrite(me, c) && !c.frozen;
    }

    public static boolean canShare(CurrentUser me, Calculation c) {
        return me.isStaff() || isAuthor(me, c);
    }

    public static boolean canDelete(CurrentUser me, Calculation c) {
        return me.isStaff();
    }

    public static boolean canFreeze(CurrentUser me, Calculation c) {
        return me.isStaff();
    }
}
