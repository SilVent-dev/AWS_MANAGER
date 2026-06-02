package com.awsmanager.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Ec2OperationLogRepository extends JpaRepository<Ec2OperationLog, UUID> {
    Page<Ec2OperationLog> findAllByOrderByExecutedAtDesc(Pageable pageable);
    List<Ec2OperationLog> findByInstanceIdOrderByExecutedAtDesc(String instanceId);
}
