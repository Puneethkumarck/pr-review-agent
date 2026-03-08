package com.stablebridge.prreview.domain.model;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record CodeAnalysis(
        List<CodeIssue> issues,
        List<ReviewSuggestion> suggestions,
        String overallAssessment
) {}
