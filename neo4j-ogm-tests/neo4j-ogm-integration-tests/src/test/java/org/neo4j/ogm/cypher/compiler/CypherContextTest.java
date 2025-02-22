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
package org.neo4j.ogm.cypher.compiler;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.gh576.DataItem;
import org.neo4j.ogm.domain.gh576.FormulaItem;
import org.neo4j.ogm.domain.gh576.Variable;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * This tests needs to run multiple times to increase the value of native ids used for the relationship entities.
 * The deletion of relationship entities started to fail usually at the second or third iteration, probably depending
 * on the Set and Iterator implementations of the JVM
 * (See especially org.neo4j.ogm.cypher.compiler.CypherContext#deletedRelationships).
 *
 * @author Andreas Berger
 * @author Michael J. Simons
 */
public class CypherContextTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private static String createTestDataStatement = TestUtils.readCQLFile("org/neo4j/ogm/cql/nodes.cql").toString();

    private Session session;

    public static List<Integer> data() {
        return IntStream.range(0, 10)
            .boxed().collect(toList());
    }

    @BeforeAll
    public static void initSessionFactory() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh576");
    }

    @BeforeEach
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        session.clear();

        session.query(createTestDataStatement, Collections.emptyMap());
    }

    // GH-576
    @MethodSource("data")
    @ParameterizedTest
    void shouldDeregisterRelationshipEntities(@SuppressWarnings("unused") Integer iterations) {
        Collection<DataItem> dataItems;
        FormulaItem formulaItem;

        Filter filter = new Filter("nodeId", ComparisonOperator.EQUALS, "m1");

        dataItems = session.loadAll(DataItem.class, filter);
        assertThat(dataItems).hasSize(1);

        formulaItem = (FormulaItem) dataItems.iterator().next();
        assertThat(formulaItem.getVariables()).hasSize(3);

        Predicate<Variable> isVariableAWithDataItemM2 = v -> v.getVariable().equals("A") && v.getDataItem().getNodeId()
            .equals("m2");
        formulaItem.getVariables().removeIf(isVariableAWithDataItemM2);
        assertThat(formulaItem.getVariables()).hasSize(2);

        session.save(formulaItem);

        dataItems = session.loadAll(DataItem.class, filter);
        assertThat(dataItems).hasSize(1);

        formulaItem = (FormulaItem) dataItems.iterator().next();
        assertThat(formulaItem.getVariables()).hasSize(2);
    }

    @AfterEach
    public void tearDown() {
        session.purgeDatabase();
    }
}
