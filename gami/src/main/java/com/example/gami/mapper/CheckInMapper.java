package com.example.gami.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.gami.dto.response.AttendanceResponse;
import com.example.gami.model.CheckIn;

@Mapper(componentModel = "spring")
public interface CheckInMapper {

    @Mapping(target = "isCheckIn", source = "checkIn")
    AttendanceResponse toAttendanceResponse(CheckIn checkIn);

    default boolean isCheckIn(CheckIn checkIn) {
        return checkIn != null;
    }
}