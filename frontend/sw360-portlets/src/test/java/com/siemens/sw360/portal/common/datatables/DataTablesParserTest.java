/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.common.datatables;

import com.google.common.collect.ImmutableMap;
import com.siemens.sw360.portal.common.datatables.data.DataTablesColumn;
import com.siemens.sw360.portal.common.datatables.data.DataTablesOrder;
import com.siemens.sw360.portal.common.datatables.data.DataTablesParameters;
import com.siemens.sw360.portal.common.datatables.data.DataTablesSearch;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DataTablesParserTest {

    @Test
    public void testName() throws Exception {
        Map<String, String[]> parameterMap = getTestParameterMap();

        DataTablesParameters parameters = DataTablesParser.parametersFrom(parameterMap);

        assertThat(parameters, is(not(nullValue())));
        assertThat(parameters.getDraw(), is(1));

        assertThat(parameters.getLength(), is(10));
        assertThat(parameters.getStart(), is(5));

        DataTablesSearch search = parameters.getSearch();
        assertThat(search, is(equalTo(new DataTablesSearch("lookingFor", false))));

        List<DataTablesColumn> columns = parameters.getColumns();
        assertThat(columns, hasSize(1));

        DataTablesColumn column1 = columns.get(0);
        assertThat(column1.isSearchable(), is(true));
        assertThat(column1.getSearch(), is(equalTo(new DataTablesSearch("col0Sear", true))));

        List<DataTablesOrder> orders = parameters.getOrders();
        assertThat(columns, hasSize(1));

        DataTablesOrder order = orders.get(0);
        assertThat(order.getColumn(), is(0));
        assertThat(order.isAscending(), is(true));
    }

    private static Map<String, String[]> getTestParameterMap() {
        ImmutableMap.Builder<String, String[]> objectBuilder = ImmutableMap.builder();

        objectBuilder.put("draw", new String[]{"1"});

        objectBuilder.put("length", new String[]{"10"});
        objectBuilder.put("start", new String[]{"5"});

        objectBuilder.put("search[value]", new String[]{"lookingFor"});
        objectBuilder.put("search[regex]", new String[]{"false"});

        objectBuilder.put("order[0][column]", new String[]{"0"});
        objectBuilder.put("order[0][dir]", new String[]{"asc"});

        objectBuilder.put("columns[0][name]", new String[]{""});
        objectBuilder.put("columns[0][data]", new String[]{"abcd"});
        objectBuilder.put("columns[0][orderable]", new String[]{"false"});
        objectBuilder.put("columns[0][searchable]", new String[]{"true"});
        objectBuilder.put("columns[0][search][value]", new String[]{"col0Sear"});
        objectBuilder.put("columns[0][search][regex]", new String[]{"true"});

        return objectBuilder.build();
    }


    @Test
    public void testUnprefix() throws Exception {

        ImmutableMap.Builder<String, String[]> objectBuilder = ImmutableMap.builder();

        objectBuilder.put("pre", new String[]{"1"});

        objectBuilder.put("ppre", new String[]{"10"});
        objectBuilder.put("prea", new String[]{"5"});
        objectBuilder.put("preab", new String[]{"6"});
        objectBuilder.put("preac", new String[]{"7"});

        Map<String, String[]> map = objectBuilder.build();

        Map<String, String[]> unprefixed = DataTablesParser.unprefix(map, "pre");

        assertThat(unprefixed.keySet(), containsInAnyOrder("", "a", "ab", "ac"));

        assertThat(unprefixed, hasEntry("", new String[]{"1"}));
        assertThat(unprefixed, hasEntry("a", new String[]{"5"}));
        assertThat(unprefixed, hasEntry("ab", new String[]{"6"}));
        assertThat(unprefixed, hasEntry("ac", new String[]{"7"}));

    }
}