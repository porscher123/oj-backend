package com.wxc.oj.mapper;

import com.wxc.oj.model.po.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 王新超
* @description 针对表【tag】的数据库操作Mapper
* @createDate 2024-03-08 20:51:25
* @Entity com.wxc.oj.model.entity.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {


    @Select("select * from tag where id in (select tag_id from problem_tag where problem_id = #{problemId})")
    List<Tag> listTagsByProblemId(Long problemId);


//    @Select("select id from tag where name = #{tagName}")
//    Long findByName(String tagName);



//    @Select("select problem_id from problem_tag where tag_id in ${tagIds}")
    List<Long> getProblemIdsByTagIds(List<Integer> tagIds);


    List<Long> getProblemIdsByTagNames(List<String> tagNames);
}




