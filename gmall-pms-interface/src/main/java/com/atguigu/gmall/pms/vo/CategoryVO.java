package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CategoryVO extends CategoryEntity {
    private List<CategoryEntity> subs;
}


