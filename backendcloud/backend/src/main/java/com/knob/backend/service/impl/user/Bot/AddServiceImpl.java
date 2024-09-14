package com.knob.backend.service.impl.user.Bot;

import com.knob.backend.mapper.BotMapper;
import com.knob.backend.pojo.Bot;
import com.knob.backend.pojo.User;
import com.knob.backend.service.impl.utils.UserDetailsImpl;
import com.knob.backend.service.user.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> add(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loggedUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loggedUser.getUser();

        String title = data.get("title");
        String content = data.get("content");
        String description = data.get("description");

        Map<String, String> result = new HashMap<>();

        if(title == null || title.length() == 0) {
            result.put("error_message", "标题不能为空");
            return result;
        }
        if(title.length()  > 100) {
            result.put("error_message", "用户名长度不能大于100");
            return result;
        }
        if(description == null || description.length() == 0) {
            description = "这个用户很懒，什么也没留下~";
        }
        if(description != null && description.length()  > 1000) {
            result.put("error_message", "bot描述不能大于1000");
            return result;
        }

        if(content == null || content.length() == 0) {
            result.put("error_message", "代码不能为空");
            return result;
        }
        if(content.length()  > 10000) {
            result.put("error_message", "代码长度不能大于10000");
            return result;
        }

        Date now = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, now, now);

        botMapper.insert(bot);
        result.put("error_message", "success");
        return result;
    }
}
