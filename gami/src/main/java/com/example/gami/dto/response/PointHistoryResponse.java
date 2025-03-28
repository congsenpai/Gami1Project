package com.example.gami.dto.response;

import java.time.LocalDate;

import com.example.gami.enums.ActionType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PointHistoryResponse {
    ActionType action;
    int pointChange;
    LocalDate actionDate;
}
