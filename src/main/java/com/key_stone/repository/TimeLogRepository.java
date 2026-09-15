package com.key_stone.repository;
import com.key_stone.domain.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TimeLogRepository extends JpaRepository<TimeLog,Long>{ }