package com.smartspace;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.smartspace")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_must_reside_in_controller_package =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule no_direct_entity_return_from_controller =
            noClasses().that().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Entity");

    @ArchTest
    static final ArchRule ban_instant_now =
            noClasses().should().callMethod(Instant.class, "now")
                    .andShould().callMethod(LocalDateTime.class, "now")
                    .andShould().callMethod(LocalDate.class, "now")
                    .andShould().callMethod(ZonedDateTime.class, "now")
                    .andShould().callMethod(OffsetDateTime.class, "now")
                    .because("You must use java.time.Clock bean to get current time for testability.");
}
