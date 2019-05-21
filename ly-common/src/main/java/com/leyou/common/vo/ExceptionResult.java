package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

@Data
public class ExceptionResult {
    private int status;
    private String message;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum em){
        this.message=em.getMsg();
        this.status=em.getCode();
        this.timestamp=System.currentTimeMillis();
    }
}
