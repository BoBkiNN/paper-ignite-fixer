This small ignite mod fixes issue when paper tries to find server jar file using `getProtectionDomain().getCodeSource().getLocation()`

Instead of this method, mod scans for `ignite.jar` and `ignite.paper.jar` system properties
### Since 1.1 mod provides ability to use tiny-remapper to remap plugins.
This is enabled by default, so to disable it use `-Dignite.AlternateRemapping=false` system property
