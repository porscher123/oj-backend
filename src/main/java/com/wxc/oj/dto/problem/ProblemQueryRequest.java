package com.wxc.oj.dto.problem;

import com.wxc.oj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询请求
 * 可以根据什么查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProblemQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String title;

    private List<String> tags;
    private Long userId;
    private String level;


    private static final long serialVersionUID = 1L;
}