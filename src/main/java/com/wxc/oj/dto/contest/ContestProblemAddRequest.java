package com.wxc.oj.dto.contest;


import lombok.Data;

import java.io.Serializable;

@Data
public class ContestProblemAddRequest implements Serializable {


    private Long problemId;

    private Long contestId;

    private Integer index;
    private static final long serialVersionUID = 1L;
}
