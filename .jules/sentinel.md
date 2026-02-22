## 2025-02-20 - Automatic Listener Registration
**Vulnerability:** N/A (Architecture Pattern)
**Learning:** The project uses `ClassScanner` to automatically register all `Listener` implementations in the `listener` package. This reduces boilerplate but can confuse reviewers/developers who expect manual registration in `onEnable`.
**Prevention:** Be aware of this pattern when adding listeners; simply placing the class in the correct package is sufficient. Explicit registration is unnecessary and would be redundant.
