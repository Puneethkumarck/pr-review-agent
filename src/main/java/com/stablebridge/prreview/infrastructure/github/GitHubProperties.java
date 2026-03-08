package com.stablebridge.prreview.infrastructure.github;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GitHubProperties(
        String token,
        String apiUrl
) {
    public GitHubProperties {
        if (apiUrl == null || apiUrl.isBlank()) {
            apiUrl = "https://api.github.com";
        }
    }
}
