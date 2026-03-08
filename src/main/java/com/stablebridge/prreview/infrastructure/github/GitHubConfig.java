package com.stablebridge.prreview.infrastructure.github;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(GitHubProperties.class)
class GitHubConfig {

    @Bean
    WebClient gitHubWebClient(GitHubProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.apiUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }
}
