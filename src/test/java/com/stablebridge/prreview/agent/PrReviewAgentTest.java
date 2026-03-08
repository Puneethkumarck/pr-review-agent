package com.stablebridge.prreview.agent;

import static com.stablebridge.prreview.fixtures.CodeAnalysisFixtures.aCodeAnalysis;
import static com.stablebridge.prreview.fixtures.PrDiffFixtures.aPrDiff;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.OWNER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.PR_NUMBER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.REPO;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.aPullRequestInput;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.stablebridge.prreview.domain.model.CompletedReview;
import com.stablebridge.prreview.domain.port.PullRequestProvider;
import com.stablebridge.prreview.domain.service.ReviewFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrReviewAgentTest {

    @Mock
    private PullRequestProvider pullRequestProvider;

    @Mock
    private ReviewFormatter reviewFormatter;

    private PrReviewAgent agent;

    @BeforeEach
    void setUp() {
        agent = new PrReviewAgent(pullRequestProvider, reviewFormatter);
    }

    @Test
    void shouldFetchPrDiffFromProvider() {
        // given
        var input = aPullRequestInput();
        var expectedDiff = aPrDiff();
        given(pullRequestProvider.fetchPullRequest(OWNER, REPO, PR_NUMBER))
                .willReturn(expectedDiff);

        // when
        var result = agent.fetchPrDiff(input);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedDiff);
    }

    @Test
    void shouldPostReviewAndReturnCompletedReview() {
        // given
        var input = aPullRequestInput();
        var diff = aPrDiff();
        var analysis = aCodeAnalysis();
        var formattedBody = "## AI Code Review\nFormatted content";

        given(reviewFormatter.format(diff, analysis)).willReturn(formattedBody);
        given(pullRequestProvider.postReview(OWNER, REPO, PR_NUMBER, formattedBody))
                .willReturn(true);

        // when
        var result = agent.postReview(input, diff, analysis);

        // then
        var expected = CompletedReview.builder()
                .pullRequest(input)
                .analysis(analysis)
                .reviewBody(formattedBody)
                .posted(true)
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnPostedFalseWhenGitHubPostFails() {
        // given
        var input = aPullRequestInput();
        var diff = aPrDiff();
        var analysis = aCodeAnalysis();
        var formattedBody = "review body";

        given(reviewFormatter.format(diff, analysis)).willReturn(formattedBody);
        given(pullRequestProvider.postReview(OWNER, REPO, PR_NUMBER, formattedBody))
                .willReturn(false);

        // when
        var result = agent.postReview(input, diff, analysis);

        // then
        assertThat(result.posted()).isFalse();
    }
}
