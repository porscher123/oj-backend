package com.wxc.oj.model.dto.contest;

import com.wxc.oj.common.PageRequest;
import lombok.Data;

import java.io.Serializable;


@Data
public class ContestSubmissionListDTO extends PageRequest implements Serializable {
    private Long contestId;
    private Long userId;
    private static final long serialVersionUID = 1L;
}
