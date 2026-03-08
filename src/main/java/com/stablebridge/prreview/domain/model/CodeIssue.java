package com.stablebridge.prreview.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record CodeIssue(
        String filename,
        IssueSeverity severity,
        IssueCategory category,
        String description,
        String lineReference
) {}
