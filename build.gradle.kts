// Root aggregator for IDE import when workspace is opened at e2eBPM/.
tasks.register("build") {
    dependsOn(":e2e-event-generator:build")
}

tasks.register("check") {
    dependsOn(":e2e-event-generator:check")
}
