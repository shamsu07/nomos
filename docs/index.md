<p align="center">
  <img alt="nomos logo" src="assets/nomos.png" width="300">
</p>
<h1 align="center">
  A fast, modern rule engine for Java.
</h1>

<p align="center">
  <img alt="License" src="https://img.shields.io/badge/license-Apache_2.0-blue.svg?style=flat-square">
  <img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/shamsu07/nomos/build.yaml?branch=main&style=flat-square">
</p>

`nomos` is a modern, lightweight, and developer-first rule engine for Java and Spring Boot. It's designed to be the simple, transparent, and fast alternative for 95% of use cases where traditional rule engines (like Drools) are heavyweight and complex.

---

### 💡 Why nomos?

Traditional rule engines are powerful but often come with:
* A steep learning curve.
* Heavy memory footprints and slow startup times.
* Complex, proprietary UIs and XML-based configuration.
* A "black box" execution model that's hard to debug.

`nomos` is different. Our philosophy is **developer-centric, simple, and transparent.**

* ✅ **Lightweight:** A small, plain Java JAR with minimal dependencies.
* ✅ **Simple DSL:** Define rules in human-readable **YAML** files or a **Fluent Java DSL**.
* ✅ **Spring-Native:** A first-class Spring Boot starter for zero-effort autoconfiguration.
* ✅ **Hot-Reloading:** Rules are treated as configuration, not code. Reload them at runtime without restarting your service.
* ✅ **Transparent:** A "glass box" design with built-in tracing so you always know *why* a rule fired.