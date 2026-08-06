package com.dnnthanh.wallet.be.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class PlatformArchitectureTest {
    @Test
    void domainAndApplicationMustNotDependOnAdaptersOrInfrastructure() {
        var classes = new ClassFileImporter().importPackages("com.dnnthanh.wallet.be");
        noClasses()
                .that()
                .resideInAnyPackage("..domain..", "..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..adapter..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }
}
