package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByToken(String token);
}