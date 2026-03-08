package com.stablebridge.prreview.domain.port;

import com.stablebridge.prreview.domain.model.PrDiff;

public interface PullRequestProvider {

    PrDiff fetchPullRequest(String owner, String repo, int prNumber);

    boolean postReview(String owner, String repo, int prNumber, String body);
}
