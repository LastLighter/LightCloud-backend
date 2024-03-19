package com.lastlight.entity.dto;

import lombok.Data;

@Data
public class QueryDto {
    private Integer page;
    private Integer pageSize;
    private Integer offset;
    public void defaultSize(){
        setOffset(0);
        setPageSize(20);
    }
}
