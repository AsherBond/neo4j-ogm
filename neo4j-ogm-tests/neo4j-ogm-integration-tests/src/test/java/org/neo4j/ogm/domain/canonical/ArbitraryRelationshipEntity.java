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
package org.neo4j.ogm.domain.canonical;

import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * NB: this isn't actually used to save anything at the time of writing; it's just so we can test meta-data resolution for
 * relationship entities.
 *
 * @author Adam George
 */
@RelationshipEntity(type = "MEMBER_OF")
public class ArbitraryRelationshipEntity {

    // nothing here yet
}
