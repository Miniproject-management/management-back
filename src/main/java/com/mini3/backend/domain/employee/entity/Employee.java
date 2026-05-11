package com.mini3.backend.domain.employee.entity;

import com.mini3.backend.domain.employee.enums.Position;
import com.mini3.backend.domain.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "emp_name", nullable = false, length = 100)
    private String empName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_no", nullable = false)
    private Department department;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private Position position;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "password", nullable = false, length = 255)
    private String password;
}