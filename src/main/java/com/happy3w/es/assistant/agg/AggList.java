/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.happy3w.es.assistant.agg;

import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * The Assistant will return a List result from Aggregation
 * @author yushan gao
 * @param <T> The data type in List
 */
public abstract class AggList<T> extends AggAssistant<List<T>> {
    private BiFunction<String, Map<String, Object>, T> createResult;
    public AggList(String name, BiFunction<String, Map<String, Object>, T> createResult, AggAssistant... subAggs) {
        super(name, subAggs);
        this.createResult = createResult;
    }

    @Override
    public List<T> collectValue(Aggregation aggregation) {
        List<T> result = new ArrayList<>();

        List buckets = null;
        if (aggregation instanceof StringTerms) {
            buckets = ((StringTerms) aggregation).getBuckets();
        } else if (aggregation instanceof LongTerms) {
            buckets = ((LongTerms) aggregation).getBuckets();
        } else {
            throw new UnsupportedOperationException("Unsupported type (" + aggregation.getClass() + ") for aggList:" + getName());
        }

        Map<String, Object> subItems = new HashMap<>();
        for (Object resultItemObj : buckets) {
            Terms.Bucket resultItem = (Terms.Bucket) resultItemObj;
            String value = resultItem.getKeyAsString();
            subItems.clear();
            subItems.put("docCount", resultItem.getDocCount());
            if (getSubAggs() != null) {
                for (AggAssistant assistant : getSubAggs()) {
                    Object subValue = assistant.collectValue(
                            resultItem.getAggregations()
                                    .asMap()
                                    .get(assistant.getName()));
                    subItems.put(assistant.getName(), subValue);
                }
            }
            T rowValue = createResult.apply(value, subItems);
            if (rowValue != null) {
                result.add(rowValue);
            }
        }
        return result;
    }

    public List<T> readValue(Map<String, Object> subItems) {
        return (List<T>) subItems.get(this.getName());
    }

    public List<T> readValue(Map<String, Object> subItems, List<T> defaultValue) {
        return (List<T>) subItems.getOrDefault(this.getName(), defaultValue);
    }

    public static long getDocCount(Map<String, Object> subItems) {
        Long v = (Long) subItems.getOrDefault("docCount", 0l);
        return v.longValue();
    }


    public static <T> AggList<T> listField(String name, String fieldCode,
                                           BiFunction<String, Map<String, Object>, T> createResult,
                                           AggAssistant... subAggs) {
        return new AggList<T>(name, createResult, subAggs) {
            @Override
            protected AbstractAggregationBuilder createBuilder() {
                return AggregationBuilders.terms(getName())
                        .field(fieldCode)
                        .size(1000);
            }
        };
    }

    public static <T> AggList<T> listScript(String name, Script script,
                                            BiFunction<String, Map<String, Object>, T> createResult,
                                            AggAssistant... subAggs) {
        return new AggList<T>(name, createResult, subAggs) {
            @Override
            protected AbstractAggregationBuilder createBuilder() {
                return AggregationBuilders.terms(getName())
                        .script(script)
                        .size(1000);
            }
        };
    }

    public static <T> AggList<T> listScriptId(String name, String scriptId,
                                              BiFunction<String, Map<String, Object>, T> createResult,
                                              AggAssistant... subAggs) {
        return new AggList<T>(name, createResult, subAggs) {
            @Override
            protected AbstractAggregationBuilder createBuilder() {
                return AggregationBuilders.terms(getName())
                        .script(new Script(ScriptType.STORED, Script.DEFAULT_SCRIPT_LANG, scriptId, new HashMap<>()))
                        .size(1000);
            }
        };
    }

    public static <T> AggList<T> listScriptCode(String name, String scriptCode,
                                                BiFunction<String, Map<String, Object>, T> createResult,
                                                AggAssistant... subAggs) {
        return new AggList<T>(name, createResult, subAggs) {
            @Override
            protected AbstractAggregationBuilder createBuilder() {
                return AggregationBuilders.terms(getName())
                        .script(new Script(scriptCode))
                        .size(1000);
            }
        };
    }
}
