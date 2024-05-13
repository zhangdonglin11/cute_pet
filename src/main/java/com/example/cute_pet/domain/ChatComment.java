package com.example.cute_pet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName chat_comment
 */
@TableName(value ="chat_comment")
@Data
public class ChatComment implements Serializable {
    /**
     * 聊天内容id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主表聊天id
     */
    private Integer chatId;

    /**
     * 消息所有者id
     */
    private Integer userId;

    /**
     * 聊天内容
     */
    private String content;

    /**
     * 发送时间
     */
    private Date createTime;

    /**
     * 消息类型 0文字 1图片 2宠物
     */
    private Integer type;

    /**
     * 是不是最后一条消息（默认1）
     */
    private Integer isLatest;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ChatComment other = (ChatComment) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getChatId() == null ? other.getChatId() == null : this.getChatId().equals(other.getChatId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getIsLatest() == null ? other.getIsLatest() == null : this.getIsLatest().equals(other.getIsLatest()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getChatId() == null) ? 0 : getChatId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getIsLatest() == null) ? 0 : getIsLatest().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", chatId=").append(chatId);
        sb.append(", userId=").append(userId);
        sb.append(", content=").append(content);
        sb.append(", createTime=").append(createTime);
        sb.append(", type=").append(type);
        sb.append(", isLatest=").append(isLatest);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}