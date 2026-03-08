package com.stablebridge.prreview.application.controller;

import com.stablebridge.prreview.domain.port.PullRequestProvider;
import com.stablebridge.prreview.domain.service.ReviewFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.stablebridge.prreview.fixtures.PrDiffFixtures.aPrDiff;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.OWNER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.PR_NUMBER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.REPO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private PullRequestProvider pullRequestProvider;

    @Mock
    private ReviewFormatter reviewFormatter;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        var controller = new ReviewController(pullRequestProvider, reviewFormatter);
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void shouldCreateReviewAndReturnResponse() {
        // given
        var diff = aPrDiff();
        var reviewBody = "## PR Summary\nFormatted content";

        given(pullRequestProvider.fetchPullRequest(OWNER, REPO, PR_NUMBER))
                .willReturn(diff);
        given(reviewFormatter.formatSummary(diff))
                .willReturn(reviewBody);
        given(pullRequestProvider.postReview(OWNER, REPO, PR_NUMBER, reviewBody))
                .willReturn(true);

        // when
        var result = client.post()
                .uri("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ReviewRequest(OWNER, REPO, PR_NUMBER))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReviewResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        var expected = ReviewResponse.builder()
                .owner(OWNER)
                .repo(REPO)
                .prNumber(PR_NUMBER)
                .title(diff.title())
                .filesChanged(2)
                .reviewBody(reviewBody)
                .posted(true)
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        then(pullRequestProvider).should().postReview(OWNER, REPO, PR_NUMBER, reviewBody);
    }

    @Test
    void shouldReturnPostedFalseWhenGitHubFails() {
        // given
        var diff = aPrDiff();
        var reviewBody = "## PR Summary";

        given(pullRequestProvider.fetchPullRequest(OWNER, REPO, PR_NUMBER))
                .willReturn(diff);
        given(reviewFormatter.formatSummary(diff))
                .willReturn(reviewBody);
        given(pullRequestProvider.postReview(OWNER, REPO, PR_NUMBER, reviewBody))
                .willReturn(false);

        // when
        var result = client.post()
                .uri("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ReviewRequest(OWNER, REPO, PR_NUMBER))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReviewResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(result.posted()).isFalse();
    }
}
