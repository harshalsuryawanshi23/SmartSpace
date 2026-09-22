package com.smartspace.entry.repository;

import com.smartspace.entry.entity.EntryCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntryCredentialRepository extends JpaRepository<EntryCredential, Long> {
    Optional<EntryCredential> findByJti(String jti);
}
