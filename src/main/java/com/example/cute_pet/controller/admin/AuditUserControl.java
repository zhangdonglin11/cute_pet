package com.example.cute_pet.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.*;
import com.example.cute_pet.service.*;
import com.example.cute_pet.util.ImageUtil;
import com.example.cute_pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/admin/audit-user")
public class AuditUserControl {
    @Autowired
    private UserService userService;
    @Autowired
    private PetService petService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatListService chatListService;
    @Autowired
    private ChatCommentService chatCommentService;
    @Autowired
    private Common common;

    @PostMapping("")
    public Result getUserList(@RequestBody HashMap<String,Object> condition){
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        // 查询所有用户或者待审核用户 0/1/2 正常，审核、禁止
        if (condition.containsKey("audit") && Objects.equals(condition.get("audit"),1)){
            userWrapper.eq("status",1);
        }
        // 搜素框条件
        if (condition.containsKey("searchIpt") && condition.get("searchIpt")!=""){
            if (Objects.equals(condition.get("searchType"),1)){
                userWrapper.eq("id",condition.get("searchIpt"));
            }
            if (Objects.equals(condition.get("searchType"),2)){
                userWrapper.like("nickname",condition.get("searchIpt").toString());
            }
            if (Objects.equals(condition.get("searchType"),3)){
                userWrapper.like("phone",condition.get("searchIpt").toString());
            }
        }
        userWrapper.orderByDesc("create_time");

        // 页码
        int current = Integer.parseInt(condition.get("current").toString());
        int size = Integer.parseInt(condition.get("size").toString());

        Page<User> page = new Page<>(current, size);
        Page<User> userList = userService.page(page, userWrapper);

        HashMap<String, Object> data = new HashMap<>();
        data.put("userList",userList.getRecords());
        data.put("total",userList.getTotal());
        data.put("size",userList.getSize());
        data.put("current",userList.getCurrent());
        data.put("pages",userList.getPages());
        return new Result<>(data);
    }
    @GetMapping("/remove-img")
    public Result removeImg(@RequestParam("id") int id) throws IOException {
        User user = userService.getById(id);
        // 从 static 目录下获取图片资源
        Resource resource = new ClassPathResource("static/image/profile-img.png");
        // 获取输入流
        InputStream inputStream = resource.getInputStream();
        // 获取新的文件名
        String newFileName = ImageUtil.getNewFileName(".png");
        // 指定保存文件的位置
        String targetLocation = ImageUtil.getNewImagePath(newFileName, "image/");
        Path targetPath = Paths.get(targetLocation);
        // 将文件内容写入指定位置，并使用新文件名
        try {
            Files.copy(inputStream, targetPath);
            System.out.println("文件保存成功！");
        } catch (IOException e) {
            System.err.println("文件保存失败：" + e.getMessage());
        }
        // 关闭输入流
        inputStream.close();
        // 删除用户旧的头像图片
        common.deleteFileImg(user.getPic());
        // 修改用户图片网络地址
        user.setPic("http://localhost:8088/image/" + newFileName);
        userService.updateById(user);
        return new Result<>(user);
    }
    @PostMapping("/upload")
    public Result updateUser(@RequestBody User user){
        user.setCreateTime(null);
        user.setRole("USER");
        userService.updateById(user);
        return new Result<>(1,"修改成功");
    }

    // 删除用户所有信息
    @GetMapping ("/del")
    public Result deleteUser(@RequestParam("id") int id){
        System.out.println(id);
        // 删除宠物
        petService.remove( new QueryWrapper<Pet>().eq("user_id",id));
        // 删除话题
        topicService.remove(new QueryWrapper<Topic>().eq("user_id",id));
        // 删除评论
        commentService.remove(new QueryWrapper<Comment>().eq("user_id", id));
        // 删除图片
        pictureService.remove(new QueryWrapper<Picture>().eq("user_id",id));
        // 删除点赞收藏
        thumbService.remove(new QueryWrapper<Thumb>().eq("user_id",id));
        // 删除对话
        ChatList chatList = chatListService.getOne(new QueryWrapper<ChatList>().eq("user_id", id));
        if (chatList!=null){
            List<ChatComment> chatCommentList = chatCommentService.list(new QueryWrapper<ChatComment>().eq("chat_id", chatList.getChatId()));
            chatCommentList.forEach(item->{
                if (item.getType()==1){
                    common.deleteFileImg(item.getContent());
                }
            });
            chatCommentService.remove(new QueryWrapper<ChatComment>().eq("chat_id", chatList.getChatId()));
            chatService.removeById(chatList.getChatId());
            chatListService.remove(new QueryWrapper<ChatList>().eq("chat_id", chatList.getChatId()));
        }
        // 删除用户头像图片
        common.deleteFileImg(userService.getById(id).getPic());
        // 删除用户
        userService.removeById(id);
        return new Result<>(1,"删除成功");
    }

    @PostMapping("/add")
    public Result addUser(@RequestBody User user) throws IOException {
        User usered = userService.getOne(new QueryWrapper<User>().eq("phone", user.getPhone()));
        if (usered!=null) return new Result<>(0,"用户已存在");
        // 从 static 目录下获取图片资源
        Resource resource = new ClassPathResource("static/image/profile-img.png");
        // 获取输入流
        InputStream inputStream = resource.getInputStream();
        // 获取新的文件名
        String newFileName = ImageUtil.getNewFileName(".png");
        // 指定保存文件的位置
        String targetLocation = ImageUtil.getNewImagePath(newFileName, "image/");
        Path targetPath = Paths.get(targetLocation);
        // 将文件内容写入指定位置，并使用新文件名
        try {
            Files.copy(inputStream, targetPath);
            System.out.println("文件保存成功！");
        } catch (IOException e) {
            System.err.println("文件保存失败：" + e.getMessage());
        }
        // 关闭输入流
        inputStream.close();
        // 修改用户图片网络地址
        user.setPic("http://localhost:8088/image/" + newFileName);
        user.setRole("USER");
        user.setStatus(0);
        userService.save(user);
        return new Result<>(1,"添加用户成功");
    }



}
