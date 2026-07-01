package kr.or.hieating.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record User(
    Long id,
    String email,
    String password,
    String name,
    String gender,
    LocalDate birth,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt) {}
