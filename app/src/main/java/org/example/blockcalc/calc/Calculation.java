package org.example.blockcalc.calc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Stores only the inputs; the result is always computed from them and the (current or frozen) prices. */
@Document("calculations")
public class Calculation {
    @Id
    public String id;
    public String projectId;
    public String title;
    public int fullBlocks;
    public int halfBlocks;
    public int batch;
    public BigDecimal ws;
    public boolean roundUp;
    public boolean frozen;
    public boolean archived;
    public PriceSnapshot snapshot;
    public String createdBy;
    public Instant createdAt;
    public String updatedBy;
    public Instant updatedAt;
    public List<Grant> grants = new ArrayList<>();
}
