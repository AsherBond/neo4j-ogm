/*
 * Copyright (c) 2002-2025 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.sdn2306;

import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Node extends BaseObject {

    @Id @GeneratedValue
    private Long id;

    @Labels protected Set<String> neo4jLabels;

    public Long getId() {
        return id;
    }

    public Set<String> getNeo4jLabels() {
        return neo4jLabels;
    }

    public void setNeo4jLabels(Set<String> neo4jLabels) {
        this.neo4jLabels = neo4jLabels;
    }
}
