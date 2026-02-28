package com.civic.reporting.model;

import com.civic.reporting.model.enumFolder.WorkflowStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class IssueStatusHistory {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Issue issue;

    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    @Column(length = 512)
    private String note;

    private LocalDateTime createdAt;
}
