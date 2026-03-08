package com.stablebridge.prreview.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record CompletedReview(
        PullRequestInput pullRequest,
        CodeAnalysis analysis,
        String reviewBody,
        boolean posted
) {}
