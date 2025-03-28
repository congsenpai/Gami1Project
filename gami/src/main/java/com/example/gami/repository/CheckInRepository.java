package com.example.gami.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.gami.model.CheckIn;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, String> {
    boolean existsByUser_UserIDAndCheckinDate(String userID, LocalDate checkinDate);
    List<CheckIn> findByUser_UserID(String userId);
    @Query("SELECT c FROM CheckIn c WHERE c.user.userID = :userId AND c.checkinDate BETWEEN :startDate AND :endDate")
    List<CheckIn> findByUser_UserIDAndCheckinDateBetween(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    long countByUser_UserIDAndCheckinDateBetween(String userID, LocalDate start, LocalDate end);

}
