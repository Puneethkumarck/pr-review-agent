package com.stablebridge.prreview.shell;

import com.stablebridge.prreview.domain.port.PullRequestProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.stablebridge.prreview.fixtures.PrDiffFixtures.aPrDiff;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.OWNER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.PR_NUMBER;
import static com.stablebridge.prreview.fixtures.PullRequestInputFixtures.REPO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewCommandsTest {

    @Mock
    private PullRequestProvider pullRequestProvider;

    private ReviewCommands commands;

    @BeforeEach
    void setUp() {
        commands = new ReviewCommands(pullRequestProvider);
    }

    @Test
    void shouldFetchPrAndDisplaySummary() {
        // given
        var diff = aPrDiff();
        given(pullRequestProvider.fetchPullRequest(OWNER, REPO, PR_NUMBER))
                .willReturn(diff);

        // when
        var result = commands.fetchPr(OWNER, REPO, PR_NUMBER);

        // then
        assertThat(result)
                .contains("PR #42")
                .contains("Fix null pointer in payment processing")
                .contains("fix/npe-merchant-config")
                .contains("main")
                .contains("Files changed: 2");

        then(pullRequestProvider).should().fetchPullRequest(OWNER, REPO, PR_NUMBER);
    }
}
