package com.wxc.oj.service;

import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxc.oj.model.vo.ProblemVO;

import java.util.List;

/**
* @author 王新超
* @description 针对表【tag】的数据库操作Service
* @createDate 2024-03-08 20:51:25
*/
public interface TagService extends IService<Tag> {

    List<Tag> listTagsByProblemId(Long problemId);

    /**
     * 通过tags的名称去查
     * ["二分", "BFS", "DFS"]
     * @param tagId
     * @return
     */
    List<Long> getProblemIdsByTagNames(List<String> tagNames);
}
