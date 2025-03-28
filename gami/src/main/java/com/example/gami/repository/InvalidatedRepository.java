package com.example.gami.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.gami.model.InvalidToken;
@Repository
public interface InvalidatedRepository  extends JpaRepository<InvalidToken, String> {    
}
