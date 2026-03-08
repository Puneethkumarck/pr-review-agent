package com.stablebridge.prreview.domain.model;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record PrDiff(
        String title,
        String description,
        String baseBranch,
        String headBranch,
        String diffContent,
        List<FileChange> files
) {}
