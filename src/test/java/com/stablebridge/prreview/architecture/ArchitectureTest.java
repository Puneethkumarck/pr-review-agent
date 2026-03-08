package com.stablebridge.prreview.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.stablebridge.prreview",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "..domain..";
    private static final String INFRASTRUCTURE = "..infrastructure..";
    private static final String AGENT = "..agent..";

    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                    .because("Domain must not depend on infrastructure (hexagonal rule)");

    @ArchTest
    static final ArchRule domainShouldNotDependOnAgent =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAPackage(AGENT)
                    .because("Domain must not depend on agent orchestration layer");

    @ArchTest
    static final ArchRule domainShouldNotUseSpringWeb =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.http..")
                    .because("Domain must not use Spring Web");

    @ArchTest
    static final ArchRule domainShouldNotUseWebClient =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework.web.reactive..")
                    .because("Domain must not use WebClient — that belongs in infrastructure");

    @ArchTest
    static final ArchRule infrastructureShouldNotDependOnAgent =
            noClasses().that().resideInAPackage(INFRASTRUCTURE)
                    .should().dependOnClassesThat().resideInAPackage(AGENT)
                    .because("Infrastructure must not depend on agent layer");
}
