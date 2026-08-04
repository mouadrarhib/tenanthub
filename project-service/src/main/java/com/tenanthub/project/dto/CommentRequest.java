package com.tenanthub.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentRequest(
        @NotNull UUID authorUserId,
        @NotBlank String content
) {
}
