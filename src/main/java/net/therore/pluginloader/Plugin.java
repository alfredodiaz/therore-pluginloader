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

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:alfredo.diaz@therore.net">Alfredo Diaz</a>
 */
@Data
@AllArgsConstructor
public class Plugin {

    private final File baseDirectory;
    private final String[] classpathPatterns;
    private final List<String> DEFAULT_EXCLUDED_CLASSES_PATTERNS = Arrays.asList(new String[]
            {
                    "^java\\..*",
                    "^javax\\..*",
                    "^sun\\..*",
                    "^oracle\\..*",
                    "^javassist\\..*",
                    "^org\\.aspectj\\..*",
                    "^net\\.sf\\.cglib\\..*"
            }
    );
    private List<String> excludedClassPatterns = new ArrayList<>();

    public Plugin(File baseDirectory, Collection<String> patterns) {
        this.baseDirectory = baseDirectory;
        this.classpathPatterns = patterns.toArray(new String[]{});
        this.excludedClassPatterns.addAll(DEFAULT_EXCLUDED_CLASSES_PATTERNS);
    }

    public Plugin(File baseDirectory, Collection<String> patterns, List<String> excludedClassPatterns) {
        this.baseDirectory = baseDirectory;
        this.classpathPatterns = patterns.toArray(new String[]{});
        this.excludedClassPatterns.addAll(DEFAULT_EXCLUDED_CLASSES_PATTERNS);
        this.excludedClassPatterns.addAll(excludedClassPatterns);
    }


}
