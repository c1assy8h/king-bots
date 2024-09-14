package com.knob.backend.service.impl.ranklist;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knob.backend.mapper.UserMapper;
import com.knob.backend.pojo.User;
import com.knob.backend.service.ranklist.GetRanklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetRanklistServiceImpl implements GetRanklistService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject getList(Integer page) {
        IPage<User> userIPage = new Page<>(page, 10);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("rating");
        List<User> userList = userMapper.selectPage(userIPage, queryWrapper).getRecords();
        for(User user : userList){
            user.setPassword("");
        }
        JSONObject resp = new JSONObject();
        resp.put("users", userList);
        resp.put("users_count", userMapper.selectCount(null));

        return resp;
    }
}
