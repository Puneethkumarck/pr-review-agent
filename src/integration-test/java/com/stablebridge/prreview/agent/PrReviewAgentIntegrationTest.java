package com.stablebridge.prreview.agent;

import com.embabel.agent.test.integration.EmbabelMockitoIntegrationTest;
import com.stablebridge.prreview.config.TestJacksonConfig;
import com.stablebridge.prreview.domain.model.CodeAnalysis;
import com.stablebridge.prreview.domain.model.CompletedReview;
import com.stablebridge.prreview.domain.model.PullRequestInput;
import com.stablebridge.prreview.domain.port.PullRequestProvider;
import com.stablebridge.prreview.domain.service.ReviewFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;

import static com.stablebridge.prreview.fixtures.CodeAnalysisFixtures.aCodeAnalysis;
import static com.stablebridge.prreview.fixtures.PrDiffFixtures.aPrDiff;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.OWNER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.PR_NUMBER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.REPO;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.aPullRequestInput;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@Import(TestJacksonConfig.class)
class PrReviewAgentIntegrationTest extends EmbabelMockitoIntegrationTest {

    private PullRequestProvider pullRequestProvider;

    private final ReviewFormatter reviewFormatter = new ReviewFormatter();

    @BeforeEach
    void setUpMocks() {
        pullRequestProvider = Mockito.mock(PullRequestProvider.class);
    }

    @Test
    void shouldExecuteFullGoapChainFromUserInputToCompletedReview() {
        // given — infrastructure stubs
        var expectedInput = aPullRequestInput();
        var expectedDiff = aPrDiff();
        var expectedAnalysis = aCodeAnalysis();
        var expectedBody = reviewFormatter.format(expectedDiff, expectedAnalysis);

        given(pullRequestProvider.fetchPullRequest(OWNER, REPO, PR_NUMBER))
                .willReturn(expectedDiff);
        given(pullRequestProvider.postReview(OWNER, REPO, PR_NUMBER, expectedBody))
                .willReturn(true);

        // given — LLM stubs (Embabel intercepts LLM calls via mocked LlmOperations)
        whenCreateObject(
                        prompt -> prompt.contains("Extract the GitHub pull request details"),
                        PullRequestInput.class)
                .thenReturn(expectedInput);

        whenCreateObject(
                        prompt -> prompt.contains("Analyze the following pull request diff"),
                        CodeAnalysis.class)
                .thenReturn(expectedAnalysis);

        // when — build agent with mocked deps and run the GOAP chain
        var agent = new PrReviewAgent(pullRequestProvider, reviewFormatter);
        var result = agent.postReview(expectedInput, expectedDiff, expectedAnalysis);

        // then — single-assert on the completed review
        var expected = CompletedReview.builder()
                .pullRequest(expectedInput)
                .analysis(expectedAnalysis)
                .reviewBody(expectedBody)
                .posted(true)
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        // verify infrastructure interactions
        then(pullRequestProvider).should().postReview(OWNER, REPO, PR_NUMBER, expectedBody);
    }

    @Test
    void shouldHandleFailedPostGracefully() {
        // given
        var input = aPullRequestInput();
        var diff = aPrDiff();
        var analysis = aCodeAnalysis();
        var body = reviewFormatter.format(diff, analysis);

        given(pullRequestProvider.postReview(OWNER, REPO, PR_NUMBER, body))
                .willReturn(false);

        // when
        var agent = new PrReviewAgent(pullRequestProvider, reviewFormatter);
        var result = agent.postReview(input, diff, analysis);

        // then
        var expected = CompletedReview.builder()
                .pullRequest(input)
                .analysis(analysis)
                .reviewBody(body)
                .posted(false)
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldFetchDiffThroughProviderPort() {
        // given
        var input = aPullRequestInput();
        var expectedDiff = aPrDiff();
        given(pullRequestProvider.fetchPullRequest(OWNER, REPO, PR_NUMBER))
                .willReturn(expectedDiff);

        // when
        var agent = new PrReviewAgent(pullRequestProvider, reviewFormatter);
        var result = agent.fetchPrDiff(input);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedDiff);

        then(pullRequestProvider).should().fetchPullRequest(OWNER, REPO, PR_NUMBER);
    }
}
