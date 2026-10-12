plugins { id("com.android.library"); kotlin("android"); kotlin("plugin.serialization"); id("com.google.devtools.ksp") }
android {
 namespace = "com.tianji.coredata"
 compileSdk = 36
 defaultConfig { minSdk = 28; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner";  }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 
}
kotlin { jvmToolchain(17) }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
implementation(project(":android:core-domain"))
implementation(project(":android:core-qimen"))
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
implementation("androidx.core:core-ktx:1.16.0")
implementation("androidx.room:room-runtime:2.7.1")
implementation("androidx.room:room-ktx:2.7.1")
ksp("androidx.room:room-compiler:2.7.1")
implementation("androidx.datastore:datastore-preferences:1.1.4")
implementation("androidx.work:work-runtime-ktx:2.10.1")
api("com.squareup.okhttp3:okhttp:4.12.0")
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
testImplementation("com.squareup.okhttp3:okhttp-tls:4.12.0")
androidTestImplementation("androidx.room:room-testing:2.7.1")
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test:runner:1.6.2")
}
