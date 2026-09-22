package com.smartspace.decorator.repository;

import com.smartspace.decorator.entity.DecoratorPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecoratorPackageRepository extends JpaRepository<DecoratorPackage, Long> {
    List<DecoratorPackage> findByDecoratorIdAndActiveTrue(Long decoratorId);
}
