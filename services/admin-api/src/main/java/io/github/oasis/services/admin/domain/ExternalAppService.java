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

package io.github.oasis.services.admin.domain;

import io.github.oasis.services.admin.internal.ApplicationKey;
import io.github.oasis.services.admin.internal.ErrorCodes;
import io.github.oasis.services.admin.internal.dao.IExternalAppDao;
import io.github.oasis.services.admin.internal.dto.ExtAppRecord;
import io.github.oasis.services.admin.internal.dto.ExtAppUpdateResult;
import io.github.oasis.services.admin.internal.dto.NewAppDto;
import io.github.oasis.services.admin.internal.dto.ResetKeyDto;
import io.github.oasis.services.admin.internal.exceptions.ExtAppNotFoundException;
import io.github.oasis.services.admin.json.apps.AppUpdateResultJson;
import io.github.oasis.services.admin.json.apps.ApplicationAddedJson;
import io.github.oasis.services.admin.json.apps.ApplicationJson;
import io.github.oasis.services.admin.json.apps.NewApplicationJson;
import io.github.oasis.services.admin.json.apps.UpdateApplicationJson;
import io.github.oasis.services.common.OasisServiceException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@Component
public class ExternalAppService {

    private static final int RSA_KEY_SIZE = 2048;
    private static final String RSA = "RSA";

    private IExternalAppDao externalAppDao;

    public ExternalAppService(IExternalAppDao externalAppDao) {
        this.externalAppDao = externalAppDao;
    }

    public void resetKey(int appId) throws OasisServiceException {
        ExtAppRecord appRecord = externalAppDao.readApplication(appId)
                .orElseThrow(() -> new ExtAppNotFoundException("No app is found by id " + appId + "!"));
        KeyPair newKeyPair = generateRandomRSAKeyPair(appRecord.getName());
        ResetKeyDto resetKey = ResetKeyDto.assignKeys(newKeyPair);
        if (!isSuccessfulOperation(externalAppDao.resetKeysOfApp(appId, resetKey))) {
            throw new OasisServiceException(ErrorCodes.KEY_CANNOT_RESET,
                    String.format("The keys for application %s cannot reset. Try again later.", appRecord.getName()));
        }
    }

    public void deactivateApplication(int appId) throws ExtAppNotFoundException {
        if (!isSuccessfulOperation(externalAppDao.deactivateApplication(appId))) {
            throw new ExtAppNotFoundException("App is not found by given id!");
        }
    }

    public List<ApplicationJson> getAllRegisteredApplications() {
        return externalAppDao.getAllRegisteredApps()
                .stream()
                .map(ApplicationJson::from)
                .collect(Collectors.toList());
    }

    public ApplicationKey readApplicationSecretKey(int appId) {
        return externalAppDao.readApplicationKey(appId);
    }

    public AppUpdateResultJson updateApplication(int appId, UpdateApplicationJson updateData) {
        ExtAppUpdateResult updateResult = externalAppDao.updateApplication(appId, updateData);
        return AppUpdateResultJson.from(updateResult);
    }

    public ApplicationAddedJson addApplication(NewApplicationJson newApplication) {
        NewAppDto appDto = NewAppDto.from(newApplication);
        assignKeys(appDto);

        String token = appDto.getToken();
        int id = externalAppDao.addApplication(appDto);

        return new ApplicationAddedJson(id, token);
    }

    public void attachAllGameApplicationsToNewGame(int gameId) {
        externalAppDao.attachAppsToNewGame(gameId);
    }

    public NewAppDto assignKeys(NewAppDto appDto) {
        KeyPair keyPair = generateRandomRSAKeyPair(appDto.getName());
        return appDto.assignKeys(keyPair);
    }

    KeyPair generateRandomRSAKeyPair(String appName) {
        try {
            String seed = String.format("%s-%d", appName, System.currentTimeMillis());
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA);
            SecureRandom secureRandom = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
            keyGen.initialize(RSA_KEY_SIZE, secureRandom);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate keys for the external application!");
        }
    }

    boolean isSuccessfulOperation(int result) {
        return result > 0;
    }
}
