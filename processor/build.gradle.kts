dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":library"))
  implementation("com.squareup:kotlinpoet:1.14.2")
  implementation("com.squareup:kotlinpoet-metadata:1.14.2")

  testImplementation("com.google.truth:truth:1.0.1")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
}

tasks {
  compileKotlin {
    kotlinOptions.moduleName = "vanilla-processor"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.DelicateKotlinPoetApi"
  }
  compileTestKotlin {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview"
  }
}
