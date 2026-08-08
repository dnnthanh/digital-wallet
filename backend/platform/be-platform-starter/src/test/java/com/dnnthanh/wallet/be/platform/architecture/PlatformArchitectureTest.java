package com.dnnthanh.wallet.be.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;

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

    @Test
    void platformConfigurationsMustBeComponentScannedInsteadOfAutoImported() {
        var classes = new ClassFileImporter().importPackages("com.dnnthanh.wallet.be.platform");
        noClasses()
                .that()
                .resideInAPackage("..platform.autoconfigure..")
                .should()
                .beAnnotatedWith(AutoConfiguration.class)
                .allowEmptyShould(true)
                .check(classes);
    }
}
