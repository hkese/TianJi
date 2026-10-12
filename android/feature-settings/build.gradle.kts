plugins { id("com.android.library"); kotlin("android"); kotlin("plugin.serialization"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "com.tianji.featuresettings"
 compileSdk = 36
 defaultConfig { minSdk = 28; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner";  }
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
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test:runner:1.6.2")
}
