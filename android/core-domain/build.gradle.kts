plugins { kotlin("jvm"); kotlin("plugin.serialization") }
kotlin { jvmToolchain(17) }
dependencies {
 implementation(project(":android:core-qimen"))
 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
 testImplementation("junit:junit:4.13.2")
}
