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
package net.therore.pluginloader

import org.junit.Assert
import org.junit.Test

/**
 * @author <a href="mailto:alfredo.diaz@therore.net">Alfredo Diaz</a>
 */
public class PluginLoaderClassOrdertTest {

    public findResourcesUsingClassLoader(String resourceName) {
        File baseDirectory = new File(System.getProperty("MAVEN_BUILD_DIRECTORY", "target") + "/test-resources")
        URLClassLoader classLoader = new URLClassLoader(
            PluginLoader.filesToURLs(baseDirectory, ["**/*.jar"] as String[]), this.class.classLoader)
        return Collections.list(classLoader.getResources(resourceName))
    }

    public findResourcesUsingPLuginLoader(String resourceName) {
        File baseDirectory = new File(System.getProperty("MAVEN_BUILD_DIRECTORY", "target"))
        Plugin plugin = new Plugin(baseDirectory, ["**/classes","**/test-resources/**/*.jar"] as String[])
        PluginLoader pluginLoader = new PluginLoader(plugin)
        return Collections.list(pluginLoader.classLoader.getResources(resourceName))
    }

    /**
     * Test that resources are ordered in the same way that URLClassLoader does
     */
    @Test
    public void testFindResources() {
        Assert.assertEquals(findResourcesUsingClassLoader("build.properties")
                , findResourcesUsingPLuginLoader("build.properties"))
    }
}
