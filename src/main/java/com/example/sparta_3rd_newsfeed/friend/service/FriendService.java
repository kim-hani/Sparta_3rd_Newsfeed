package com.example.sparta_3rd_newsfeed.friend.service;

import com.example.sparta_3rd_newsfeed.member.entity.Member;
import com.example.sparta_3rd_newsfeed.member.repository.MemberRepository;
import com.example.sparta_3rd_newsfeed.friend.entity.FriendStatus;
import com.example.sparta_3rd_newsfeed.friend.dto.request.StatusUpdateRequestDto;
import com.example.sparta_3rd_newsfeed.friend.dto.response.FriendResponseDto;
import com.example.sparta_3rd_newsfeed.friend.dto.response.PageResponseDto;
import com.example.sparta_3rd_newsfeed.friend.entity.Friend;
import com.example.sparta_3rd_newsfeed.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void sendRequest(Long memberId, Long receiverId) {
        if (memberId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        Member sender = memberRepository.findByIdOrElseThrow(memberId);
        Member receiver = memberRepository.findByIdOrElseThrow(receiverId);

        Optional<Friend> existingFriend = friendRepository.findBySenderAndReceiver(sender, receiver);

        if(existingFriend.isPresent()) {
            Friend friend = existingFriend.get();
            if (friend.getStatus() == FriendStatus.REJECTED) {
                friend.updateStatus(FriendStatus.PENDING);
                friendRepository.save(friend);
                return;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 친구 요청을 보냈습니다.");
        }

        Friend friend = new Friend(sender, receiver);
        friendRepository.save(friend);
    }

    @Transactional
    public void updateStatus(Long memberId, Long friendId, StatusUpdateRequestDto request) {
        Friend friend = friendRepository.findByIdOrElseThrow(friendId);

        if(!memberId.equals(friend.getReceiver().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 친구 요청만 수락/거절할 수 있습니다.");
        }

        friend.updateStatus(request.getStatus());
    }

    @Transactional(readOnly = true)
    public PageResponseDto<FriendResponseDto> getPendingRequests(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Friend> friends = friendRepository.findPendingRequests(memberId, pageable);

        Page<FriendResponseDto> friendsDto = friends.map(FriendResponseDto::new);

        return new PageResponseDto<>(friendsDto);
    }

    // 팔로워 목록 (나를 팔로우하는 사람들)
    @Transactional(readOnly = true)
    public PageResponseDto<FriendResponseDto> getFollowers(Long memberId, String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member receiver = memberRepository.findByIdOrElseThrow(memberId);
        Page<Friend> friends;

        if (name != null && !name.isBlank()) {
            friends = friendRepository.findByReceiverAndName(receiver.getId(), name, pageable);
        } else {
            friends = friendRepository.findByReceiver(receiver.getId(), pageable);
        }

        Page<FriendResponseDto> friendsDto = friends.map(f -> new FriendResponseDto(f.getId(), f.getSender().getId(), f.getSender().getUsername(), f.getSender().getEmail()));

        return new PageResponseDto<>(friendsDto);
    }

    // 팔로잉 목록 (내가 팔로우하는 사람들)
    @Transactional(readOnly = true)
    public PageResponseDto<FriendResponseDto> getFollowings(Long memberId, String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member sender = memberRepository.findByIdOrElseThrow(memberId);
        Page<Friend> friends;

        if (name != null && !name.isBlank()) {
            friends = friendRepository.findBySenderAndName(sender.getId(), name, pageable);
        } else {
            friends = friendRepository.findBySender(sender.getId(), pageable);
        }

        Page<FriendResponseDto> friendsDto = friends.map(f -> new FriendResponseDto(f.getId(), f.getReceiver().getId(), f.getReceiver().getUsername(), f.getReceiver().getEmail()));

        return new PageResponseDto<>(friendsDto);
    }

    @Transactional
    public void delete(Long memberId, Long receiverId) {
        Member sender = memberRepository.findByIdOrElseThrow(memberId);
        Member receiver = memberRepository.findByIdOrElseThrow(receiverId);

        Optional<Friend> friend = friendRepository.findBySenderAndReceiver(sender, receiver);
        if (friend.isPresent() && friend.get().getStatus() == FriendStatus.ACCEPTED) {
            friendRepository.delete(friend.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "친구가 아닙니다.");
        }
    }
}
