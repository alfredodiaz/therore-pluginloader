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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:alfredo.diaz@therore.net">Alfredo Diaz</a>
 */
public class PluginClassLoader extends URLClassLoader {

    static private final String CLASS_FILE_SUFFIX = ".class";

    static protected final boolean parallelCapableClassLoaderAvailable =
            (getMethodIfAvailable(ClassLoader.class, "registerAsParallelCapable") != null);

    static {
        if (parallelCapableClassLoaderAvailable) {
            ClassLoader.registerAsParallelCapable();
        }
    }

    protected final Set<String> LOADED_CLASSES = Collections.synchronizedSet(new HashSet<>());
    protected final Set<String> JAR_URL_PREFIXES;

    private final List<Pattern> excludedClassPatterns = new ArrayList<>();

    public PluginClassLoader(URL[] urls, List<String> excludedClassesPattern) {
        super(urls, Thread.currentThread().getContextClassLoader());

        for (String pattern : excludedClassesPattern) {
            excludedClassPatterns.add(Pattern.compile(pattern));
        }
        JAR_URL_PREFIXES = Arrays.stream(urls).map(
                url -> "jar:"+url.toString()
        ).collect(Collectors.toSet());

    }

    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            }
            catch (NoSuchMethodException ex) {
                return null;
            }
        }
        else {
            Set<Method> candidates = new HashSet<Method>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    protected boolean isExcluded(String name) {
        for (Pattern pattern : excludedClassPatterns) {
            if (pattern.matcher(name).matches())
                return true;
        }
        return false;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isEligibleForOverriding(name)) {
            Class<?> result = super.findClass(name);
            if (result != null) {
                LOADED_CLASSES.add(name);
                if (resolve) {
                    resolveClass(result);
                }
                return result;
            }
        }
        return super.loadClass(name, resolve);
    }

    public boolean isEligibleForOverriding(String name) {
        if (LOADED_CLASSES.contains(name))
            return false;

        if (isExcluded(name))
            return false;

        return findResource(name.replace('.', '/') + CLASS_FILE_SUFFIX) != null;
    }


}
