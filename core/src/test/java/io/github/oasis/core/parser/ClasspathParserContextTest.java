/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Isuru Weerarathna
 */
class ClasspathParserContextTest {

    @Test
    void manipulateFullPath() {
        ClasspathParserContext loader = new ClasspathParserContext("a/b/c/d.yaml");
        Assertions.assertEquals("a/b/c/e/f.yaml", loader.manipulateFullPath("e/f.yaml"));
        Assertions.assertEquals("a/b/c/e/f.yaml", loader.manipulateFullPath("./e/f.yaml"));
        Assertions.assertEquals("a/b/c/e/f.yaml", loader.manipulateFullPath("/e/f.yaml"));
        Assertions.assertEquals("a/b/e/f.yaml", loader.manipulateFullPath("../e/f.yaml"));
        Assertions.assertEquals("a/b/c/f.yaml", loader.manipulateFullPath("f.yaml"));
        Assertions.assertEquals("a/b/c/f.yaml", loader.manipulateFullPath("./f.yaml"));
    }

    @Test
    void manipulateFullPathInRoot() {
        ClasspathParserContext loader = new ClasspathParserContext("d.yaml");
        Assertions.assertEquals("e/f.yaml", loader.manipulateFullPath("e/f.yaml"));
        Assertions.assertEquals("e/f.yaml", loader.manipulateFullPath("./e/f.yaml"));
        Assertions.assertEquals("e/f.yaml", loader.manipulateFullPath("/e/f.yaml"));
        Assertions.assertEquals("../e/f.yaml", loader.manipulateFullPath("../e/f.yaml"));
        Assertions.assertEquals("f.yaml", loader.manipulateFullPath("f.yaml"));
        Assertions.assertEquals("f.yaml", loader.manipulateFullPath("./f.yaml"));
    }

    @Test
    void parseStack() {
        ClasspathParserContext loader = new ClasspathParserContext("a/b/c/d.yaml");
        String nextPath = loader.manipulateFullPath("e/f.yaml");
        Assertions.assertEquals("a/b/c/e/f.yaml", nextPath);
        loader.pushCurrentLocation(nextPath);

        nextPath = loader.manipulateFullPath("x/y/z.yaml");
        Assertions.assertEquals("a/b/c/e/x/y/z.yaml", nextPath);
        loader.pushCurrentLocation(nextPath);

        loader.popCurrentLocation();
        nextPath = loader.manipulateFullPath("p.yaml");
        Assertions.assertEquals("a/b/c/e/p.yaml", nextPath);
    }
}