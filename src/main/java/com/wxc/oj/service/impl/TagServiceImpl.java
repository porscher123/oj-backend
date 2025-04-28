package com.wxc.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.model.entity.Tag;
import com.wxc.oj.service.TagService;
import com.wxc.oj.mapper.TagMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 王新超
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2024-03-08 20:51:25
*/
@Service
public class
TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

    @Resource
    private TagMapper tagMapper;
    public List<Tag> listTagsByProblemId(Long problemId) {
        List<Tag> tags = tagMapper.listTagsByProblemId(problemId);
        return tags;
    }
}




