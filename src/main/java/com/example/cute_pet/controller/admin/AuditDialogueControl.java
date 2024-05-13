package com.example.cute_pet.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.*;
import com.example.cute_pet.service.ChatCommentService;
import com.example.cute_pet.service.ChatListService;
import com.example.cute_pet.service.ChatService;
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
@RequestMapping("api/admin/audit-dialogue")
public class AuditDialogueControl {
    @Autowired
    private UserService userService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatListService chatListService;
    @Autowired
    private ChatCommentService chatCommentService;
    @Autowired
    private Common common;

    /**
     * 获取话题列表
     */
    @PostMapping("")
    public Result getChatList(@RequestBody HashMap<String, Object> condition) throws IllegalAccessException {
        QueryWrapper<ChatList> chatListWrapper = new QueryWrapper<>();
        // 查询所有用户或者待审核用户 3/4 正常，审核、禁止
        if (condition.containsKey("audit") && Objects.equals(condition.get("audit"), 1)) {
            chatListWrapper.in("status", 3, 4);
        }
        // 搜素框条件
        if (condition.containsKey("searchIpt") && condition.get("searchIpt") != "") {
            // 用户id查询
            if (Objects.equals(condition.get("searchType"), 1)) {
                Chat chat = chatService.getOne(new QueryWrapper<Chat>()
                        .eq("user_id", condition.get("searchIpt"))
                        .or()
                        .eq("another_id", condition.get("searchIpt")));
                if (chat==null)return new Result<>(0,"无符合条件");
                chatListWrapper.eq("chat_id", chat.getId());
            }
            // 用户电话
            if (Objects.equals(condition.get("searchType"), 2)) {
                User user = userService.getOne(new QueryWrapper<User>().eq("phone", condition.get("searchIpt")));
                if (user == null) return new Result<>(1, "没有该用户");
                Chat chat = chatService.getOne(new QueryWrapper<Chat>()
                        .eq("user_id", condition.get("searchIpt"))
                        .or()
                        .eq("another_id", condition.get("searchIpt")));
                if (chat==null)return new Result<>(0,"无符合条件");
                chatListWrapper.eq("chat_id", chat.getId());
            }
            // 用户昵称
            if (Objects.equals(condition.get("searchType"), 3)) {
                User user = userService.getOne(new QueryWrapper<User>().like("nickname", condition.get("searchIpt")));
                if (user == null) return new Result<>(1, "没有该用户");
                List<Chat> chat = chatService.list(new QueryWrapper<Chat>()
                        .eq("user_id", condition.get("searchIpt"))
                        .or()
                        .eq("another_id", condition.get("searchIpt")));
                ArrayList<Integer> chatIdArr = new ArrayList<>();
                chat.forEach(item -> {
                    chatIdArr.add(item.getId());
                });
                chatListWrapper.in("chat_id", chatIdArr);
            }
        }
        // 页码
        int current = Integer.parseInt(condition.get("current").toString());
        int size = Integer.parseInt(condition.get("size").toString());

        Page<ChatList> page = new Page<>(current, size);
        Page<ChatList> PageChatList = chatListService.page(page, chatListWrapper);

        ArrayList<Object> chatArr = new ArrayList<>();
        for (ChatList chatList : PageChatList.getRecords()) {
            HashMap<String, Object> chatListMap = new HashMap<>();
            // 将对象的属性值赋值给Map
            common.handleObject(chatList, chatListMap);

            User user = userService.getById(chatList.getUserId());
            chatListMap.put("userName", user.getNickname());
            chatListMap.put("userPic", user.getPic());

            User another = userService.getById(chatList.getAnotherId());
            chatListMap.put("anotherName", another.getNickname());
            chatListMap.put("anotherPic", another.getPic());

            // 获取对话记录
            List<ChatComment> chatCommentArr = chatCommentService.list(new QueryWrapper<ChatComment>().eq("chat_id", chatList.getChatId()));
            chatListMap.put("chatCommentArr",chatCommentArr);

            chatArr.add(chatListMap);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("chatArr", chatArr);
        data.put("total", PageChatList.getTotal());
        data.put("size", PageChatList.getSize());
        data.put("current", PageChatList.getCurrent());
        data.put("pages", PageChatList.getPages());
        return new Result<>(data);
    }

    /**
     * 审核对话列表
     */
    @PostMapping("/dialogue")
    public Result auditPet(@RequestBody HashMap<String,Object> fromData){
        ChatList chatList = chatListService.getById((int) fromData.get("cid"));
        // 通过对话审核
        if (chatList!=null&&Objects.equals(fromData.get("type"),1)){
            if (chatList.getStatus()==3) chatList.setStatus(0);
            if (chatList.getStatus()==4) chatList.setStatus(2);
            chatListService.updateById(chatList);
            return new Result<>(1,"操作成功");
        }
        // 删除对话记录
        if (chatList!=null&&Objects.equals(fromData.get("type"),2)){
            // 删除所有聊天记录的图片
            QueryWrapper<ChatComment> findType = new QueryWrapper<ChatComment>()
                    .eq("chat_id", chatList.getChatId()).eq("type", 1);
            List<ChatComment> chatCommentList = chatCommentService.list(findType);
            for (ChatComment chatComment : chatCommentList) {
                // 获取数据库图片网络地址
                String picHttpAddress = chatComment.getContent();
                // 传图片链接删除图片文件
                common.deleteFileImg(picHttpAddress);
            }
            // 删除所有聊天记录
            QueryWrapper<ChatComment> chatIdWrapper = new QueryWrapper<ChatComment>().eq("chat_id",  chatList.getChatId());
            chatCommentService.remove(chatIdWrapper);
            // 删除对话列表
            chatListService.remove(new QueryWrapper<ChatList>().eq("chat_id", chatList.getChatId()));
            // 删除对话主表
            chatService.removeById(chatList.getChatId());
            return new Result<>(1, "操作成功");
        }
        return new Result<>(0,"操作失败");
    }
}
