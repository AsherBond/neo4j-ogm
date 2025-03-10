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
package org.neo4j.ogm.persistence.session.capability;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.domain.gh787.EntityWithCustomIdConverter;
import org.neo4j.ogm.domain.gh787.MyVeryOwnIdType;
import org.neo4j.ogm.domain.gh789.Entity;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class SaveCapabilityTest extends TestContainersTestBase {

    private Session session;
    private Artist aerosmith;
    private Artist bonJovi;
    private Artist defLeppard;

    @BeforeEach
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music",
            "org.neo4j.ogm.domain.gh787", "org.neo4j.ogm.domain.gh789");
        session = sessionFactory.openSession();
        session.purgeDatabase();
        aerosmith = new Artist("Aerosmith");
        bonJovi = new Artist("Bon Jovi");
        defLeppard = new Artist("Def Leppard");
    }

    @AfterEach
    public void clearDatabase() {
        session.purgeDatabase();
    }

    // GH-84
    @Test
    void saveCollectionShouldSaveLists() {
        Album nineLives = new Album("Nine Lives");
        aerosmith.addAlbum(nineLives);
        Album crossRoad = new Album("Cross Road");
        bonJovi.addAlbum(crossRoad);
        List<Artist> artists = Arrays.asList(aerosmith, bonJovi, defLeppard);
        session.save(artists);
        session.clear();
        assertThat(session.countEntitiesOfType(Artist.class)).isEqualTo(3);
        assertThat(session.countEntitiesOfType(Album.class)).isEqualTo(2);
    }

    // GH-84
    @Test
    void saveCollectionShouldSaveSets() {
        Set<Artist> artists = new HashSet<>();
        artists.add(aerosmith);
        artists.add(bonJovi);
        artists.add(defLeppard);
        session.save(artists);
        session.clear();
        assertThat(session.countEntitiesOfType(Artist.class)).isEqualTo(3);
    }

    // GH-84
    @Test
    void saveCollectionShouldSaveArrays() {
        Artist[] artists = new Artist[]{aerosmith, bonJovi, defLeppard};
        session.save(artists);
        session.clear();
        assertThat(session.countEntitiesOfType(Artist.class)).isEqualTo(3);
    }

    @Test
    void shouldSaveNewNodesAndNewRelationships() {
        Artist leann = new Artist("Leann Rimes");
        Album lost = new Album("Lost Highway");
        lost.setArtist(bonJovi);
        lost.setGuestArtist(leann);
        session.save(lost);
        session.clear();

        Artist loadedLeann = session.load(Artist.class, leann.getId());
        assertThat(loadedLeann).isNotNull();
        assertThat(loadedLeann.getName()).isEqualTo("Leann Rimes");
        assertThat(loadedLeann.getGuestAlbums().iterator().next().getName()).isEqualTo(lost.getName());

        Artist loadedBonJovi = session.load(Artist.class, bonJovi.getId());
        assertThat(loadedBonJovi).isNotNull();
        assertThat(loadedBonJovi.getName()).isEqualTo("Bon Jovi");
        assertThat(loadedBonJovi.getAlbums().iterator().next().getName()).isEqualTo(lost.getName());

        Album loadedLost = session.load(Album.class, lost.getId());
        assertThat(loadedLost).isNotNull();
        assertThat(loadedLost.getName()).isEqualTo("Lost Highway");
        assertThat(loadedLost.getGuestArtist()).isEqualTo(loadedLeann);
        assertThat(loadedLost.getArtist().getName()).isEqualTo(loadedBonJovi.getName());
    }

    @Test
    void shouldSaveOnlyModifiedNodes() {

        int depth = 1;
        Neo4jSession neo4jSession = (Neo4jSession) session;
        CompileContext context = null;

        Artist leann = new Artist("Leann Rimes");
        Album lost = new Album("Lost Highway");
        lost.setArtist(bonJovi);
        lost.setGuestArtist(leann);

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(lost, depth);
        assertThat(context.registry()).as("Should save 3 nodes and 2 relations (5 items)").hasSize(5);

        session.save(lost);

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(lost, depth);
        assertThat(context.registry()).as("Should have nothing to save").isEmpty();

        session.clear();

        Artist loadedLeann = session.load(Artist.class, leann.getId(), depth);

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(loadedLeann, depth);
        assertThat(context.registry()).as("Should have nothing to save").isEmpty();

        loadedLeann.setName("New name");
        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(loadedLeann, depth);
        assertThat(context.registry()).as("Should have one node to save").hasSize(1);

        loadedLeann.getGuestAlbums().iterator().next().setName("New Album Name");

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(loadedLeann, depth);
        assertThat(context.registry()).as("Should have two node to save").hasSize(2);
    }

    @Test
    void shouldCountRelationshipEntities() {
        Album greatestHits = new Album("Greatest Hits");

        Studio chessRecordsStudios = new Studio("Chess Records");
        Studio mercuryStudios = new Studio("Mercury");

        Recording recording1 = new Recording(greatestHits, chessRecordsStudios, 1962);
        Recording recording2 = new Recording(greatestHits, mercuryStudios, 1967);

        greatestHits.setRecording(recording1);
        greatestHits.setRecording(recording2);

        session.save(recording1);
        session.save(recording2);

        assertThat(session.countEntitiesOfType(Recording.class)).isEqualTo(2);
    }

    // GH-789
    @Test
    void saveWithCustomKeyConverterShouldWork() {

        EntityWithCustomIdConverter entity = new EntityWithCustomIdConverter(new MyVeryOwnIdType("asd"));
        session.save(entity);
        session.clear();

        // Should be retrievable as well
        entity = session.load(EntityWithCustomIdConverter.class, entity.getKey());
        assertThat(entity).isNotNull();
    }

    // GH-789
    @Test
    void safeWithCompositeKey() {

        Entity entity = Entity.from("first", "second");

        session.save(entity);
        session.clear();

        // Should be retrievable
        entity = session.load(Entity.class, entity.getKey());
        session.clear();
        assertThat(entity).isNotNull();
        assertThat(entity.getKey()).isNotNull();

        // Also in a collection
        Collection<Entity> entities = session.loadAll(Entity.class, Collections.singletonList(entity.getKey()));
        session.clear();
        assertThat(entities).hasSize(1);

        // Also needs to be updateble
        entity = entities.iterator().next();
        entity.setSome("Some value");
        session.save(entity);
        session.clear();

        entity = session.load(Entity.class, entity.getKey());
        assertThat(entity.getSome()).isEqualTo("Some value");
    }
}
