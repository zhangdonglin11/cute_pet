package com.example.cute_pet.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import java.util.*;

/**
 * 小程序通知页面相关接口
 */
@RestController
@RequestMapping("/api/user/inform")
public class InformControl {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatListService chatListService;
    @Autowired
    private ChatCommentService chatCommentService;
    @Autowired
    private UserService userService;
    @Autowired
    private PetService petService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private Common common;

    // 将对象遍历成map
    private static void handleObject(Object handleObj, HashMap<String, Object> resultMap) throws IllegalAccessException {
        // 循环把对象的属性和值赋值新的map集合
        Field[] fields = handleObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            resultMap.put(field.getName(), field.get(handleObj));
        }
    }

    /**
     * 消息页获取被点赞数量和被评论数量
     */
    @GetMapping("")
    public Result getNewsNumber(@RequestHeader("token") String token) throws IllegalAccessException {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();

        // 1.获取我的宠物，我发的话题被点赞数量
        QueryWrapper<Pet> eqPet = new QueryWrapper<Pet>().eq("user_id", userId).ne("status", 0);
        List<Pet> petList = petService.list(eqPet);
        ArrayList<Integer> petIdArr = new ArrayList<>();
        for (Pet pet : petList) {
            petIdArr.add(pet.getId());
        }

        QueryWrapper<Topic> eqTopic = new QueryWrapper<Topic>().eq("user_id", userId).eq("user_id", userId).ne("status", 0);
        List<Topic> topicList = topicService.list(eqTopic);
        ArrayList<Integer> topicIdArr = new ArrayList<>();
        for (Topic topic : topicList) {
            topicIdArr.add(topic.getId());
        }

        // 2.通过pet_id或者topic_id筛选出被点赞数据
        if (petIdArr.isEmpty())petIdArr.add(0);
        if (topicIdArr.isEmpty())topicIdArr.add(0);
        QueryWrapper<Thumb> thumbQueryWrapper = new QueryWrapper<Thumb>()
                .in("pet_id", petIdArr)
                .or()
                .in("topic_id", topicIdArr);
        List<Thumb> thumbList = thumbService.list(thumbQueryWrapper);



        // 3.获取被评论数量
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<Comment>()
                .in("pet_id", petIdArr).isNull("parent_id")
                .or()
                .in("topic_id", topicIdArr).isNull("parent_id")
                .or()
                .eq("parent_id", userId)
                .orderByDesc("create_time");
        List<Comment> commentList = commentService.list(commentQueryWrapper);

        // 处理喜欢的消息
        ArrayList<Object> likeArr = new ArrayList<>();
        for (Thumb thumb:thumbList){

            HashMap<String, Object> likeMap = new HashMap<>();

            User user = userService.getById(thumb.getUserId());
            // 添加对方id
            likeMap.put("userId",user.getId());
            // 对方用户图片
            likeMap.put("userPic",user.getPic());
            // 对方名字
            likeMap.put("userName",user.getNickname());
            // 时间
            likeMap.put("createTime",thumb.getCreateTime());
            // 来源图片
            QueryWrapper<Picture> eqPic= new QueryWrapper<Picture>();
            if (thumb.getPetId()!=null){
                eqPic.eq("pet_id", thumb.getPetId()).last("LIMIT 1");
                likeMap.put("petId",thumb.getPetId());
                likeMap.put("topicId",null);
            }else {
                eqPic.eq("topic_id", thumb.getTopicId()) .last("LIMIT 1");
                likeMap.put("topicId",thumb.getTopicId());
                likeMap.put("petId",null);
            }
            Picture onePic = pictureService.getOne(eqPic);
            if (onePic!=null)likeMap.put("targetPic",onePic.getPic());

            likeArr.add(likeMap);
        };

        // 处理评论的消息
        ArrayList<Object> commentArr = new ArrayList<>();
        for (Comment comment :commentList){
            HashMap<String, Object> commentMap = new HashMap<>();
            // 评论id
            commentMap.put("commentId",comment.getId());
            User user = userService.getById(comment.getUserId());
            // 添加对方id
            commentMap.put("userId",user.getId());
            // 对方用户图片
            commentMap.put("userPic",user.getPic());
            // 对方名字
            commentMap.put("userName",user.getNickname());
            // 时间
            commentMap.put("createTime",comment.getCreateTime());

            // 来源图片
            QueryWrapper<Picture> eqPic= new QueryWrapper<Picture>();
            if (comment.getPetId()!=null){
                eqPic.eq("pet_id", comment.getPetId()).last("LIMIT 1");
                commentMap.put("petId",comment.getPetId());
                commentMap.put("topicId",null);
            }else {
                eqPic.eq("topic_id", comment.getTopicId()) .last("LIMIT 1");
                commentMap.put("topicId",comment.getTopicId());
                commentMap.put("petId",null);
            }
            Picture onePic = pictureService.getOne(eqPic);
            if (onePic!=null)commentMap.put("targetPic",onePic.getPic());
            // 目标内容
            commentMap.put("content",comment.getContent());

            commentArr.add(commentMap);
        }


        HashMap<String, Object> data = new HashMap<>();
        data.put("likeCunt",thumbList.size());
        data.put("commentCount",commentList.size());
        data.put("likeArr",likeArr);
        data.put("commentArr",commentArr);

        return new Result<>(1,"请求成功",data);
    }


    /**
     * 发起和目标聊天对话的请求
     */
    @GetMapping("/chat")
    public Result getChar(@RequestParam("anotherId") int anotherId,
                          @RequestHeader("token") String token) throws IllegalAccessException {

        // 1、点击聊天
        // 判断是不是第一次聊天，如果是会在主表生成一条记录返回聊天主表id，并在聊天列表表分别插入两条记录，如果不是第一次聊天进入下一步
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        if (userId == anotherId) return new Result<>(0, "不能和自己对话");
        // 判断是否有聊天主表
        QueryWrapper<Chat> ChatBetween = new QueryWrapper<Chat>()
                .eq("user_id", userId)
                .eq("another_id", anotherId)
                .or()
                .eq("user_id", anotherId)
                .eq("another_id", userId)
                .last("LIMIT 1");
        Chat oneChat = chatService.getOne(ChatBetween);
        if (oneChat == null) {
            //  没有聊天记录
            // 1.聊天主表
            Chat newChat = new Chat();
            newChat.setUserId(userId);
            newChat.setAnotherId(anotherId);
            boolean saved = chatService.save(newChat);
            // 2.聊天列表
            if (saved) {
                ChatList chatList1 = new ChatList();
                chatList1.setChatId(newChat.getId());
                chatList1.setUserId(userId);
                chatList1.setAnotherId(anotherId);
                chatList1.setUnread(0);
                chatList1.setStatus(0);
                chatList1.setIsOnline(1);

                chatListService.save(chatList1);
                ChatList chatList2 = new ChatList();
                chatList2.setChatId(newChat.getId());
                chatList2.setUserId(anotherId);
                chatList2.setAnotherId(userId);
                chatList2.setUnread(0);
                chatList2.setStatus(0);
                chatList2.setIsOnline(1);
                chatListService.save(chatList2);

                // 返回我的对话列表信息，自动添加一条打招呼的信息
                HashMap<String, Object> newChatList = hadleChatLit(chatList1);

                return new Result<>(newChatList);
            }
            return new Result<>(0, "发起聊天失败");
        } else {
            // 查询对方话题列表状态
            QueryWrapper<ChatList> ChatListWrapper = new QueryWrapper<ChatList>()
                    .eq("chat_id", oneChat.getId())
                    .eq("user_id", anotherId)
                    .ne("status", 2)  //拉黑
                    .ne("status", 4)  //拉黑举报
                    .last("LIMIT 1");
            ChatList anotherChatList = chatListService.getOne(ChatListWrapper);
            // 调用底部复用方法处理聊天列表的数据
            if (anotherChatList != null) {
                // 把双方对话列表设置显示状态
                anotherChatList.setStatus(0);
                anotherChatList.setIsOnline(1);
                chatListService.updateById(anotherChatList);

                // 查询用户聊天列表
                QueryWrapper<ChatList> myChatListWrapper = new QueryWrapper<ChatList>()
                        .eq("chat_id", oneChat.getId())
                        .eq("user_id", userId);
                ChatList myChatList = chatListService.getOne(myChatListWrapper);

                // 把双方对话列表设置显示状态
                myChatList.setStatus(0);
                myChatList.setIsOnline(1);
                chatListService.updateById(myChatList);

                HashMap<String, Object> newChatList = hadleChatLit(myChatList);
                return new Result<>(newChatList);
            } else {
                return new Result<>(0, "你已被对方拉如黑名单");
            }

        }
    }

    /**
     * 获取对话列表
     */
    @GetMapping("chat/list")
    public Result getChatList(@RequestHeader("token") String token) throws IllegalAccessException {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // 我方删除不显示
        QueryWrapper<ChatList> chatListWrapper = new QueryWrapper<ChatList>()
                .eq("user_id", userId)
                .eq("is_online", 1)
                .notIn("status", 1);
        List<ChatList> chatLists = chatListService.list(chatListWrapper);

        ArrayList<Object> chatListArr = new ArrayList<>();
        for (ChatList chatList : chatLists) {
            // 调用底部复用方法处理聊天列表的数据
            HashMap<String, Object> newChatList = hadleChatLit(chatList);

            chatListArr.add(newChatList);
        }
        return new Result<>(chatListArr);
    }


    /**
     * 获取聊天记录
     */
    @GetMapping("/chat/history")
    public Result getChatHistory(@RequestParam("chatId") int chatId,
                                 @RequestHeader("token") String token) throws IllegalAccessException {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<Chat> chatWrapper = new QueryWrapper<Chat>()
                .eq("id", chatId)
                .eq("user_id", userId)
                .or()
                .eq("id", chatId)
                .eq("another_id", userId)
                .last("LIMIT 1");
        Chat chat = chatService.getOne(chatWrapper);
        if (chat != null) {
            // 按照时间最旧排序 ，如果使用分页模式需要按照最新排序，获取的每一页数据再按照反转排序
            QueryWrapper<ChatComment> chatCommentWrapper = new QueryWrapper<ChatComment>()
                    .eq("chat_id", chat.getId())
                    .orderByAsc("create_time");
            List<ChatComment> chatCommentList = chatCommentService.list(chatCommentWrapper);

            ArrayList<Object> chatCommentArr = new ArrayList<>();
            for (ChatComment chatComment : chatCommentList) {
                HashMap<String, Object> newChatComment = new HashMap<>();
                // 将对象属性和值遍历进map
                handleObject(chatComment, newChatComment);
                // 获取用户头像
                newChatComment.put("userPic", userService.getById(chatComment.getUserId()).getPic());

                chatCommentArr.add(newChatComment);
            }
            ChatList chatList = new ChatList();
            chatList.setUnread(0);
            QueryWrapper<ChatList> chatListQueryWrapper = new QueryWrapper<ChatList>()
                    .eq("chat_id", chatId).eq("user_id", userId);
            chatListService.update(chatList, chatListQueryWrapper);
            return new Result<>(chatCommentArr);
        } else {
            return new Result<>(0, "操作失败");
        }
    }

    /**
     * 发送信息
     */
    @PostMapping("/chat/sand")
    public Result sendChat(@RequestBody ChatComment chatComment,
                           @RequestHeader("token") String token) {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // 判断聊天列表的状态 状态为2，4 不可发信息
        // 操作的用户的表信息
        QueryWrapper<ChatList> chatListQueryWrapper1 = new QueryWrapper<ChatList>()
                .eq("chat_id", chatComment.getChatId())
                .eq("user_id", userId)
                .notIn("status", 2, 4);
        ChatList userChatList = chatListService.getOne(chatListQueryWrapper1);
        if (userChatList == null) return new Result<>(0, "在黑名单中");
        // 对方的
        QueryWrapper<ChatList> chatListQueryWrapper2 = new QueryWrapper<ChatList>()
                .eq("chat_id", chatComment.getChatId())
                .eq("user_id", userChatList.getAnotherId())
                .notIn("status", 2, 4);
        ChatList anotherChatList = chatListService.getOne(chatListQueryWrapper2);
        // 双方都有权限
        if (anotherChatList != null) {
            // 保存对话信息到数据库
            chatComment.setUserId(userId);
            chatComment.setIsLatest(0);
            boolean saved = chatCommentService.save(chatComment);
            if (!saved) return new Result<>(0, "发送失败");

            // 如果状态为1 双方状态修改为0
            if (userChatList.getStatus() == 1) {
                userChatList.setStatus(0);
                anotherChatList.setStatus(0);
            }
            userChatList.setIsOnline(1);
            anotherChatList.setIsOnline(1);

            // 将对方的信息列表未读+1
            anotherChatList.setUnread(anotherChatList.getUnread() + 1);
            // 修改对方的对话列表
            chatListService.updateById(anotherChatList);

            // 修改用户的对话列表
            chatListService.updateById(userChatList);

            return new Result<>(1, "发送成功");
        }
        return new Result<>(0, "你在对方黑名单");
    }

    /**
     * 发送图片信息
     */
    @PostMapping("/chat/sandpic")
    public Result sandPic(@RequestBody MultipartFile files,
                          @RequestParam("chatId") int chatId,
                          @RequestHeader("token") String token) {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // 判断聊天列表的状态 状态为2，4 不可发信息
        // 操作的用户的表信息
        QueryWrapper<ChatList> chatListQueryWrapper1 = new QueryWrapper<ChatList>()
                .eq("chat_id", chatId)
                .eq("user_id", userId)
                .notIn("status", 2, 4);
        ChatList userChatList = chatListService.getOne(chatListQueryWrapper1);
        if (userChatList == null) return new Result<>(0, "在黑名单中");
        // 对方的
        QueryWrapper<ChatList> chatListQueryWrapper2 = new QueryWrapper<ChatList>()
                .eq("chat_id", chatId)
                .eq("user_id", userChatList.getAnotherId())
                .notIn("status", 2, 4);
        ChatList anotherChatList = chatListService.getOne(chatListQueryWrapper2);
        // 双方都有权限
        if (anotherChatList != null && !files.isEmpty()) {
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
            if (!state) return new Result<>(0, "操作失败");
            // 修改图片地址
            // 保存对话信息到数据库
            ChatComment chatComment = new ChatComment();
            chatComment.setChatId(chatId);
            chatComment.setUserId(userId);
            chatComment.setType(1);
            chatComment.setIsLatest(0);
            chatComment.setContent("http://localhost:8088/image/" + newFileName);
            boolean saved = chatCommentService.save(chatComment);
            if (!saved) {
                // 保存图片地址到数据库 失败就删除图片文件
                if (file.exists()) {
                    FileSystemUtils.deleteRecursively(file);
                } else {
                    System.out.println("目标文件不存在");
                }
            }

            // 如果状态为1 双方状态修改为0
            if (userChatList.getStatus() == 1) {
                userChatList.setStatus(0);
                anotherChatList.setStatus(0);
            }
            userChatList.setIsOnline(1);
            anotherChatList.setIsOnline(1);

            // 将对方的信息列表未读+1
            anotherChatList.setUnread(anotherChatList.getUnread() + 1);
            // 修改对方的对话列表
            chatListService.updateById(anotherChatList);

            // 修改用户的对话列表
            chatListService.updateById(userChatList);

            return new Result<>(1, "发送成功");
        }
        return new Result<>(0, "在对方黑名单中");
    }

    /**
     * 删除聊天
     */
    @GetMapping("/chat/delete")
    public Result deleteChat(@RequestParam("chatId") int chatId,
                             @RequestHeader("token") String token) {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        // 判断聊天列表的状态 状态为2，3，4 不可删除
        // 操作的用户的表信息
        QueryWrapper<ChatList> chatListQueryWrapper1 = new QueryWrapper<ChatList>()
                .eq("chat_id", chatId)
                .eq("user_id", userId)
                .notIn("status", 2, 4);
        ChatList userChatList = chatListService.getOne(chatListQueryWrapper1);
        if (userChatList == null) return new Result<>(0, "取消黑名单才可删除");
        // 对方的
        QueryWrapper<ChatList> chatListQueryWrapper2 = new QueryWrapper<ChatList>()
                .eq("chat_id", chatId)
                .eq("user_id", userChatList.getAnotherId())
                .notIn("status", 2, 4);
        ChatList anotherChatList = chatListService.getOne(chatListQueryWrapper2);
        // 判断是否可以删除
        // 真删
        if (userChatList.getStatus() == 0 && anotherChatList.getStatus() == 0) {
            // 删除所有聊天记录的图片
            QueryWrapper<ChatComment> findType = new QueryWrapper<ChatComment>()
                    .eq("chat_id", chatId).eq("type", 1);
            List<ChatComment> chatCommentList = chatCommentService.list(findType);
            for (ChatComment chatComment : chatCommentList) {
                // 获取数据库图片网络地址
                String picHttpAddress = chatComment.getContent();
                // 传图片链接删除图片文件
                common.deleteFileImg(picHttpAddress);
            }
            // 删除所有聊天记录
            QueryWrapper<ChatComment> chatIdWrapper = new QueryWrapper<ChatComment>().eq("chat_id", chatId);
            chatCommentService.remove(chatIdWrapper);
            // 修改双方聊天列表的状态为1
            userChatList.setStatus(1);
            userChatList.setIsOnline(0);
            anotherChatList.setStatus(1);
            anotherChatList.setIsOnline(0);
            chatListService.updateById(userChatList);
            chatListService.updateById(anotherChatList);
            return new Result<>("删除成功");
        } else {
            userChatList.setIsOnline(0);
            chatListService.updateById(userChatList);
            return new Result<>("删除成功");
        }
    }

    /**
     * 拉黑对方
     */
    @GetMapping("/chat/blacklist")
    public Result blacklistChat(@RequestParam("chatId") int chatId,
                                @RequestHeader("token") String token) throws IllegalAccessException {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<ChatList> chatListQueryWrapper = new QueryWrapper<ChatList>()
                .eq("chat_id", chatId)
                .eq("user_id", userId);
        ChatList chatList = chatListService.getOne(chatListQueryWrapper);
        if (chatList != null) {
            // 如果已经举报则设置为4
            switch (chatList.getStatus()) {
                case 2:
                    chatList.setStatus(0);
                    break; // 可选
                case 3:
                    chatList.setStatus(4);
                    break; // 可选
                case 4:
                    chatList.setStatus(3);
                    break; // 可选
                // 你可以有任意数量的case语句
                default: // 可选
                    chatList.setStatus(2);
            }
            chatList.setIsOnline(1);
            // 修改我的聊天列表的状态
            chatListService.update(chatList, chatListQueryWrapper);

            // 调用底部复用方法处理聊天列表的数据
            HashMap<String, Object> newChatList = hadleChatLit(chatList);

            return new Result<>(1, "操作成功", newChatList);
        }
        return new Result<>(0, "操作失败");
    }

    /**
     * 举报对方
     */
    @GetMapping("/chat/report")
    public Result reportChat(@RequestParam("chatId") int chatId,
                             @RequestHeader("token") String token) throws IllegalAccessException {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        QueryWrapper<ChatList> chatListQueryWrapper = new QueryWrapper<ChatList>()
                .eq("chat_id", chatId)
                .eq("user_id", userId);
        ChatList chatList = chatListService.getOne(chatListQueryWrapper);

        if (chatList != null) {

            if (chatList.getStatus() == 2 || chatList.getStatus() == 4) {
                chatList.setStatus(4);
                // 可选
            } else {
                chatList.setStatus(3);
            }
            // 列表状态修改为
            // 修改我的聊天列表的状态
            chatListService.update(chatList, chatListQueryWrapper);

            // 调用底部复用方法处理聊天列表的数据
            HashMap<String, Object> newChatList = hadleChatLit(chatList);
            return new Result<>(1, "操作成功", newChatList);
        }
        return new Result<>(0, "操作失败");
    }


    private HashMap<String, Object> hadleChatLit(ChatList oneChatList) throws IllegalAccessException {
        HashMap<String, Object> newChatList = new HashMap<>();
        // 循环把对象的属性和值赋值新的map集合
        handleObject(oneChatList, newChatList);
        // 目标用户的个人信息
        User user = userService.getById(oneChatList.getAnotherId());
        newChatList.put("anotherName", user.getNickname());
        newChatList.put("anotherPic", user.getPic());

        // 最新一条对话信息
        QueryWrapper<ChatComment> chatIdWrapper = new QueryWrapper<ChatComment>()
                .eq("chat_id", oneChatList.getChatId())
                .orderByDesc("id")
                .last("LIMIT 1");
        ChatComment chatComment = chatCommentService.getOne(chatIdWrapper);
        if (chatComment == null) {
            // 自动添加一条打招呼的信息
            ChatComment newComment1 = new ChatComment();
            newComment1.setChatId(oneChatList.getChatId());
            newComment1.setUserId(oneChatList.getAnotherId());
            newComment1.setContent("Hi~");
            newComment1.setType(0);
            newComment1.setIsLatest(0);
            chatCommentService.save(newComment1);

            ChatComment newComment2 = new ChatComment();
            newComment2.setChatId(oneChatList.getChatId());
            newComment2.setUserId(oneChatList.getUserId());
            newComment2.setContent("你好呀，很高兴认识你！");
            newComment2.setType(0);
            newComment2.setIsLatest(0);
            chatCommentService.save(newComment2);
            return newChatList;
        }
        newChatList.put("content", chatComment.getContent());
        newChatList.put("type", chatComment.getType());
        newChatList.put("createTime", chatComment.getCreateTime());
        return newChatList;
    }

}
