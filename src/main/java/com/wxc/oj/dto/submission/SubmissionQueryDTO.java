package com.wxc.oj.dto.submission;
import com.wxc.oj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


/**
 * 查询提交的DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmissionQueryDTO extends PageRequest implements Serializable {

    private Long problemId;

    private Long userId;

    private String language;

    private String JudgeResult;


    private Date createTime;


    private static final long serialVersionUID = 1L;
}
