package com.example.sparta_3rd_newsfeed.friend.dto.response;

import com.example.sparta_3rd_newsfeed.friend.entity.Friend;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FriendResponseDto {
    private final Long id;
    private final Long memberId;
    private final String name;
    private final String email;

    public FriendResponseDto(Friend friend) {
        this.id = friend.getId();
        this.memberId = friend.getSender().getId();
        this.name = friend.getSender().getUsername();
        this.email = friend.getSender().getEmail();
    }
}
