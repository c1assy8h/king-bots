package com.knob.backend.service.impl.user.Bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.knob.backend.mapper.BotMapper;
import com.knob.backend.pojo.Bot;
import com.knob.backend.pojo.User;
import com.knob.backend.service.impl.utils.UserDetailsImpl;
import com.knob.backend.service.user.bot.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetListServiceImpl  implements GetListService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public List<Bot> getlist() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        QueryWrapper<Bot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        return botMapper.selectList(queryWrapper);
    }
}
