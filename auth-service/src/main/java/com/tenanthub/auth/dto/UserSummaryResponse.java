package com.tenanthub.auth.dto;

import java.util.UUID;

public record UserSummaryResponse(UUID id, String email) {
}
