package com.example.application.repository;

import com.example.application.model.ApplicationUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    ApplicationUser findByUserName(String username);

    @Modifying
    @Transactional
    @Query("update ApplicationUser u set u.nickname = ?1 where u.userName = ?2")
    void setUserNickname(String nickname, String userId);
}
