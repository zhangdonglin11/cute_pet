package com.example.cute_pet.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.Comment;
import com.example.cute_pet.domain.Topic;
import com.example.cute_pet.service.CommentService;
import com.example.cute_pet.service.UserService;
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
@RequestMapping("api/admin/audit-comment")
public class AuditCommentControl {
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private Common common;

    @PostMapping("")
    public Result getCommentList(@RequestBody HashMap<String,Object> condition) throws IllegalAccessException {
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        if (Objects.equals( condition.get("audit"),1)){
            commentQueryWrapper.gt("status",0);
            commentQueryWrapper.orderByDesc("status");
        }else {
            commentQueryWrapper.orderByDesc("create_time");
        }

        // 页码
        int current = Integer.parseInt(condition.get("current").toString());
        int size = Integer.parseInt(condition.get("size").toString());

        Page<Comment> page = new Page<>(current, size);
        Page<Comment> PageCommentList = commentService.page(page, commentQueryWrapper);

        ArrayList<Object> commentArr = new ArrayList<>();
        for (Comment comment : PageCommentList.getRecords()){
            HashMap<String, Object> commentMap = new HashMap<>();
            // // 将对象的属性值赋值给petMap
            common.handleObject(comment, commentMap);

            commentMap.put("nickname",userService.getById(comment.getUserId()).getNickname());
            commentMap.put("pic",userService.getById(comment.getUserId()).getPic());
            commentArr.add(commentMap);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("commentArr",commentArr);
        data.put("total",PageCommentList.getTotal());
        data.put("size",PageCommentList.getSize());
        data.put("current",PageCommentList.getCurrent());
        data.put("pages",PageCommentList.getPages());
        return new Result<>(data);
    }

    // 删除评论
    @PostMapping("/update")
    public Result auditComment(@RequestBody HashMap<String,Object> fromData){
        System.out.println(fromData.toString());

        Comment comment = commentService.getById(fromData.get("cid").toString());
        // 通过评论
        if (comment!=null&&Objects.equals(fromData.get("type"),1)){
            comment.setStatus(0);
            commentService.updateById(comment);
            return new Result<>(1,"操作成功");
        }
        // 删除评论
        if (comment!=null&&Objects.equals(fromData.get("type"),2)){
            commentService.removeById(comment.getId());
            return new Result<>(1,"操作成功");
        }
        return new Result<>(0,"操作失败");
    }
}
