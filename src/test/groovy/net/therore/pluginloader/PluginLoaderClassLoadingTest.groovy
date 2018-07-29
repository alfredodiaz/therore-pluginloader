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
import org.slf4j.Logger

/**
 * @author <a href="mailto:alfredo.diaz@therore.net">Alfredo Diaz</a>
 */
public class PluginLoaderClassLoadingTest {

    /**
     * Test that resources are ordered in the same way that URLClassLoader does
     */
    @Test
    public void testLoadingClasses() {
        PluginLoader pluginLoader = new PluginLoader(new net.therore.pluginloader.Plugin(
                "target/ldaptive-output" as File,["**/*.jar"]));

        pluginLoader.invokeInPlugin({ ->
            def connectionFactory = pluginLoader.loadClass("org.ldaptive.DefaultConnectionFactory").newInstance("ldap://directory.ldaptive.org");
            println (connectionFactory)
        })
    }

    /**
     * Test to share classes between plugin and host
     */
    @Test
    public void testSharingClasses() {
        PluginLoader pluginLoader = new PluginLoader(new net.therore.pluginloader.Plugin(
                "target/ldaptive-output" as File,["**/*.jar"], Arrays.asList("^org\\.slf4j\\..*")));

        pluginLoader.invokeInPlugin({ ->
            def connectionFactory = pluginLoader.loadClass("org.ldaptive.DefaultConnectionFactory").newInstance("ldap://directory.ldaptive.org");
            Logger connectionFactoryLogger = connectionFactory.provider.logger
            println (connectionFactoryLogger)
        })
    }
}
