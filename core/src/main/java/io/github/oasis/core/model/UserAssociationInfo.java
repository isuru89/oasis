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

package io.github.oasis.core.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Data
public class UserAssociationInfo implements Serializable {

    private String email;
    private long id;

    private Map<Integer, GameAssociation> games;

    @Data
    public static class GameAssociation implements Serializable {
        private int team;

        public GameAssociation() {}

        private GameAssociation(int teamId) {
            this.team = teamId;
        }

        public static GameAssociation ofTeam(int teamId) {
            return new GameAssociation(teamId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GameAssociation that = (GameAssociation) o;
            return team == that.team;
        }

        @Override
        public int hashCode() {
            return Objects.hash(team);
        }
    }

}
