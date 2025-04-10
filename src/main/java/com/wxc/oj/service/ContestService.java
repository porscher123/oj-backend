package com.wxc.oj.service;

import com.wxc.oj.model.entity.Contest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxc.oj.model.vo.ContestVO;
import com.wxc.oj.queueMessage.ContestMessage;

/**
* @author 王新超
* @description 针对表【contest】的数据库操作Service
* @createDate 2025-03-24 21:58:05
*/
public interface ContestService extends IService<Contest> {
    void contestInStatus_0(Contest contest);
    void contestInStatus_1(ContestMessage contest);
    void contestInStatus_2(ContestMessage contest);
    ContestVO getContestVO(Contest contest);
}
