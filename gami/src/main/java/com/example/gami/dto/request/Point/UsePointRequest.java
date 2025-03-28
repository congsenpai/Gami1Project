package com.example.gami.dto.request.Point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
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
public class UsePointRequest {
    @NotEmpty(message = "User ID must not be empty")
    String userID;
    @Min(value = 0,message = "Min point at least 1")
    int points;
}
