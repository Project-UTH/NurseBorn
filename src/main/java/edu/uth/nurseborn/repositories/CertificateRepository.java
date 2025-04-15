package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByNurseProfileUserUserId(Long userId);
}