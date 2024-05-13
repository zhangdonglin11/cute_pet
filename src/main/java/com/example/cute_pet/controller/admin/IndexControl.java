package com.example.cute_pet.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cute_pet.domain.Comment;
import com.example.cute_pet.domain.Pet;
import com.example.cute_pet.domain.Topic;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.service.CommentService;
import com.example.cute_pet.service.PetService;
import com.example.cute_pet.service.TopicService;
import com.example.cute_pet.service.UserService;
import com.example.cute_pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@RestController
@RequestMapping("api/admin/index")
public class IndexControl {
    @Autowired
    private PetService petService;
    @Autowired
    private UserService userService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private CommentService commentService;

    @GetMapping("")
    public Result getTest(@RequestParam("time") int time) {
        // 1.根据请求的时间返回总宠物数量，总用户、总话题/
        // 获取当前日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 获取前7天的日期
        LocalDate lastDays = currentDateTime.minusDays(time).toLocalDate();
        // 构造查询条件 按时间段查询宠物、用户、话题
        QueryWrapper<Pet> queryWrapperPet = new QueryWrapper<Pet>()
                .gt("status",0)
                .apply("create_time >= {0} and create_time <= {1}",
                        lastDays.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                        currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        QueryWrapper<User> queryWrapperUser = new QueryWrapper<User>()
                .apply("create_time >= {0} and create_time <= {1}",
                        lastDays.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                        currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        QueryWrapper<Topic> queryWrapperTopic = new QueryWrapper<Topic>()
                .in("status",1,2,3)
                .apply("create_time >= {0} and create_time <= {1}",
                        lastDays.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                        currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 宠物数量
        int petCount = petService.list(queryWrapperPet).size();
        // 用户数量
        int userCount = userService.list(queryWrapperUser).size();
       // 话题数量
        int topicCount = topicService.list(queryWrapperTopic).size();

        // 宠物活跃度  一个宠物10点活跃+评论一条1点活跃 一定时间段的的宠物评论
        QueryWrapper<Comment> queryWrapperPetComment = new QueryWrapper<Comment>()
                .isNull("pet_id")
                .apply("create_time >= {0} and create_time <= {1}",
                        lastDays.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                        currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
       int petActive = petCount*5+commentService.list(queryWrapperPetComment).size();

        // 话题活跃度 一个话题10点活跃+评论一条1点活跃 一定时间段的的话题评论
        QueryWrapper<Comment> queryWrapperTopicComment = new QueryWrapper<Comment>()
                .isNull("topic_id")
                .apply("create_time >= {0} and create_time <= {1}",
                        lastDays.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                        currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        int topicActive = topicCount*5+commentService.list(queryWrapperTopicComment).size();


        //  昨天和今天的活跃。(总宠物数量、总话题)的评论
        // 获取昨天的数据
        LocalDate yesterday = currentDateTime.minusDays(1).toLocalDate();
        // 构造查询条件
        QueryWrapper<Comment> queryWrapperYesterday = new QueryWrapper<Comment>()
                .apply("create_time >= {0} and create_time < {1}",
                yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
        int ActiveYesterday = commentService.list(queryWrapperYesterday).size();

        // 获取今天的数据
        LocalDate today = currentDateTime.toLocalDate();
        // 构造查询条件
        QueryWrapper<Comment> queryWrapperToday = new QueryWrapper<Comment>()
                .gt("create_time",today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));

        int ActiveToday = commentService.list(queryWrapperToday).size();

        HashMap<String, Object> data = new HashMap<>();
        data.put("petCount",petCount);
        data.put("userCount",userCount);
        data.put("topicCount",topicCount);
        data.put("petActive",petActive);
        data.put("topicActive",topicActive);
        data.put("ActiveYesterday",ActiveYesterday);
        data.put("ActiveToday",ActiveToday);
        return new Result<>(data);
    }
}
