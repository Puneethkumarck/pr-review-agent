package com.stablebridge.prreview.infrastructure.github;

import com.stablebridge.prreview.domain.model.FileChange;
import com.stablebridge.prreview.domain.model.PrDiff;
import com.stablebridge.prreview.domain.port.PullRequestProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class GitHubPullRequestAdapter implements PullRequestProvider {

    private final WebClient gitHubWebClient;

    @Override
    public PrDiff fetchPullRequest(String owner, String repo, int prNumber) {
        log.info("Fetching PR #{} from {}/{}", prNumber, owner, repo);

        var prJson = gitHubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{number}", owner, repo, prNumber)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        var filesJson = gitHubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{number}/files", owner, repo, prNumber)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .collectList()
                .block();

        List<FileChange> files = filesJson.stream()
                .map(this::toFileChange)
                .toList();

        var diffContent = gitHubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{number}", owner, repo, prNumber)
                .accept(MediaType.valueOf("application/vnd.github.v3.diff"))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return PrDiff.builder()
                .title(prJson.get("title").asText())
                .description(prJson.has("body") ? prJson.get("body").asText("") : "")
                .baseBranch(prJson.get("base").get("ref").asText())
                .headBranch(prJson.get("head").get("ref").asText())
                .diffContent(diffContent)
                .files(files)
                .build();
    }

    @Override
    public boolean postReview(String owner, String repo, int prNumber, String body) {
        log.info("Posting review to PR #{} on {}/{}", prNumber, owner, repo);
        try {
            gitHubWebClient.post()
                    .uri("/repos/{owner}/{repo}/pulls/{number}/reviews",
                            owner, repo, prNumber)
                    .bodyValue(Map.of("body", body, "event", "COMMENT"))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to post review to PR #{} on {}/{}", prNumber, owner, repo, e);
            return false;
        }
    }

    private FileChange toFileChange(JsonNode node) {
        return FileChange.builder()
                .filename(node.get("filename").asText())
                .status(node.get("status").asText())
                .additions(node.get("additions").asInt())
                .deletions(node.get("deletions").asInt())
                .patch(node.has("patch") ? node.get("patch").asText() : null)
                .build();
    }
}
