package com.example.sparta_3rd_newsfeed.member.repository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.example.sparta_3rd_newsfeed.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    
     Optional<Member> findByEmailAndIsDeletedFalse(String email);

    default Member findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."));
    }
}

