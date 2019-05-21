package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.net.UnknownServiceException;
import java.util.List;

/**
 * @Author: cuzz
 * @Date: 2018/11/5 13:47
 * @Description:
 */

@Data
@Table(name = "tb_spec_group")
public class SpecGroup {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long cid;
    private String name;

    @Transient
    private List<SpecParam> params;
}
