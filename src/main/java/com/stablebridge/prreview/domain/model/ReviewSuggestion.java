package com.stablebridge.prreview.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ReviewSuggestion(
        String filename,
        String description,
        String suggestedCode
) {}
