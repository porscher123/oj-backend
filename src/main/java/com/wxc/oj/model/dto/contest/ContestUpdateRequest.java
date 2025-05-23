package com.wxc.oj.model.dto.contest;


import lombok.Data;


@Data
public class ContestUpdateRequest extends ContestAddRequest{

    private Long contestId;

}
