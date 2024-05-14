# 项目简介

####  这是一个仿宠物森林小程序和添加了论坛功能的后端项目。用户可以添加宠物、浏览其他用户分享的宠物社交信息、发布话题交流的帖子、和其他用户私聊对话等。该项目后端代码使用 Spring Boot 框架搭建，该项目采用了 MyBatis-Plus 进行持久化操作，数据存储使用 MySQL 数据库，同时实现了 JWT（JSON Web Token）进行用户认证和授权。该项目是我做的毕业设计，整个项目我将添加到   <<完整项目.zip>>   中（uniapp实现的小程序代码、vue实现的后台管理、数据库的sql文件、后端项目的代码）

#### 项目主要技术

- Java
- Spring Boot
- MyBatis-Plus
- MySQL
- JWT

#### 文件介绍

```
cute_pet 
├──/src/
	├── /main/
		├── /java/
			├── /com.example.cute_pet/
				├── /config/         配置文件
				├── /controller/     业务代码
				├── /domain/		
				├── /mapper/
				├── /service/
				├── /util/			工具类
				resources
			├── /resources/			静态文件等
```

#### 使用方法

- jdk17 
- mysql 数据库
- java 相关环境等
- idea 代码编辑器

1. 将项目克隆到本地

   ```
   git clone https://github.com/JasonZhang1124/cute_pet.git
   ```

2. 下载依赖

   在idea编辑器打开项目中的pom.xml文件，点击右上角的按钮下载依赖

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/updateMaven.png)

   使用介绍省略......

   

   启动

   ![](https://raw.githubusercontent.com/JasonZhang1124/cute_pet/main/img/start.png)

   #### 小程序端效果展示

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/index.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/topic.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/topicAdd.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/message.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/messageDetail.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/user.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/userUpdate.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/petDetail.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/petAdd.png)

   

   #### 后台管理效果展示

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/adminIndex.png)

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/adminPet.png)

   

   ![](https://github.com/JasonZhang1124/cute_pet/blob/main/img/adminUser.png)