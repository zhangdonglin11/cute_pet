package com.example.cute_pet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName chat_list
 */
@TableName(value ="chat_list")
@Data
public class ChatList implements Serializable {
    /**
     * 聊天列表表id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 聊天主表id
     */
    private Integer chatId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 对方用户id
     */
    private Integer anotherId;

    /**
     * 未读数
     */
    private Integer unread;

    /**
     * 正常0，删除1，拉黑2，举报3，举报加拉黑4
     */
    private Integer status;

    /**
     * 是否显示在用户对话列表

     */
    private Integer isOnline;

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
        ChatList other = (ChatList) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getChatId() == null ? other.getChatId() == null : this.getChatId().equals(other.getChatId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAnotherId() == null ? other.getAnotherId() == null : this.getAnotherId().equals(other.getAnotherId()))
            && (this.getUnread() == null ? other.getUnread() == null : this.getUnread().equals(other.getUnread()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getIsOnline() == null ? other.getIsOnline() == null : this.getIsOnline().equals(other.getIsOnline()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getChatId() == null) ? 0 : getChatId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAnotherId() == null) ? 0 : getAnotherId().hashCode());
        result = prime * result + ((getUnread() == null) ? 0 : getUnread().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getIsOnline() == null) ? 0 : getIsOnline().hashCode());
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
        sb.append(", anotherId=").append(anotherId);
        sb.append(", unread=").append(unread);
        sb.append(", status=").append(status);
        sb.append(", isOnline=").append(isOnline);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}