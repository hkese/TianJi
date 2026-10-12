# Architecture
`core-domain` contains serializable records, integrity validation and the bounded offline planner. `core-qimen` is a pure Kotlin deterministic ruleset interface and blocked implementation with canonical hashing, bounded cache, batch and comparison hooks.

`core-data` owns Room, foreign keys, schema 1→2 migration, transactional backup/restore, DataStore, Keystore, WorkManager and strict HTTPS AI comparison. The original v1 schema was recovered from the installed app database. No destructive migration fallback is enabled. Existing chart JSON is preserved; new charts use a versioned serialization format.

Feature modules expose state-driven Compose screens. `app` wires Navigation, a lifecycle ViewModel, Flow, coroutines and Koin. Data operations execute on Dispatchers.IO; screens consume one combined state. `core-ui` shares Material 3 themes, accessible controls, responsive width and bilingual text.

The Python Lab imports only user-exported data, validates Pydantic/JSON schemas and stores canonical content-addressed datasets. Training/evaluation never executes an imported model artifact. All model outputs remain RESEARCH_ONLY and registry entries are non-deployable metadata.
