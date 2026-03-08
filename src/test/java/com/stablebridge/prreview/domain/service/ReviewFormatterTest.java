package com.stablebridge.prreview.domain.service;

import static com.stablebridge.prreview.fixtures.CodeAnalysisFixtures.aCleanAnalysis;
import static com.stablebridge.prreview.fixtures.CodeAnalysisFixtures.aCodeAnalysis;
import static com.stablebridge.prreview.fixtures.PrDiffFixtures.aPrDiff;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReviewFormatterTest {

    private final ReviewFormatter formatter = new ReviewFormatter();

    @Test
    void shouldFormatReviewWithIssuesAndSuggestions() {
        // given
        var diff = aPrDiff();
        var analysis = aCodeAnalysis();

        // when
        var result = formatter.format(diff, analysis);

        // then
        assertThat(result)
                .contains("## AI Code Review: Fix null pointer")
                .contains("[INFO]")
                .contains("Service.java")
                .contains("Optional")
                .contains("Ready to merge");
    }

    @Test
    void shouldFormatCleanReviewWithoutIssueSection() {
        // given
        var diff = aPrDiff();
        var analysis = aCleanAnalysis();

        // when
        var result = formatter.format(diff, analysis);

        // then
        assertThat(result)
                .contains("## AI Code Review")
                .contains("Approve")
                .doesNotContain("### Issues Found")
                .doesNotContain("### Suggestions");
    }

    @Test
    void shouldIncludeSuggestedCodeSnippets() {
        // given
        var diff = aPrDiff();
        var analysis = aCodeAnalysis();

        // when
        var result = formatter.format(diff, analysis);

        // then
        assertThat(result)
                .contains("Optional.ofNullable(config)")
                .contains("```");
    }
}
