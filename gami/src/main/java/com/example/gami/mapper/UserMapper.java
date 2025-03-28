package com.example.gami.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.gami.dto.request.UserRequest;
import com.example.gami.dto.response.UserResponse;
import com.example.gami.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "checkins", ignore=true)
    @Mapping(target = "histories", ignore=true)
    @Mapping(target = "points", ignore=true)
    public User toUser(UserRequest request);

    public UserResponse toUserResponse(User user);

}
