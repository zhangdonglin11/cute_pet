package com.example.cute_pet.controller.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.Comment;
import com.example.cute_pet.domain.Pet;
import com.example.cute_pet.domain.Picture;
import com.example.cute_pet.domain.Thumb;
import com.example.cute_pet.service.*;
import com.example.cute_pet.util.ImageUtil;
import com.example.cute_pet.util.Result;
import com.example.cute_pet.util.TokenUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 宠物信息相关接口
 */
@RestController
@RequestMapping("api/user/pet")
public class PetControl {
    @Autowired
    private Common common;
    @Autowired
    private PetService petService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private UserService userService;
    /**
     * 获取宠物的草稿
     */
    @GetMapping("/draft")
    public Result getPetDraft(@RequestHeader("token") String token) {
        Integer uid = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<Pet> eqPet = new QueryWrapper<Pet>().eq("user_id", uid).eq("status", 0);
        Pet pet = petService.getOne(eqPet);
        if (pet != null) {
            ArrayList<String> picList = common.getPicList("pet_id", pet.getId());
            HashMap<String, Object> data = new HashMap<>();
            data.put("pet", pet);
            data.put("picList", picList);
            return new Result<>(1, "请求成功", data);
        } else {
            Pet newPet = new Pet();
            newPet.setUserId(uid);
            newPet.setStatus(0);
            boolean saved = petService.save(newPet);
            HashMap<String, Object> data = new HashMap<>();
            if (saved) {
                data.put("pet", newPet);
                return new Result<>(1, "请求成功", data);
            }
            return new Result<>(0, "请求失败", data);

        }
    }

    /**
     * 保存宠物的草稿
     */
    @PostMapping("/draft")
    public Result savePetDraft(@RequestBody Pet pet,
                               @RequestParam("imageChange") boolean imageChange,
                               @RequestHeader("token") String token) {
        int uid = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // if (userService.getById(uid).getStatus() ==2) return new Result<>(0,"用户已被禁用");
        if (Objects.equals(pet.getUserId(), uid)) {
            // 保存宠物信息并设置状态为0 草稿
            pet.setStatus(0);
            System.out.println(pet);
            petService.updateById(pet);
            // 如果图片修改了就删除所有的旧图片
            if (imageChange) {
                // 传宠物id删除所有宠物图片
                common.deletePicList("pet_id", pet.getId());
            }
            return new Result<>(1, "保存草稿成功");
        } else {
            return new Result<>(0, "保存草稿失败");
        }
    }

