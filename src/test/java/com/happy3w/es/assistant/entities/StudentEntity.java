package com.happy3w.es.assistant.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "test-index-student", type = "test-student-type", shards = 1, replicas = 0, refreshInterval = "-1")
public class StudentEntity {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Keyword)
    private String gender;

    @Field(type = FieldType.Integer)
    private int age;

    @Field(type = FieldType.Keyword)
    private String schoolName;

    @Field(type = FieldType.Integer)
    private int grade;

    @Field(type = FieldType.Integer)
    private int classNo;
}
