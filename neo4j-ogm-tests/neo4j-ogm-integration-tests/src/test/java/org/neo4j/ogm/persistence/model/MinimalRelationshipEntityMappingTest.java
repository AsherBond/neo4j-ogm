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
package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.domain.cineasts.minimum.Actor;
import org.neo4j.ogm.domain.cineasts.minimum.Movie;
import org.neo4j.ogm.domain.cineasts.minimum.Role;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class MinimalRelationshipEntityMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeAll
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.cineasts.minimum");
    }

    @BeforeEach
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    // GH-607
    @Test
    void verifyChangeOfRelationshipEnd() {
        session = sessionFactory.openSession();
        Actor actor = new Actor("A1");
        Movie movie = new Movie("M1");
        Role role = new Role("R1", actor, movie);

        session.save(role);

        session = sessionFactory.openSession();
        Iterable<Map<String, Object>> results = session
            .query("MATCH (m:Movie) <- [:ACTS_IN] - (:Actor {name: 'A1'}) RETURN COUNT(m) as cnt",
                Collections.emptyMap()).queryResults();
        assertThat(results).hasSize(1);
        assertThat(results).first().satisfies(m -> assertThat(m).containsEntry("cnt", 1L));

        // New session / TX
        session = sessionFactory.openSession();
        movie = new Movie("M2");
        role = session.load(Role.class, role.getId());
        role.setMovie(new Movie("M2"));
        session.save(role);

        session = sessionFactory.openSession();
        results = session
            .query("MATCH (m:Movie) <- [:ACTS_IN] - (:Actor {name: 'A1'}) RETURN COUNT(m) as cnt",
                Collections.emptyMap()).queryResults();
        assertThat(results).hasSize(1);
        assertThat(results).first().satisfies(m -> assertThat(m).containsEntry("cnt", 1L));
    }
}
