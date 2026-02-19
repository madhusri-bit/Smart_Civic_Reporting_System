package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Data
public class Assignment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Issue issue;

    @ManyToOne
    private User assignedTo;

    private LocalDateTime assignedAt;
}