    /**
     * 保存宠物
     */
    @PostMapping()
    public Result savePet(@RequestBody Pet pet,
                          @RequestParam("imageChange") boolean imageChange,
                          @RequestHeader("token") String token) {
        Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // 判断图片的用户id和token的用户id是否相等
        if (Objects.equals(pet.getUserId(), userId)) {
            // 保存宠物信息并设置状态为3需要管理员审核
            pet.setStatus(3);
            petService.updateById(pet);
            // 如果图片修改了就删除所有的旧图片
            if (imageChange) {
                common.deletePicList("pet_id", pet.getId());
            }
            return new Result<>(1, "宠物信息保存成功");
        } else {
            return new Result<>(0, "宠物信息保存失败");
        }
    }
    /**
     * 保存宠物图片数组
     */
    @PostMapping("/pic")
    public Result savePetPic(@RequestBody MultipartFile files,
                             @RequestParam("pid") int pid,
                             @RequestHeader("token") String token) {
        System.out.println("aaa");
        // 查询该宠物的用户id
        Integer pet_userID = petService.getById(pid).getUserId();
        Integer user_id = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        if (Objects.equals(pet_userID, user_id)) {
            if (!files.isEmpty()) {
                // 保存文件操作
                // 获取文件后缀
                String suffixName = ImageUtil.getImagePath(files);
                // 生成新文件名称
                String newFileName = ImageUtil.getNewFileName(suffixName);
                // 生成保存文件的地址
                File file = new File(ImageUtil.getNewImagePath(newFileName, "image/"));
                // 保存文件  保存成功
                boolean state = ImageUtil.saveImage(files, file);
                // 保存位置
                if (state) {
                    // 修改图片地址
                    Picture petPic = new Picture();
                    petPic.setPetId(pid);
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
    /**
     * 查询单个宠物信息
     */
    @GetMapping("")
    public Result findPetById(@RequestParam("pid") int pid,
                              @RequestHeader("token") String token) {
        // 查询宠物的信息
        Pet pet = petService.getById(pid);
        if (pet != null) {
            // 获取宠物图片数组
            ArrayList<String> picList = common.getPicList("pet_id", pid);
            // 获取点赞数据
            HashMap<String, Object> petLike = common.getPetLike("pet_id", pid, token);
            // 获取评论区数据
            HashMap<String, Object> petComment = common.getPetComment("pet_id", pid);

            HashMap<String, Object> petObj = new HashMap<>();

            petObj.put("pet", pet);
            petObj.put("pic", picList);
            petObj.put("petLike", petLike);
            petObj.put("petComment", petComment);
            return new Result<>(1, "请求成功", petObj);
        } else {
            return new Result<>(0, "请求失败");
        }
    }
    /**
     * 删除宠物信息相
     */
    @GetMapping("/delete")
    public Result deletePet(@RequestParam("pid") int pid,
                            @RequestHeader("token") String token) {
        Pet pet = petService.getById(pid);
        Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        if (Objects.equals(pet.getUserId(), userId)) {
            // 删除宠物的信息
            petService.removeById(pid);
            // 删除宠物所有的图片 这是自定义删除宠物图片复用的方法
            common.deletePicList("pet_id", pet.getId());
            // 删除宠物的评论
            QueryWrapper<Comment> eqCommentPetId = new QueryWrapper<Comment>().eq("pet_id", pid);
            commentService.remove(eqCommentPetId);
            // 删除宠物的收藏
            QueryWrapper<Thumb> eqThumbPetId = new QueryWrapper<Thumb>().eq("pet_id", pid);
            thumbService.remove(eqThumbPetId);
            return new Result<>(1, "操作成功");
        } else {
            return new Result<>(1, "操作失败");
        }
    }

    /**
     * 点赞宠物
     */
    @GetMapping("/like")
    public Result setLike(@RequestParam("pid") int pid,
                          @RequestHeader("token") String token) {
        Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<Thumb> eqThumb = new QueryWrapper<Thumb>().eq("user_id", userId).eq("pet_id", pid);
        Thumb owned = thumbService.getOne(eqThumb);
        if (owned != null) {
            boolean removed = thumbService.remove(eqThumb);
        } else {
            Thumb thumb = new Thumb();
            thumb.setPetId(pid);
            thumb.setUserId(userId);
            thumbService.save(thumb);
        }
        return new Result<>(1, "操作成功");
    }
    /**
     * 发表评论 {}
     */
    @PostMapping("/comment")
    public Result petComment(@RequestBody Comment petComment,
                             @RequestHeader("token") String token) {
        System.out.println(petComment);
        petComment.setUserId(TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
        boolean saved = commentService.save(petComment);
        if (saved) {
            return new Result<>(1, "评论成功");
        } else {
            return new Result<>(0, "评论失败");
        }
    }

    /**
     * 举报宠物 {}
     */
    @GetMapping("/reportPet")
    public Result reportPet(@RequestParam("pid") int pid){

        Pet pet = petService.getById(pid);
        if (pet.getStatus()>0){
            pet.setStatus(pet.getStatus()+1);
            petService.updateById(pet);
        }
        return new Result<>(1,"操作成功");
    }
    // 举报评论
    @GetMapping("/reportComment")
    public Result reportComment(@RequestParam("cid") int cid){
        Comment comment = commentService.getById(cid);
        comment.setStatus(2);
        commentService.updateById(comment);
        return new Result<>(1,"操作成功");
    }

}
