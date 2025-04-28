package com.wxc.oj.controller;

import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.model.entity.Tag;
import com.wxc.oj.service.TagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("tag")
@Slf4j
public class TagController {

    @Resource
    private TagService tagService;


    @GetMapping("list")
    public BaseResponse<List<Tag>> listTags() {
        List<Tag> list = tagService.list();
        return ResultUtils.success(list);
    }



}
