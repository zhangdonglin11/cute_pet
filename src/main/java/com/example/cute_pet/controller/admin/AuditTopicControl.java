package com.example.cute_pet.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.*;
import com.example.cute_pet.service.*;
import com.example.cute_pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/admin/audit-topic")
public class AuditTopicControl {
    @Autowired
    private UserService userService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private Common common;

    /**
     * 获取话题列表
     */
    @PostMapping("")
    public Result getPetList(@RequestBody HashMap<String,Object> condition) throws IllegalAccessException {
        QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
        topicWrapper.ne("status", 0);
        // 查询所有用户或者待审核用户 0/1/2 正常，审核、禁止
        if (condition.containsKey("audit") && Objects.equals(condition.get("audit"), 1)) {
            topicWrapper.gt("status", 2);
        }
        // 搜素框条件
        if (condition.containsKey("searchIpt") && condition.get("searchIpt")!=""){
            // 话题标题
            if (Objects.equals(condition.get("searchType"),1)){
                topicWrapper.like("title",condition.get("searchIpt").toString());
            }
            // 话题内容
            if (Objects.equals(condition.get("searchType"),2)){
                topicWrapper.like("content",condition.get("searchIpt").toString());
            }
            // 用户id
            if (Objects.equals(condition.get("searchType"),3)){
                topicWrapper.like("user_id",condition.get("searchIpt"));
            }
        }
        // 页码
        int current = Integer.parseInt(condition.get("current").toString());
        int size = Integer.parseInt(condition.get("size").toString());

        Page<Topic> page = new Page<>(current, size);
        Page<Topic> PagePetList = topicService.page(page, topicWrapper);

        ArrayList<Object> topicArr = new ArrayList<>();
        for (Topic topic : PagePetList.getRecords()) {
            HashMap<String, Object> topicMap = new HashMap<>();
            // // 将对象的属性值赋值给petMap
            common.handleObject(topic, topicMap);
            // // 查询用户头像昵称
            User newUser = userService.getById(topic.getUserId());
            topicMap.put("userName",newUser.getNickname());
            topicMap.put("userPic",newUser.getPic());

            // 宠物图片数组
            List<Picture> pictureList = pictureService.list(new QueryWrapper<Picture>().eq("topic_id", topic.getId()));
            ArrayList<String> picArr = new ArrayList<>();
            for (Picture picture:pictureList){
                picArr.add(picture.getPic());
            }
            topicMap.put("topicPic",picArr);

            // // 不好的评论数组
            ArrayList<Object> commentArr = new ArrayList<>();
            List<Comment> commentList = commentService.list(new QueryWrapper<Comment>().eq("topic_id",topic.getId()).gt("status", 1));
            for (Comment comment : commentList) {
                HashMap<String, Object> commentMap = new HashMap<>();
                common.handleObject(comment, commentMap);
                // 评论昵称，评论头像
                User commentUser = userService.getById(comment.getUserId());
                commentMap.put("userName",commentUser.getNickname());
                commentMap.put("userPic",commentUser.getPic());
                commentArr.add(commentMap);
            }
            topicMap.put("commentArr",commentArr);
            // 把每条宠物的数据添加到petArr
            topicArr.add(topicMap);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("topicArr",topicArr);
        data.put("total",PagePetList.getTotal());
        data.put("size",PagePetList.getSize());
        data.put("current",PagePetList.getCurrent());
        data.put("pages",PagePetList.getPages());
        return new Result<>(data);
    }

    /**
     * 审核宠物
     */
    @PostMapping("/topic")
    public Result auditPet(@RequestBody HashMap<String,Object> fromData){
        System.out.println(fromData.toString());
        Topic topic = topicService.getById((int) fromData.get("tid"));
        // 通过宠物
        if (topic!=null&&Objects.equals(fromData.get("type"),1)){
            topic.setStatus(1);
            topicService.updateById(topic);
            return new Result<>(1,"操作成功");
        }
        // 删除宠物
        if (topic!=null&&Objects.equals(fromData.get("type"),2)){
            // 删除宠物所有的图片 这是自定义删除宠物图片复用的方法
            common.deletePicList("topic_id", topic.getId());
            // 删除宠物的评论
            QueryWrapper<Comment> eqCommentPetId = new QueryWrapper<Comment>().eq("topic_id", topic.getId());
            commentService.remove(eqCommentPetId);
            // 删除宠物的收藏
            QueryWrapper<Thumb> eqThumbPetId = new QueryWrapper<Thumb>().eq("topic_id", topic.getId());
            thumbService.remove(eqThumbPetId);
            // 删除宠物的信息
            topicService.removeById(topic.getId());
            return new Result<>(1, "操作成功");
        }
        return new Result<>(0,"操作失败");
    }
}
