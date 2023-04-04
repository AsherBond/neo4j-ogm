/*
 * Copyright (c) 2002-2023 "Neo4j,"
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
package org.neo4j.ogm.typeconversion;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Gerrit Meier
 */
public class DateConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.date");
    private static final ClassInfo memoInfo = metaData.classInfo("Memo");
    SimpleDateFormat simpleDateISO8601format = new SimpleDateFormat(DateString.ISO_8601);

    @Test
    void assertFieldDateConversionToISO8601FormatByDefault() {
        FieldInfo fieldInfo = memoInfo.propertyField("recorded");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class)).isTrue();
        assertThat(attributeConverter.toGraphProperty(new Date(0))).isEqualTo("1970-01-01T00:00:00.000Z");
    }

    @Test
    void assertFieldDateConversionWithUserDefinedFormat() {
        FieldInfo fieldInfo = memoInfo.propertyField("actioned");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class)).isTrue();
        assertThat(attributeConverter.toGraphProperty(new Date(0))).isEqualTo("1970-01-01");
    }

    @Test
    void assertFieldDateLongConversion() {
        FieldInfo fieldInfo = memoInfo.propertyField("closed");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateLongConverter.class)).isTrue();
        Date date = new Date(0);
        Long value = (Long) attributeConverter.toGraphProperty(date);
        assertThat(value).isEqualTo(new Long(0));
    }

    @Test
    void assertFieldCustomTypeConversion() {
        FieldInfo fieldInfo = memoInfo.propertyField("approved");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(new Date(1234567890123L))).isEqualTo("20090213113130");
    }

    @Test
    void assertConvertingNullGraphPropertyWorksCorrectly() {
        FieldInfo methodInfo = memoInfo.propertyField("approved");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute(null)).isEqualTo(null);
    }

    @Test
    void assertConvertingNullAttributeWorksCorrectly() {
        FieldInfo methodInfo = memoInfo.propertyField("approved");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(null)).isEqualTo(null);
    }

    // DATAGRAPH-550
    @Test
    void assertArrayFieldDateConversionToISO8601FormatByDefault() {
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date[] dates = new Date[]{new Date(0), new Date(20000)};
        FieldInfo fieldInfo = memoInfo.propertyField("escalations");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateArrayStringConverter.class)).isTrue();
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertThat(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"))
            .isTrue();
        assertThat(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1]
            .equals(simpleDateISO8601format.format(new Date(20000)))).isTrue();
    }

    // DATAGRAPH-550
    @Test
    void assertConvertingNullArrayGraphPropertyWorksCorrectly() {
        FieldInfo methodInfo = memoInfo.propertyField("escalations");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute(null)).isEqualTo(null);
    }

    // DATAGRAPH-550
    @Test
    void assertConvertingNullArrayAttributeWorksCorrectly() {
        FieldInfo methodInfo = memoInfo.propertyField("escalations");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(null)).isEqualTo(null);
    }

    // DATAGRAPH-550
    @Test
    void assertCollectionFieldDateConversionToISO8601FormatByDefault() {
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(0));
        dates.add(new Date(20000));
        FieldInfo fieldInfo = memoInfo.propertyField("implementations");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateCollectionStringConverter.class)).isTrue();
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertThat(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"))
            .isTrue();
        assertThat(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1]
            .equals(simpleDateISO8601format.format(new Date(20000)))).isTrue();
    }

    // DATAGRAPH-550
    @Test
    void assertConvertingNullCollectionGraphPropertyWorksCorrectly() {
        FieldInfo methodInfo = memoInfo.propertyField("implementations");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute(null)).isEqualTo(null);
    }

    // DATAGRAPH-550
    @Test
    void assertConvertingNullCollectionAttributeWorksCorrectly() {
        FieldInfo methodInfo = memoInfo.propertyField("implementations");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(null)).isEqualTo(null);
    }

    // DATAGRAPH-424
    @Test
    void assertFieldDateConversionWithExplicitAnnotation() {
        FieldInfo fieldInfo = memoInfo.propertyField("modified");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class)).isTrue();
        assertThat(attributeConverter.toGraphProperty(new Date(0))).isEqualTo("1970-01-01T00:00:00.000Z");
    }

    // DATAGRAPH-424
    @Test
    void assertFieldDateConversionWithExplicitAnnotationWorksForNullGraphValue() {
        FieldInfo fieldInfo = memoInfo.propertyField("modified");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class)).isTrue();
        assertThat(attributeConverter.toEntityAttribute(null)).isNull();
    }

    // DATAGRAPH-424
    @Test
    void assertFieldDateConversionWithExplicitAnnotationFailsForEmptyGraphValue() {
        assertThrows(RuntimeException.class, () -> {
            FieldInfo fieldInfo = memoInfo.propertyField("modified");
            assertThat(fieldInfo.hasPropertyConverter()).isTrue();
            AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
            assertThat(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class)).isTrue();
            assertThat(attributeConverter.toEntityAttribute("")).isNull();
        });
    }

    // DATAGRAPH-424
    @Test
    void assertFieldLenientDateConversionWithExplicitAnnotationWorksForEmptyGraphValue() {
        FieldInfo fieldInfo = memoInfo.propertyField("legacyDate");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class)).isTrue();
        assertThat(attributeConverter.toEntityAttribute("")).isNull();
    }
}
