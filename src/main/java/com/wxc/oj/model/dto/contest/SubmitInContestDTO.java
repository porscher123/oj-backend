package com.wxc.oj.model.dto.contest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import lombok.Data;

@Data
public class SubmitInContestDTO extends SubmissionAddRequest {

    private Long contestId;
}
