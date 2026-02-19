package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class IssueVote {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Issue issue;

    @ManyToOne
    private User user;
}
