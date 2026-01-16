package com.example.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomEntity {
    private String id;
    private String name;
    private String description;
    private Integer capacity;
    private String ownerId;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
