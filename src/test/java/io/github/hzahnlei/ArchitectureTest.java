package io.github.hzahnlei;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "io.github.hzahnlei", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule layerDependenciesAreRespected =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .optionalLayer("domain").definedBy("io.github.hzahnlei.domain..")
                    .optionalLayer("usecase").definedBy("io.github.hzahnlei.usecase..")
                    .optionalLayer("infra").definedBy("io.github.hzahnlei.infra..")
                    .whereLayer("domain").mayNotAccessAnyLayer()
                    .whereLayer("usecase").mayOnlyAccessLayers("domain")
                    .whereLayer("infra").mayOnlyAccessLayers("usecase", "domain");
}
