package com.stablebridge.prreview.domain.model;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record PrDiff(
        String title,
        String description,
        String baseBranch,
        String headBranch,
        String diffContent,
        List<FileChange> files
) {}
