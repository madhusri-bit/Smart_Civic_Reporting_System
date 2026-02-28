package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class IssueReporter {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Issue issue;

    @ManyToOne
    private User reporter;

    private LocalDateTime createdAt;
}
