package com.example.cute_pet.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.*;
import com.example.cute_pet.service.*;
import com.example.cute_pet.util.ImageUtil;
import com.example.cute_pet.util.Result;
import com.example.cute_pet.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * 小程序话题页面相关接口
 */
@RestController
@RequestMapping("api/user/topic")
public class TopicControl {
    @Autowired
    private Common common;
    @Autowired
    private TopicService topicService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private UserService userService;


    // 查询单个话题
    @GetMapping("")
    public Result getTopic(@RequestParam("tid") int tid, @RequestHeader("token") String token) throws IllegalAccessException {
        Topic topic = topicService.getById(tid);
        if (topic != null) {
            // 获取宠物图片数组
            ArrayList<String> picList = common.getPicList("topic_id", tid);
            // 获取点赞数据
            HashMap<String, Object> topicLike = common.getPetLike("topic_id", tid, token);
            // 获取评论区数据
            HashMap<String, Object> topicComment = common.getPetComment("topic_id", tid);

            HashMap<String, Object> topicObj = new HashMap<>();
            topicObj.put("pic", picList);
            topicObj.put("topicLike", topicLike);
            topicObj.put("topicComment", topicComment);

            // 获取用户的信息 头像和昵称
            User user = userService.getById(topic.getUserId());

            HashMap<String, Object> newTopic = new HashMap<>();
            // 循环把对象的属性和值赋值新的map集合
            Field[] fields = topic.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                newTopic.put(field.getName(), field.get(topic));
            }
            newTopic.put("userName", user.getNickname());
            newTopic.put("userSex", user.getSex());
            newTopic.put("userPic", user.getPic());
            topicObj.put("topic", newTopic);


            return new Result<>(1, "请求成功", topicObj);
        } else {
            return new Result<>(0, "请求失败");
        }
    }

    // 查看次数+1
    @GetMapping("/viewed")
    public void viewed(@RequestParam("tid") int tid) {
        Topic topic = topicService.getById(tid);
        if (topic != null) {
            topic.setViewed(topic.getViewed() + 1);
            topicService.updateById(topic);
        }
    }


    // 保存话题
    @PostMapping("/")
    public Result saveTopic(@RequestBody Topic topic,
                            @RequestParam("imageChange") boolean imageChange,
                            @RequestHeader("token") String token) {
        Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // 判断图片的用户id和token的用户id是否相等
        if (Objects.equals(topic.getUserId(), userId)) {
            // 保存宠物信息并设置状态为3需要管理员审核
            if (topic.getClassify()==null){
                topic.setClassify("日常");
            }
            topic.setStatus(3);
            topicService.updateById(topic);
            // 如果图片修改了就删除所有的旧图片
            if (imageChange) {
                common.deletePicList("topic_id", topic.getId());
            }
            return new Result<>(1, "话题发布成功");
        } else {
            return new Result<>(0, "话题发布失败");
        }
    }

    /**
     * 保存宠物图片数组
     */
    @PostMapping("/pic")
    public Result savePetPic(@RequestBody MultipartFile files,
                             @RequestParam("tid") int tid,
                             @RequestHeader("token") String token) {
        // 查询该宠物的用户id
        Integer topic_userID = topicService.getById(tid).getUserId();
        Integer user_id = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();

        System.out.println(tid);

        if (Objects.equals(topic_userID, user_id)) {
            if (!files.isEmpty()) {
                // 保存文件操作
                // 获取文件后缀
                String suffixName = ImageUtil.getImagePath(files);
                // 生成新文件名称
                String newFileName = ImageUtil.getNewFileName(suffixName);
                // 保存文件 生成保存文件的地址
                File file = new File(ImageUtil.getNewImagePath(newFileName, "image/"));
                // 保存成功
                boolean state = ImageUtil.saveImage(files, file);
                // 保存位置
                if (state) {
                    // 修改图片地址
                    Picture petPic = new Picture();
                    petPic.setTopicId(tid);
                    petPic.setUserId(user_id);
                    petPic.setPic("http://localhost:8088/image/" + newFileName);
                    boolean saved = pictureService.save(petPic);
                    if (!saved) {
                        // 保存图片地址到数据库 失败就删除图片文件
                        if (file.exists()) {
                            FileSystemUtils.deleteRecursively(file);
                        } else {
                            System.out.println("目标文件不存在");
                        }
                    }
                    System.out.println("添加了图片" + petPic);
                    return new Result<>(1, "图片添加成功");
                }
            }
        }
        return new Result<>(0, "添加图片失败");
    }


    // 获取初始化的话题编辑
    @GetMapping("/draft")
    public Result topicDraft(@RequestHeader("token") String token) {
        Integer uid = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<Topic> eqTopic = new QueryWrapper<Topic>().eq("user_id", uid).eq("status", 0);
        Topic topic = topicService.getOne(eqTopic);
        if (topic != null) {
            ArrayList<String> picList = common.getPicList("topic_id", topic.getId());
            HashMap<String, Object> data = new HashMap<>();
            data.put("topic", topic);
            data.put("picList", picList);
            return new Result<>(1, "请求成功", data);
        } else {
            Topic newTopic = new Topic();
            newTopic.setUserId(uid);
            newTopic.setAllow(1);
            newTopic.setViewed(0);
            newTopic.setStatus(0);
            boolean saved = topicService.save(newTopic);
            HashMap<String, Object> data = new HashMap<>();
            if (saved) {
                data.put("topic", newTopic);
                return new Result<>(1, "请求成功", data);
            }
            return new Result<>(0, "请求失败", data);
        }
    }


    // 获取topic页的数据
    @PostMapping("/list")
    public Result getFiltrateTopicList(@RequestBody HashMap<String, Object> filtrate, @RequestHeader("token") String token) throws IllegalAccessException {
        QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();

        if (Objects.equals(filtrate.get("whoTopic"), 1)) {
            topicQueryWrapper.eq("user_id", TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
            // 个人的都可以看
            topicQueryWrapper.between("status", 1, 3);
        } else {
            // 审核后的才可以被查询
            topicQueryWrapper.between("status", 1, 2);
        }
        if (filtrate.containsKey("classify") && filtrate.get("classify") != null) {
            topicQueryWrapper.eq("classify", filtrate.get("classify"));
        }
        topicQueryWrapper.orderByDesc("create_time");

        int current = Integer.parseInt(filtrate.get("current").toString());
        int size = Integer.parseInt(filtrate.get("size").toString());
        Page<Topic> page = new Page<>(current, size);
        Page<Topic> pageList = topicService.page(page, topicQueryWrapper);
        System.out.println(pageList.getRecords());
        ArrayList<Object> topicArr = new ArrayList<>();
        for (Topic topic : pageList.getRecords()) {
            // 1.获取话题图片数组
            ArrayList<String> picArr = common.getPicList("topic_id", topic.getId());
            // 2.宠物的获取评论
            HashMap<String, Object> topicComment = common.getPetComment("topic_id", topic.getId());
            // 3.获取宠物的点赞状态
            HashMap<String, Object> topicLike = common.getPetLike("topic_id", topic.getId(), token);
            HashMap<String, Object> topicMap = new HashMap<>();
            // 获取用户的信息 头像和昵称
            User user = userService.getById(topic.getUserId());

            HashMap<String, Object> newTopic = new HashMap<>();
            // 循环把对象的属性和值赋值新的map集合
            Field[] fields = topic.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                newTopic.put(field.getName(), field.get(topic));
            }
            newTopic.put("userName", user.getNickname());
            newTopic.put("userSex", user.getSex());
            newTopic.put("userPic", user.getPic());

            topicMap.put("topic", newTopic);
            topicMap.put("pic", picArr);
            topicMap.put("topicComment", topicComment);
            topicMap.put("topicLike", topicLike);
            topicArr.add(topicMap);
        }
        HashMap<String, Object> topicObj = new HashMap<>();
        topicObj.put("topicArr", topicArr);  // 宠物数组
        topicObj.put("total", pageList.getTotal());
        topicObj.put("size", pageList.getSize());
        topicObj.put("current", pageList.getCurrent());
        topicObj.put("pages", pageList.getPages());

        return new Result<>(1, "请求成功", topicObj);
    }


    /**
     * 点赞
     */
    @GetMapping("/like")
    public Result setLike(@RequestParam("tid") int tid, @RequestHeader("token") String token) {
        Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<Thumb> eqThumb = new QueryWrapper<Thumb>().eq("user_id", userId).eq("topic_id", tid);
        Thumb owned = thumbService.getOne(eqThumb);
        if (owned != null) {
            boolean removed = thumbService.remove(eqThumb);
        } else {
            Thumb thumb = new Thumb();
            thumb.setTopicId(tid);
            thumb.setUserId(userId);
            thumbService.save(thumb);
        }
        return new Result<>(1, "操作成功");
    }

    /**
     * 发表评论 {}
     */
    @PostMapping("/comment")
    public Result topicComment(@RequestBody Comment comment, @RequestHeader("token") String token) {
        System.out.println(comment);
        Topic topic = topicService.getById(comment.getTopicId());
        if (topic.getAllow() == 1) {
            comment.setUserId(TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
            boolean saved = commentService.save(comment);
            return new Result<>(1, "评论成功");
        } else {
            return new Result<>(0, "话题设定不许评论");
        }
    }


    /**
     * 删除话题 {}
     */
    @GetMapping("/delete")
    public Result deleteTopic(@RequestParam("tid") int tid, @RequestHeader("token") String token) {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<Topic>()
                .eq("id", tid)
                .eq("user_id", userId)
                .last("LIMIT 1");
        Topic topic = topicService.getOne(topicQueryWrapper);

        if (topic != null) {
            int status = topic.getStatus();
            if (status == 1 || status == 2) {
                //  真删话题的信息
                topicService.removeById(tid);
                // 删除所有的图片 这是自定义删除图片复用的方法
                common.deletePicList("topic_id", tid);
                // 删除评论
                QueryWrapper<Comment> eqCommentPetId = new QueryWrapper<Comment>().eq("topic_id", tid);
                commentService.remove(eqCommentPetId);
                // 删除宠物的收藏
                QueryWrapper<Thumb> eqThumbPetId = new QueryWrapper<Thumb>().eq("topic_id", tid);
                thumbService.remove(eqThumbPetId);
                return new Result<>(1, "操作成功");
            } else {
                topic.setStatus(4);
                topicService.updateById(topic);
                return new Result<>(1, "操作成功");
            }
        } else {
            // 不是该用户权限删除
            return new Result<>(1, "操作失败");
        }
    }

    /**
     * 举报话题 {}
     */
    @GetMapping("/report")
    public Result getReport(@RequestParam("tid") int tid) {
        Topic topic = topicService.getById(tid);
        if (topic == null) new Result<>(0, "举报失败");
        int status = topic.getStatus();
        if (status == 1 || status == 2) topic.setStatus(topic.getStatus() + 1);
        topicService.updateById(topic);
        return new Result<>(1, "举报成功");
    }
}
