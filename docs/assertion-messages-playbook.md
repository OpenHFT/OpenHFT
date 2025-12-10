# Playbook: Adding Meaningful Assertion Messages

Purpose: When tests fail, the message should point you to *which property* broke and where to look next. Use this when
touching tests to decide whether to add or refine assertion messages.

## When to add messages

- Prefer clear test names and domain-specific helpers first; add messages for non-obvious assertions (binary payload
  fields, lifecycle/refCount transitions, looped/parameterised cases).
- One assertion = one fact under test. The message should restate that fact in domain terms, not just echo
  expected/actual.

## Quick procedure

1. **Inventory**: List changed tests/assertions (e.g., `git diff` and grep `assert`).
2. **Decide necessity**: Add messages only where the failure would be ambiguous (loops, multiple similar asserts,
   lifecycle stages, multi-field payloads).
3. **Phrase it**: State the property being checked, with context like index/stage:
    - `"flag from binary payload"`
    - `"refCount after reserve"`
    - `"value at index " + i`
    - `"UTF-8 trailer text mismatch"`
4. **Loops/param tests**: Include indices/parameters in the message so the failing case is obvious.
5. **Lifecycle/threads**: Include the stage (`after reserve`, `after release`) or thread/iteration context if
   multi-threaded.
6. **Performance**: If constructing a rich message is expensive, use lazy suppliers (JUnit 5) so the cost is paid only
   on failure.
7. **Helpers over duplication**: For repeated patterns, extract custom assertion helpers (e.g.,
   `assertRefCount(String stage, long expected, long actual)`).

## Research keywords (if you need deeper guidance)

- `JUnit assertEquals custom message best practices`
- `assertion message describe property not just values`
- `lazy assertion message supplier junit5`
- `assertion messages in loops include index`
- `binary payload decoding test assertions`
- `refcount lifecycle test assertion message`
- `AssertJ describedAs best practices`
- `alternatives to assertion messages custom assertions`

## Examples

- Binary payload: `assertEquals("s24 from payload", -6_666_666, s24);`
- Lifecycle: `assertEquals("refCount after reserve", 2, bytes.refCount());`
- Loops: `assertEquals("8-bit round-trip mismatch at iteration " + i, s, nbytes2.toString());`
- CAS test: `assertFalse("compareAndSet should fail when locked value unchanged", result);`

## Naming note

- Permanent docs (`.adoc`/`.md`) should use kebab-case filenames (e.g., `assertion-messages-playbook.md`).
- Temporary or transient Markdown files and top-level READMEs may stay UPPER_CASE if that is the existing convention (
  e.g., `README.adoc`, short-lived scratch files).
