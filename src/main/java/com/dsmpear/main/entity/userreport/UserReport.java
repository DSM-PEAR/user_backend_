package com.dsmpear.main.entity.userreport;


import com.dsmpear.main.entity.report.Report;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "user_report_tbl")
public class UserReport {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private String userEmail;

    @Column(name = "report_id", nullable = false)
    private Integer reportId;
    @ManyToOne
    @JsonBackReference
    private Report members;

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "report_id")
    private Report report;
}
