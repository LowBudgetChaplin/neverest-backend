package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.UserEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByQrCode(String qrCode);

    Optional<UserEntity> findByAuthSubject(String authSubject);

    @Query("select u from UserEntity u where lower(u.authSubject) = lower(:authSubject)")
    Optional<UserEntity> findByAuthSubjectIgnoreCase(@Param("authSubject") String authSubject);

    boolean existsByAuthSubject(String authSubject);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findByIdForUpdate(@Param("id") UUID id);
}
