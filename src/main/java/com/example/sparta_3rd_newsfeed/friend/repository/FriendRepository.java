package com.example.sparta_3rd_newsfeed.friend.repository;

import com.example.sparta_3rd_newsfeed.member.entity.Member;
import com.example.sparta_3rd_newsfeed.friend.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // ID로 친구 요청 찾기 (없으면 예외 발생)
    default Friend findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "친구 요청이 존재하지 않습니다."));
    }

    // 친구 요청 찾기 (보낸 사람 & 받는 사람 기준)
    Optional<Friend> findBySenderAndReceiver(Member sender, Member receiver);

    // 특정 사용자의 친구 목록 조회 (친구 상태: ACCEPTED)
    @Query("SELECT f FROM Friend f JOIN FETCH f.sender JOIN FETCH f.receiver WHERE f.receiver.id = :receiverId AND f.status = 'ACCEPTED'")
    Page<Friend> findByReceiver(Long receiverId, Pageable pageable);

    // 특정 사용자의 친구 목록에서 이름 검색
    @Query("SELECT f FROM Friend f JOIN FETCH f.sender JOIN FETCH f.receiver WHERE f.receiver.id = :receiverId AND f.status = 'ACCEPTED' AND f.sender.username LIKE %:name%")
    Page<Friend> findByReceiverAndName(Long receiverId, String name, Pageable pageable);

    // 사용자가 친구 요청을 보낸 목록 조회 (친구 상태: ACCEPTED)
    @Query("SELECT f FROM Friend f JOIN FETCH f.sender JOIN FETCH f.receiver WHERE f.sender.id = :senderId AND f.status = 'ACCEPTED'")
    Page<Friend> findBySender(Long senderId, Pageable pageable);

    // 사용자가 친구 요청을 보낸 목록에서 이름 검색
    @Query("SELECT f FROM Friend f JOIN FETCH f.sender JOIN FETCH f.receiver WHERE f.sender.id = :senderId AND f.status = 'ACCEPTED' AND f.receiver.username LIKE %:name%")
    Page<Friend> findBySenderAndName(Long senderId, String name, Pageable pageable);

    // 사용자가 받은 친구 요청 목록 조회 (친구 상태: PENDING)
    @Query("SELECT f FROM Friend f JOIN FETCH f.sender JOIN FETCH f.receiver WHERE f.receiver.id = :memberId AND f.status = 'PENDING'")
    Page<Friend> findPendingRequests(Long memberId, Pageable pageable);

}
