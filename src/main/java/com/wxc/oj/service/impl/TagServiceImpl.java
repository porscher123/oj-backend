package com.wxc.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.model.entity.Tag;
import com.wxc.oj.service.TagService;
import com.wxc.oj.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 王新超
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2024-03-07 19:55:49
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




