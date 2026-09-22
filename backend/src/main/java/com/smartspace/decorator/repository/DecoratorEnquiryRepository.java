package com.smartspace.decorator.repository;

import com.smartspace.decorator.entity.DecoratorEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DecoratorEnquiryRepository extends JpaRepository<DecoratorEnquiry, Long> {
    Optional<DecoratorEnquiry> findByPublicId(String publicId);
    List<DecoratorEnquiry> findByDecoratorId(Long decoratorId);
    List<DecoratorEnquiry> findByRenterUserId(Long renterUserId);
}
