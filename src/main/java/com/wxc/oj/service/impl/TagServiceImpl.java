package com.wxc.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Tag;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.service.TagService;
import com.wxc.oj.mapper.TagMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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


    /**
     * 通过tags的名称去查
     * ["二分", "BFS", "DFS"]
     * @param tags
     * @return 返回题目的id
     */
    public List<Integer> getTagIdsByTagsName(List<String> tags) {
        LambdaQueryWrapper<Tag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(!tags.isEmpty(), Tag::getName, tags);
        List<Tag> tags1 = tagMapper.selectList(lambdaQueryWrapper);
        List<Integer> collect = tags1.stream().map(Tag::getId).collect(Collectors.toList());
        return collect;
    }


    public List<Long> getProblemIdsByTagIds(List<Integer> tagIds) {
        List<Long> problemIdsByTagIds = tagMapper.getProblemIdsByTagIds(tagIds);
        return problemIdsByTagIds;
    }


    public List<Long> getProblemIdsByTagNames(List<String> tagNames) {
        List<Integer> tagIdsByTagsName = this.getTagIdsByTagsName(tagNames);
        List<Long> problemIds = this.getProblemIdsByTagIds(tagIdsByTagsName);
        return problemIds;
    }
}




