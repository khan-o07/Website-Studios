package com.websitestudios.entity;

import com.websitestudios.enums.ProjectTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_service_types", indexes = {
        @Index(name = "idx_pst_request_id", columnList = "project_request_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pst_project_request"))
    private ProjectRequest projectRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 30)
    private ProjectTypeEnum projectType;
}