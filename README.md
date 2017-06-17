# therore - pluginloader

PluginLoader is a library oriented to build pluggable components
for the Java Virtual Machine.

#### Features
* **Sandboxed**: each plugin has its own classpath which means that it can use
    a different version of a library or a class without conflicts.
* **Runtime Plugins**: pluginloader loads plugins at runtime and on-demand.
