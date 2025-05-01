package com.wxc.oj.model.dto.submission;
import com.wxc.oj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


/**
 * 查询提交的DTO
 * 供前端进行筛选查询，可以根据
 * submission的ID，用户，语言，评测结果（ACCEPTED, WRONG ANSWER...）
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
