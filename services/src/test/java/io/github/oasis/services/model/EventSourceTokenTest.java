/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.model;

import io.github.oasis.model.collect.Pair;
import io.github.oasis.services.utils.SecurityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class EventSourceTokenTest {

    @Test
    public void testToken() throws NoSuchAlgorithmException, IOException {
        EventSourceToken token = new EventSourceToken();
        token.setDisplayName("Stack Overflow");
        token.setSourceName("stack-overflow");
        token.setActive(true);

        Pair<PrivateKey, PublicKey> pair = SecurityUtils.generateRSAKey(token.getSourceName());
        token.setPublicKey(pair.getValue1().getEncoded());
        token.setSecretKey(pair.getValue0().getEncoded());

        PrivateKey decoded = token.getSecretPrivateKey();
        Assert.assertEquals(pair.getValue0(), decoded);
    }

}
