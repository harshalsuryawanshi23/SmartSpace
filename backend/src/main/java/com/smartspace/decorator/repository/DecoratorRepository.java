package com.smartspace.decorator.repository;

import com.smartspace.decorator.entity.Decorator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecoratorRepository extends JpaRepository<Decorator, Long> {
    Optional<Decorator> findByUserId(Long userId);
}