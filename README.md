Happy3w Elasticsearch Assistant
=========================

This library is used to make it more easy to access Elasticsearch.

Quick Start
-----------

### Maven configuration

Add the Maven dependency:

```xml
<dependency>
    <groupId>com.happy3w</groupId>
    <artifactId>es-assistant</artifactId>
    <version>x.y.z.RELEASE</version>
</dependency>
```

### Gradle configuration

Add the Gradle dependency:

```
compile('com.happy3w:es-assistant:x.y.z.RELEASE')
```

### AggAssistant

Statistic data using AggAssistant

```java
        class SchoolStatItem {
          private String schoolName;
          private int studentCount;
          private double avgAge;
        }

        @Autowired
        private ElasticsearchTemplate elasticsearchTemplate;

        // Create an aggAsistant for aggregation all
        AggValue avgAge = AggValue.value("avgAge", "age", AggregationBuilders::avg);
        AggList<SchoolStatItem> schoolAggAssistant = AggList.listField(
          "schoolName", // The name of this agg which is used like an id;
          "schoolName", // The field to be agg in type student-type
          (value, subItems) -> new SchoolStatItem(
            value,                                // A value of agg field
            (int) AggList.getDocCount(subItems),  // The document count which match this agg criteria
            avgAge.readValue(subItems)),          // Get the sub agg result
          avgAge);  // Sub aggs of this agg

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
          .withQuery(matchAllQuery())
          .withSearchType(SearchType.DEFAULT)
          .withIndices("index-student").withTypes("student-type")
          .addAggregation(schoolAggAssistant.toAggBuilder())
          .build();
        List<SchoolStatItem> schoolStatItems = elasticsearchTemplate.query(searchQuery,
          new AggAssistantListExtractor<>(schoolAggAssistant));
```
