package com.stablebridge.prreview.fixtures;

import com.stablebridge.prreview.domain.model.PullRequestInput;

public final class PullRequestInputFixtures {

    public static final String OWNER = "acme";
    public static final String REPO = "webapp";
    public static final int PR_NUMBER = 42;

    private PullRequestInputFixtures() {}

    public static PullRequestInput aPullRequestInput() {
        return new PullRequestInput(OWNER, REPO, PR_NUMBER);
    }
}
