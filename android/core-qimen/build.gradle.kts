plugins { kotlin("jvm"); kotlin("plugin.serialization") }
kotlin { jvmToolchain(17) }
dependencies {
 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
 implementation("cn.6tail:lunar:1.7.7")
 testImplementation("junit:junit:4.13.2")
}

tasks.test {
 systemProperty("tianji.contractFixture", rootProject.file("contracts/fixtures/blocked-chart.json").absolutePath)
 systemProperty("tianji.root", rootProject.projectDir.absolutePath)
}
