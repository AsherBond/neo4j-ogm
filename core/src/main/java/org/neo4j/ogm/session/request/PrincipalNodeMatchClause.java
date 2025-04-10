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
package org.neo4j.ogm.session.request;

import org.neo4j.ogm.cypher.Filter;

/**
 * @author Jasper Blues
 * @author Michael J. Simons
 */
class PrincipalNodeMatchClause implements MatchClause {

    private final String varName;
    private StringBuilder clause;

    PrincipalNodeMatchClause(String label) {
        this(label, "n");
    }

    PrincipalNodeMatchClause(String label, String varName) {

        this.varName = varName;
        this.clause = new StringBuilder();
        this.clause.append(String.format("MATCH (%s:`%s`) ", varName, label));
    }

    @Override
    public MatchClause append(Filter filter) {
        clause.append(filter.toCypher(varName, clause.indexOf(" WHERE ") == -1));
        return this;
    }

    @Override
    public String toCypher() {
        return clause.toString();
    }
}
