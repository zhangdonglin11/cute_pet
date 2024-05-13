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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/admin/audit-pet")
public class AutitPetControl {
    @Autowired
    private UserService userService;
    @Autowired
    private PetService petService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private Common common;

    /**
     * 获取宠物列表
     */
    @PostMapping("")
    public Result getPetList(@RequestBody HashMap<String,Object> condition) throws IllegalAccessException {
        QueryWrapper<Pet> petWrapper = new QueryWrapper<>();
        // 查询所有用户或者待审核用户 0/1/2 正常，审核、禁止
        if (condition.containsKey("audit") && Objects.equals(condition.get("audit"), 1)) {
            petWrapper.gt("status", 2);
            petWrapper.orderBy(true, false, "status");
        }else {
            petWrapper.gt("status", 0);
        }
        // 搜素框条件
        if (condition.containsKey("searchIpt") && condition.get("searchIpt")!=""){
            // 用户id
            if (Objects.equals(condition.get("searchType"),1)){
                User user = userService.getOne(new QueryWrapper<User>().eq("id", condition.get("searchIpt")));
                if (user==null) return new Result<>(1,"没有该用户！");
                petWrapper.eq("user_id",condition.get("searchIpt"));
            }
            // 用户电话
            if (Objects.equals(condition.get("searchType"),2)){
                User user = userService.getOne(new QueryWrapper<User>().eq("phone", condition.get("searchIpt")));
                if (user==null) return new Result<>(0,"没有该用户！");
                petWrapper.eq("user_id",user.getId());
            }
            // 宠物昵称
            if (Objects.equals(condition.get("searchType"),3)){
                petWrapper.like("pet_nick",condition.get("searchIpt").toString());
            }
        }
        // 页码
        int current = Integer.parseInt(condition.get("current").toString());
        int size = Integer.parseInt(condition.get("size").toString());

        Page<Pet> page = new Page<>(current, size);
        Page<Pet> PagePetList = petService.page(page, petWrapper);

        ArrayList<Object> petArr = new ArrayList<>();
        for (Pet pet : PagePetList.getRecords()) {
            HashMap<String, Object> petMap = new HashMap<>();
            // // 将对象的属性值赋值给petMap
            common.handleObject(pet, petMap);
            // // 查询用户头像昵称
            User newUser = userService.getById(pet.getUserId());
            petMap.put("userName",newUser.getNickname());
            petMap.put("userPic",newUser.getPic());

            // 宠物图片数组
            List<Picture> pictureList = pictureService.list(new QueryWrapper<Picture>().eq("pet_id", pet.getId()));
            ArrayList<String> picArr = new ArrayList<>();
            for (Picture picture:pictureList){
                picArr.add(picture.getPic());
            }
            petMap.put("petPic",picArr);

            // // 不好的评论数组
            ArrayList<Object> commentArr = new ArrayList<>();
            List<Comment> commentList = commentService.list(new QueryWrapper<Comment>().eq("pet_id",pet.getId()).gt("status", 1));
            for (Comment comment : commentList) {
                HashMap<String, Object> commentMap = new HashMap<>();
                common.handleObject(comment, commentMap);
                // 评论昵称，评论头像
                User commentUser = userService.getById(comment.getUserId());
                commentMap.put("userName",commentUser.getNickname());
                commentMap.put("userPic",commentUser.getPic());
                commentArr.add(commentMap);
            }
            petMap.put("commentArr",commentArr);
            // 把每条宠物的数据添加到petArr
            petArr.add(petMap);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("petArr",petArr);
        data.put("total",PagePetList.getTotal());
        data.put("size",PagePetList.getSize());
        data.put("current",PagePetList.getCurrent());
        data.put("pages",PagePetList.getPages());
        return new Result<>(data);
    }

    /**
     * 审核宠物
     */
    @PostMapping("/pet")
    public Result auditPet(@RequestBody HashMap<String,Object> fromData){
        System.out.println(fromData.toString());
        Pet pet = petService.getById((int) fromData.get("pid"));
        // 通过宠物
        if (pet!=null&&Objects.equals(fromData.get("type"),1)){
            pet.setStatus(1);
            petService.updateById(pet);
            return new Result<>(1,"操作成功");
        }
        // 删除宠物
        if (pet!=null&&Objects.equals(fromData.get("type"),2)){
            // 删除宠物所有的图片 这是自定义删除宠物图片复用的方法
            common.deletePicList("pet_id", pet.getId());
            // 删除宠物的评论
            QueryWrapper<Comment> eqCommentPetId = new QueryWrapper<Comment>().eq("pet_id", pet.getId());
            commentService.remove(eqCommentPetId);
            // 删除宠物的收藏
            QueryWrapper<Thumb> eqThumbPetId = new QueryWrapper<Thumb>().eq("pet_id", pet.getId());
            thumbService.remove(eqThumbPetId);
            // 删除宠物的信息
            petService.removeById(pet.getId());
            return new Result<>(1, "操作成功");
        }
        return new Result<>(0,"操作失败");
    }

    /**
     * 审核评论
     */
    @PostMapping("/comment")
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

    /**
     * 修改用户状态
     */
    @PostMapping("/user")
    public Result forbidUser(@RequestBody HashMap<String,Object> fromData) {
        User user = userService.getById((int) fromData.get("uid"));
        // 通过宠物
        if (user != null) {
            user.setStatus(2);
            userService.updateById(user);
            return new Result<>(1, "操作成功");
        }
        return new Result<>(0, "操作失败");
    }

}
