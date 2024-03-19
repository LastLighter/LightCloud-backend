package com.lastlight.entity.dto;

import com.lastlight.annotation.Token;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileQueryDto extends QueryDto{
    public static final Integer MAX_PAGE_SIZE = 500;
    private Long userId;
    private String fileMd5;
    private String fileParent;
    private Integer fileCategory;
    private String name;
    @Token
    private String token;

    public FileQueryDto(Integer pageSize, Integer offset, String fileMd5) {
        setPageSize(pageSize);
        setOffset(offset);
        this.fileMd5 = fileMd5;
    }
}
