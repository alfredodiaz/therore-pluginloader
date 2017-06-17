package net.therore.pluginloader;

import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alfredo on 6/16/17.
 */
public class PluginLoader {

    static private final PathMatcher JAR_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.jar");

    static private URL[] filesToURLs(File[] files) {
        return Arrays.stream(files)
                .filter(directory -> directory.exists())
                .flatMap(directory -> findJars(directory)).map(file -> getUrl(file)).collect(Collectors.toList())
          .toArray(new URL[0]);
    }

    @SneakyThrows
    static private URL getUrl(Path file){
        return file.toUri().toURL();
    }

    @SneakyThrows
    static private Stream<? extends Path> findJars(File directory) {
        return Files.walk(Paths.get(directory.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .filter(file -> JAR_MATCHER.matches(file.getFileName()));
    }

    @SneakyThrows
    static public Class loadPluginClass(Plugin plugin) {
        URL[] urls = filesToURLs(plugin.getLibraryDirectories());
        URLClassLoader urlClassLoader = new URLClassLoader(urls, null);
        PluginClassLoader loader = new PluginClassLoader(urlClassLoader);
        Class<?> clazz = loader.loadClass(plugin.getMainClass(), true);
        return clazz;
    }

    @SneakyThrows
    static public void invokePluginMain(Plugin plugin) {
        Class<?> clazz = loadPluginClass(plugin);
        Constructor<?> constructor = clazz.getConstructor(new Class[0]);
        Object o = constructor.newInstance();
        Method mainMethod = o.getClass().getMethod("main", new Class<?>[] { String[].class });
        Thread thread = Thread.currentThread();
        ClassLoader previousThreadContextClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(clazz.getClassLoader());
        try {
            mainMethod.invoke(null, new Object[]{new String[]{}});
        } finally {
            thread.setContextClassLoader(previousThreadContextClassLoader);
        }
    }

}
