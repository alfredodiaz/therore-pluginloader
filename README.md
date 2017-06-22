# therore - pluginloader

PluginLoader is a library oriented to build pluggable components
for the Java Virtual Machine.

#### Features
* **Sandboxed**: each plugin has its own classpath which means that it can use
    a different version of a library or a class without conflicts.
* **Runtime Plugins**: pluginloader loads plugins at runtime and on-demand.

#### Making use of pluginloader. Example in Groovy

##### Firstly, include pluginloader dependency
```xml
<dependency>
  <groupId>net.therore.pluginloader</groupId>
  <artifactId>therore-pluginloader</artifactId>
  <version>LATEST</version>
</dependency>

```
##### Define the classpath of the plugin using patterns
```groovy
// In our case the directory "plugin-directory" contains the plugin files.
Plugin plugin = new Plugin(new File("plugin-directory"), ["conf",lib/*.jar"])

```

##### Instanciate a plugin loader
```groovy
PluginLoader pluginLoader = new PluginLoader(plugin)

```

##### Invoke a method of a class inside the plugin

```groovy
pluginLoader.invokeInPlugin({ ->
    def className = "com.mycompany.myplugin.Main"
    Class pluginMainClass = pluginLoader.loadClass(className)
    pluginMainClass.main(new String[0])
})

```



##### Altogether
```groovy
Plugin plugin = new Plugin(new File("plugin-directory"), ["**/*.jar"])

PluginLoader pluginLoader = new PluginLoader(plugin)
pluginLoader.invokeInPlugin({ ->
    def className = "com.mycompany.myplugin.Main"
    Class pluginMainClass = pluginLoader.loadClass(className)
    pluginMainClass.main(new String[0])
})

```
**You can check a running example of use in the tests of the project**
