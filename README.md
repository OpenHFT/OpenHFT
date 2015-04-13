OpenHFT Parent Module
=====================

Parent module to include active modules

Key Modules
===========

Java Thread Affinity - Declaritive binding of threads to individual CPUs.  Using in combination with isocpus= on Linux, you can reduce scheduling jitter from 5 ms to 10 micro-seconds.

Java Lang - Low level IO such as 64-bit access to off heap memory. Used by Chronicle and Huge Collections

Java Chroncicle - Low latency IPC. Supports 0.1 micro-second persisted IPC between processes and signle digit persisted replication between machines. Less than 15 micro-seconds between machines, 99% of the time for real messages.

HugeCollections - Off heap hash maps.  HugeHashMap is a process private, off heap storage supporting over one billon key-values. SharedHashMap is a the same shared between processes and is persisted.  SharedHashMap supports tens of millions operations per second, for hundreds of millions of keys-values, with no GC pressure (trivial garbage produced)

Java Runtime Compiler - Wrapper for the Java Compiler API to support in memory compilation.  Give it is String of Java code and it will give you a Class.

Tools
=====

#### OpenHFT Recommends

---

[![YourKit](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/)
 
YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

Review : YourKit is a great profiler, once you start profiling, you'll see a real-time display of CPU and memory usage. Another tab shows time spent in the garbage collector, how many collections have taken place recently. You can force a garbage collection to take place to watch the effect on performance. Other tabs provide info on the number of running threads and the number of classes loaded. YourKit also  allows you to take a snapshot of the running process; this is automatically saved and can be subsequently reopened for comparative purposes. Within the snapshot, you can view a hotspot listing, showing the methods where the application spent most of its time. You can also see a call tree - either merged or arranged by individual thread - and view back traces of individual methods showing where they were called from.

---

[![Teamcity](https://www.jetbrains.com/teamcity/docs/logo_teamcity.png)](http://www.jetbrains.com/teamcity/)

---

[![IntelliJ](http://www.jetbrains.com/img/logos/logo_intellij_idea.png)](http://www.jetbrains.com/idea/)

---

NOTE : Other commercial profilers and IDE's can be used.  VisualVM creates too much noise to be useful at this level.

