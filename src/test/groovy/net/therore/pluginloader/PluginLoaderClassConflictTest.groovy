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

import org.drools.examples.banking.BankingExample1
import org.junit.Test

/**
 * @author <a href="mailto:alfredo.diaz@therore.net">Alfredo Diaz</a>
 */
public class PluginLoaderClassConflictTest {

    /**
     * Invoking the class version that is located out of the plugin
     */
    @Test(expected = UnsupportedOperationException.class)
    public void loadingLocalClassVersion() {
        BankingExample1.main(new String[0])
    }

    /**
     * Invoking the class version that is located into the plugin
     */
    @Test
    public void loadingPluginClassVersion() {
        Plugin plugin = new Plugin(
                new File(System.getProperty("MAVEN_BUILD_DIRECTORY", "target") + "/test-resources"),
                ["**/*.jar"]
        )

        PluginLoader pluginLoader = new PluginLoader(plugin)
        pluginLoader.invokeInPlugin({ ->
            def className = "org.drools.examples.banking.BankingExample1"
            Class exampleClass = pluginLoader.loadClass(className)
            exampleClass.main(new String[0])
        })

    }
}
