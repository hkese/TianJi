pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "TianJi"
include(":android:app", ":android:core-domain", ":android:core-qimen", ":android:core-data", ":android:core-ui", ":android:feature-home", ":android:feature-chart", ":android:feature-goals", ":android:feature-planner", ":android:feature-journal", ":android:feature-settings")
