package com.wxc.oj.model.dto.contest;

import lombok.Data;

import java.util.Date;

@Data
public class ContestBaseUpdateRequest {
    private Long contestId;

    private String title;

    private String description;

//    private Date startTime;

    private Integer duration;

    private Boolean isPublic;

    private Long hostId;
}
