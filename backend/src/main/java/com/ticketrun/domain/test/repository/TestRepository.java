package com.ticketrun.domain.test.repository;

import com.ticketrun.domain.test.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {
}
