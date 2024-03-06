package com.wxc.oj.model.dto.submission;
import com.wxc.oj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


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

    private static final long serialVersionUID = 1L;
}
