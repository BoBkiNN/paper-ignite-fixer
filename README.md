This small ignite mod fixes issue when paper tries to find server jar file using `getProtectionDomain().getCodeSource().getLocation()`

Instead of this method, mod scans for `ignite.jar` and `ignite.paper.jar` system properties