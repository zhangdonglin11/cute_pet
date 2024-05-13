package com.example.cute_pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.cute_pet.domain.Comment;
import com.example.cute_pet.domain.Picture;
import com.example.cute_pet.domain.Thumb;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.service.CommentService;
import com.example.cute_pet.service.PictureService;
import com.example.cute_pet.service.ThumbService;
import com.example.cute_pet.service.UserService;
import com.example.cute_pet.util.ImageUtil;
import com.example.cute_pet.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;


import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 这是一些复用的方法
 */

@Component
public class Common {
    @Autowired
    private PictureService pictureService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;
    @Autowired
    private ThumbService thumbService;

    // 处理用户数据
    public HashMap<String, Object> handleProfile(User newUser) {
        HashMap<String, Object> profile = new HashMap<>();
        profile.put("id", newUser.getId());
        profile.put("nickname", newUser.getNickname());
        profile.put("phone", newUser.getPhone());
        profile.put("status", newUser.getStatus());
        profile.put("pic", newUser.getPic());
        profile.put("sex", newUser.getSex());
        profile.put("token", TokenUtils.sign(newUser));
        return profile;
    }

    // 将对象遍历成map
    public void handleObject(Object handleObj, HashMap<String, Object> resultMap) throws IllegalAccessException {
        // 循环把对象的属性和值赋值新的map集合
        Field[] fields = handleObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            resultMap.put(field.getName(), field.get(handleObj));
        }
    }

    /**
     * 查询用户
     */
    public User getUser(int uid){
        return userService.getById(uid);
    }

    /**
     * 1.获取宠物图片数组 纯图片链接字符串
     *  type传值{pet_id/topic_uid}  id 为话题id或者宠物id
     */
    public ArrayList<String> getPicList(String type,int id) {
        ArrayList<String> picList = new ArrayList<>();
        QueryWrapper<Picture> eqPid = new QueryWrapper<Picture>().eq(type, id);
        List<Picture> petPicList = pictureService.list(eqPid);
        petPicList.forEach(item -> {
            picList.add(item.getPic());
        });
        return picList;
    }


    /**
     * 2.删除图片数组 type传值{pet_id/topic_uid}
     */
    public void deletePicList(String type,int id) {
        // 删除该宠物的所有图片
        QueryWrapper<Picture> eqId = new QueryWrapper<Picture>().eq(type, id);
        List<Picture> picList = pictureService.list(eqId);
        picList.forEach(item -> {
            // 获取数据库图片网络地址
            String picHttpAddress = pictureService.getById(item.getId()).getPic();
            deleteFileImg(picHttpAddress);
            // 根据id照片的删除记录
            boolean b = pictureService.removeById(item.getId());
            System.out.println("删除记录" + b);
        });
    }

    // 删除图片文件的方法，传网络图片路径删除图片文件
    public void deleteFileImg(String picHttpAddress) {
        if (!StringUtils.isEmpty(picHttpAddress)) {
            // 获取文件名
            String picFileName = picHttpAddress.substring(picHttpAddress.indexOf("image/") + 6);
            // 生成图片物理路径
            String imageFilePath = ImageUtil.getNewImagePath(picFileName, "image/");
            System.out.println("oldImagePath" + imageFilePath);
            // 删除图片 判断空不能删除
            if (!imageFilePath.isEmpty()) {
                File file = new File(imageFilePath);
                if (file.exists()) {
                    FileSystemUtils.deleteRecursively(file);
                } else {
                    System.out.println("目标文件不存在");
                }
            }
        }
    }

    /**
     * 3.获取所有评论
     * type{pet_id/topic_id} id{宠物/话题 id}
     */
    public HashMap<String, Object> getPetComment(String type,int id) {
        // 获取评论
        HashMap<String, Object> commentData = new HashMap<>();
        // 1. 获取评论数量
        QueryWrapper<Comment> commentPid = new QueryWrapper<Comment>().eq(type, id);
        int comCount = commentService.list(commentPid).size();
        commentData.put("count", comCount);

        // 2.1查询该宠物根评论
        QueryWrapper<Comment> commentWp = new QueryWrapper<Comment>().eq(type, id).eq("level", 0);
        List<Comment> rootCommentList = commentService.list(commentWp);

        // 2.2循环根评论添加子评论
        ArrayList<Object> commentArr = new ArrayList<>();
        rootCommentList.forEach(item -> {
            // 2.2.1设置单个条宠物评论
            HashMap<String, Object> comment = new HashMap<>();
            comment.put("id", item.getId());
            comment.put("content", item.getContent());
            comment.put("nickName", userService.getById(item.getUserId()).getNickname());
            comment.put("userId", item.getUserId());
            comment.put("level", item.getLevel());
            comment.put("createTime", item.getCreateTime());
            comment.put("avatar", userService.getById(item.getUserId()).getPic());

            // 2.2.2 通过根评论id,查询子评论 保存在下面数组
            ArrayList<Object> childCommentArr = new ArrayList<>();
            // 查询根评论所有的子评论
            QueryWrapper<Comment> eqRootId = new QueryWrapper<Comment>().eq("root_id", item.getId());
            List<Comment> rootChildComment = commentService.list(eqRootId);
            // 遍历所有子评论并设置好数据格式
            rootChildComment.forEach(cItem -> {
                // 设置子评论
                HashMap<String, Object> childComment = new HashMap<>();
                childComment.put("id", cItem.getId());  // 评论id
                childComment.put("content", cItem.getContent()); // 评论内容
                childComment.put("userId", cItem.getUserId());  // 评论用户id
                childComment.put("nickName", userService.getById(cItem.getUserId()).getNickname()); // 评论用户昵称
                childComment.put("avatar", userService.getById(cItem.getUserId()).getPic());
                childComment.put("level", cItem.getLevel());  // 评论层级
                childComment.put("createTime", cItem.getCreateTime());// 评论时间
                childComment.put("rootId", cItem.getRootId()); // 根评论id，没有为null
                if (cItem.getParentId() != null) {
                    childComment.put("parentId", cItem.getParentId()); // 回复目标用户的id
                    childComment.put("replyUserName", userService.getById(cItem.getParentId()).getNickname()); // 回复目标用户的昵称
                }
                // 子评论数据
                childCommentArr.add(childComment);
            });
            //  将子评论数据添加到根评论
            comment.put("child", childCommentArr);
            // 将根评论添加到数组集合
            commentArr.add(comment);
        });
        // 2. 将评论的数组集合添加到总评论集合的一个属性中
        commentData.put("comments", commentArr);
        return commentData;
    }

    /**
     * 5.获取宠物的点赞状态
     *  type{pet_id/topic} id{宠物id/话题id} token
     */
    public HashMap<String, Object> getPetLike(String type,int id,String token){
        // 获取点赞相关
        HashMap<String, Object> likeObj = new HashMap<>();
        // 获取点赞数量
        QueryWrapper<Thumb> pId = new QueryWrapper<Thumb>().eq(type,id);
        int Count = thumbService.list(pId).size();
        likeObj.put("count", Count);
        // 用户是否点赞
        if (TokenUtils.verify(token)) {
            QueryWrapper<Thumb> eqT = new QueryWrapper<Thumb>().eq(type, id).eq("user_id", TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
            Thumb isThumb = thumbService.getOne(eqT, false);
            if (isThumb == null) {
                likeObj.put("isLike", false);
            } else {
                likeObj.put("isLike", true);
            }
        } else {
            likeObj.put("isLike", false);
        }
        return likeObj;
    }
}
