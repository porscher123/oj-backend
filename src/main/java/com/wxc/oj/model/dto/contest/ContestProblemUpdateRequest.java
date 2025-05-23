package com.wxc.oj.model.dto.contest;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ContestProblemUpdateRequest implements Serializable {
    private Long contestId;


    List<ContestProblemDTO> problems;

    private static final long serialVersionUID = 1L;
}
