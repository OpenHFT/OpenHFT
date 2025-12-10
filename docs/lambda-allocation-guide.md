# Guide: Minimising Lambda Allocations in Hot Paths

Goal: keep GC noise low. Non-capturing lambdas are singleton-cached (effectively free); capturing lambdas allocate
closure objects. Minimise captures, hoist/reuse any that must capture.

## What counts as “capturing”?

- Uses enclosing locals/params, `this`, instance fields, or outer lambda variables.
- Non-capturing only uses its own parameters or statics/compile-time constants.

## Why it matters

- Non-capturing lambdas → one cached instance.
- Capturing lambdas → allocate closure objects; can dominate GC in hot loops/streams.
- Escape analysis might remove some allocations, but don’t rely on it.

## Do/Don’t patterns

- Prefer non-capturing lambdas & static method references on hot paths.
- Avoid capturing `this` inside loops/streams; hoist to a field or use static helpers.
- Hoist capturing lambdas: allocate once (field/local) and reuse instead of per-iteration.
- Keep captured state minimal and immutable; capture values, not whole contexts.
- For streams on hot/short pipelines, compare to a simple loop; use primitive streams and non-capturing refs where
  possible.

## Detection tips

- Language rule: if it touches enclosing locals/fields/`this`, it’s capturing.
- Profiling: in JFR/async-profiler, synthetic `*$$Lambda$*` allocations in hot paths are suspects.
- Static scans: search `->`/`::` and check for outer variable use; consider lint rules that flag captures in hot code.

## Refactor checklist

1) Can it be made non-capturing? (static helper taking all needed args)
2) If capture is needed, can you hoist/caches the lambda once per instance/config?
3) If still hot, replace with explicit class/loop.

## Examples

- Bad (alloc per iteration): `for (...) doWork(x -> x.apply(discount)); // captures discount each time`
- Better: `Function<Price, Price> applyDiscount = x -> x.apply(discount); for (...) doWork(applyDiscount);`
- Best (no capture): `forEach(MyUtil::process);` when logic only needs params/statics.

## References (for further reading)

- Oracle lambda internals: non-capturing = singleton; capturing carries state.
- dev.java: “favor non-capturing lambdas over capturing ones, for performance reasons.”
- OpenJDK JDK-8193066: removed capturing lambdas in JarFile to avoid extra allocations.
- “Java Lambdas and Low Latency” (vanillajava.blog): microbenchmarks showing lambda allocation costs.
- Ionuț Baloșin: `this::method` in a loop allocates; hoist or avoid capture.
