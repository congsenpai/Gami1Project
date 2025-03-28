package com.example.gami.mapper;

import org.mapstruct.Mapper;

import com.example.gami.dto.response.PointHistoryResponse;
import com.example.gami.model.PointHistory;

@Mapper(componentModel="spring")
public interface PointHistoryMapper {
    public PointHistoryResponse toPointHistoryResponse(PointHistory pointHistory);
}
