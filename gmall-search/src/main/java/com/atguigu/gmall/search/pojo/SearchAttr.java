package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class SearchAttr {
    //用户搜索可能要用的字段
    @Field(type= FieldType.Long)
    private Long attrId;
    @Field(type=FieldType.Keyword)
    private String attrName;
    @Field(type=FieldType.Keyword)
    private String attrValue;

}
