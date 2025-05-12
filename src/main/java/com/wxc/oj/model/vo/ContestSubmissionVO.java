package com.wxc.oj.model.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ContestSubmissionVO extends SubmissionVO implements Serializable {
    private Long contestId;
    private Date submissionTime;
    private Integer problemIndex;
    private static final long serialVersionUID = 1L;
}
