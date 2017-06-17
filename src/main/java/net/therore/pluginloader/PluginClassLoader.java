package net.therore.pluginloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PluginClassLoader extends ClassLoader {

    /** Packages that are excluded by default */
    static public final String[] DEFAULT_EXCLUDED_PACKAGES = new String[]
            {"java.", "javax.", "sun.", "oracle.", "javassist.", "org.aspectj.", "net.sf.cglib."};
    static private final String CLASS_FILE_SUFFIX = ".class";
    static private final int BUFFER_SIZE = 4096;
    static private final Pattern CLASS_IN_JAR_PATTERN = Pattern.compile("(jar:file:/[^!]+)(!.*)");

    static protected final boolean parallelCapableClassLoaderAvailable =
            (getMethodIfAvailable(ClassLoader.class, "registerAsParallelCapable") != null);

    static {
        if (parallelCapableClassLoaderAvailable) {
            ClassLoader.registerAsParallelCapable();
        }
    }

    protected final Set<String> JAR_URL_PREFIXES;

    public PluginClassLoader(URLClassLoader parent) {
        super(parent);
        for (String packageName : DEFAULT_EXCLUDED_PACKAGES) {
            excludePackage(packageName);
        }
        JAR_URL_PREFIXES = Arrays.stream(parent.getURLs()).map(
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

    private final Set<String> excludedPackages =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(8));

    private final Set<String> excludedClasses =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(8));

    public void excludePackage(String packageName) {
        this.excludedPackages.add(packageName);
    }

    public void excludeClass(String className) {
        this.excludedClasses.add(className);
    }

    protected boolean isExcluded(String className) {
        if (this.excludedClasses.contains(className)) {
            return true;
        }
        for (String packageName : this.excludedPackages) {
            if (className.startsWith(packageName)) {
                return true;
            }
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
            Class<?> result = loadClassForOverriding(name);
            if (result != null) {
                if (resolve) {
                    resolveClass(result);
                }
                return result;
            }
        }
        return super.loadClass(name, resolve);
    }

    public boolean isEligibleForOverriding(String className) {
        if (isExcluded(className))
            return false;

        String internalName = className.replace('.', '/') + CLASS_FILE_SUFFIX;
        URL resource = getParent().getResource(internalName);

        if (resource == null)
            return false;

        Matcher matcher = CLASS_IN_JAR_PATTERN.matcher(resource.toString());
        if (!matcher.matches())
            return false;

        boolean result = JAR_URL_PREFIXES.contains(matcher.group(1));
        return result;
    }


    protected Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
        Class<?> result = findLoadedClass(name);
        if (result == null) {
            byte[] bytes = loadBytesForClass(name);
            if (bytes != null) {
                result = defineClass(name, bytes, 0, bytes.length);
            }
        }
        return result;
    }

    protected byte[] loadBytesForClass(String name) throws ClassNotFoundException {
        InputStream is = openStreamForClass(name);
        if (is == null) {
            return null;
        }
        try {
            // Load the raw bytes.
            byte[] bytes = copyToByteArray(is);
            // Transform if necessary and use the potentially transformed bytes.
            return bytes;
        }
        catch (IOException ex) {
            throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
        }
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        try {
            int byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ex) {
            }
            try {
                out.close();
            }
            catch (IOException ex) {
            }
        }
    }

    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }

    protected InputStream openStreamForClass(String name) {
        String internalName = name.replace('.', '/') + CLASS_FILE_SUFFIX;
        return getParent().getResourceAsStream(internalName);
    }

}
