/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package net.therore.pluginloader;

import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * @author <a href="mailto:alfredo.diaz@therore.net">Alfredo Diaz</a>
 */
public class PluginLoader {

    public static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

    @SneakyThrows
    static private URL[] filesToURLs(File baseDirectory, String[] patterns) {
        return Files.walk(Paths.get(baseDirectory.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .filter(path -> stream(patterns)
                        .anyMatch(pattern ->
                                FILE_SYSTEM.getPathMatcher("glob:" + pattern).matches(path)
                        )
                )
                .map(PluginLoader::getUrl).collect(Collectors.toList())
          .toArray(new URL[0]);
    }

    @SneakyThrows
    static private URL getUrl(Path file){
        return file.toUri().toURL();
    }

    private final PluginClassLoader classLoader;

    @SneakyThrows
    public PluginLoader(Plugin plugin) {
        this.classLoader = new PluginClassLoader(filesToURLs(plugin.getBaseDirectory(), plugin.getClasspathPatterns()));
    }

    @SneakyThrows
    public Class loadClass(String name) {
        return classLoader.loadClass(name, false);
    }

    public <T> T invokeInPlugin(Callable<T> callable) throws Throwable {
        Thread thread = Thread.currentThread();
        ClassLoader previousThreadContextClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(this.classLoader);
        try {
            return callable.call();
        } catch (ExceptionInInitializerError e) {
            Throwable e1 = e.getException();
            Throwable e2 = e1 != null ? e1 : e;
            e2.printStackTrace();
            throw e2;
        } catch (InvocationTargetException e) {
            Throwable e2 = e.getTargetException();
            if (e2 instanceof ExceptionInInitializerError) {
                Throwable e3 = ((ExceptionInInitializerError) e2).getException();
                if (e3 != null) {
                    e3.printStackTrace();
                    throw e3;
                }
            }
            e2.printStackTrace();
            throw e2;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        } finally {
            thread.setContextClassLoader(previousThreadContextClassLoader);
        }
    }

}
