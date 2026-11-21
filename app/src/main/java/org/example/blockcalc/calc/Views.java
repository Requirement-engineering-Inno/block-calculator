package org.example.blockcalc.calc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Read-only models for the templates. */
public final class Views {

    private Views() {
    }

    /** {@code total} and {@code currency} are null for workers: they see the number of blocks instead of money. */
    public record CalcRow(String id, String title, String project, boolean projectSystem, String author,
                          Instant updatedAt, boolean archived, boolean frozen, String total, String currency,
                          String blocks) {
    }

    public record GrantRow(String userId, String username, String name, AccessLevel level) {
    }

    public record CalcDetail(
            String id, String title, String projectId, String projectName, boolean projectSystem,
            boolean frozen, boolean archived,
            int fullBlocks, int halfBlocks, int batch, BigDecimal ws, boolean roundUp,
            String author, Instant createdAt, String updatedBy, Instant updatedAt,
            CalcResult result,
            boolean canEdit, boolean canWrite, boolean canShare, boolean canDelete, boolean canFreeze,
            boolean canShareAll,
            boolean showMoney,
            List<GrantRow> grants) {
    }

    /** Values shown in the calculation form. */
    public record CalcForm(String id, String projectId, String title, Integer fullBlocks, Integer halfBlocks,
                           Integer batch, BigDecimal ws, boolean roundUp) {
    }
}
