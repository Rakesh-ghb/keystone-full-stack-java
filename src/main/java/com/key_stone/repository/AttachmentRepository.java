package com.key_stone.repository;
import com.key_stone.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AttachmentRepository extends JpaRepository<Attachment,Long>{ }