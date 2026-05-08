package com.mini3.backend.domain.department.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_no")
    private Long deptNo;

    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;

    @Column(name = "dept_desc", length = 255)
    private String deptDesc;
}
