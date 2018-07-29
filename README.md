# therore - pluginloader

PluginLoader is a library oriented to build pluggable components
for the Java Virtual Machine.

#### Features
* **Sandboxed**: each plugin has its own classpath which means that it can use
    a different version of a library or a class without conflicts.
* **Runtime Plugins**: pluginloader loads plugins at runtime and on-demand.
* **Support Exclussions**: through patterns, pluginloader allows you to specify which classes you want to be shared between host and plugin.

#### Making use of pluginloader. Example in Groovy

##### Firstly, include pluginloader dependency
```xml
<dependency>
  <groupId>net.therore.pluginloader</groupId>
  <artifactId>therore-pluginloader</artifactId>
  <version>LATEST</version>
</dependency>

```

***Note:*** all the examples are written in groovy for ease of reading

##### Define the classpath of the plugin using file/directory patterns
```groovy
// In our case the directory "plugin-directory" contains the plugin files.
Plugin plugin = new Plugin(new File("plugin-directory"), ["conf",lib/*.jar"])

```

##### Alternatively you can define the classpath of the plugin using file/directory patterns and excluded classes patterns. Excluded classes are shared between host and plugin. 
```groovy
// In our case the directory "plugin-directory" contains the plugin files. We want to share slf4j classes.
Plugin plugin = new Plugin(new File("plugin-directory"), ["conf",lib/*.jar"], Arrays.asList("^org\\.slf4j\\..*"))

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
**You can check a running examples of use in the test directory of this project**
