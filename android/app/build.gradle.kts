plugins { id("com.android.application"); kotlin("android"); kotlin("plugin.serialization"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "com.tianji.app"
 compileSdk = 36
 defaultConfig { minSdk = 28; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; applicationId = "com.tianji.navigator.daily"; targetSdk = 36; versionCode = 6; versionName = "0.4.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildFeatures { compose = true }
}
kotlin { jvmToolchain(17) }

dependencies {
implementation(project(":android:core-domain"))
implementation(project(":android:core-qimen"))
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
implementation("androidx.core:core-ktx:1.16.0")
implementation(platform("androidx.compose:compose-bom:2025.04.01"))
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-core")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.activity:activity-compose:1.10.1")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
implementation(project(":android:core-data"))
implementation(project(":android:core-ui"))
implementation(project(":android:feature-home"))
implementation(project(":android:feature-chart"))
implementation(project(":android:feature-goals"))
implementation(project(":android:feature-planner"))
implementation(project(":android:feature-journal"))
implementation(project(":android:feature-settings"))
implementation("androidx.navigation:navigation-compose:2.9.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
implementation("io.insert-koin:koin-android:4.0.4")
androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test:runner:1.6.2")
}
