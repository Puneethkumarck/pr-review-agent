package com.stablebridge.prreview.domain.model;

public record PullRequestInput(
        String owner,
        String repo,
        int prNumber
) {}
