package com.wxc.oj.model.dto.contest;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ContestAddRequest implements Serializable {
    private String title;

    private String description;

    private Date startTime;

    private Integer duration;

    private Integer isPublic;

    private List<Long> problems;

    private static final long serialVersionUID = 1L;
}
