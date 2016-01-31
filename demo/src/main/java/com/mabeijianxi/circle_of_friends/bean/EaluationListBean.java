package com.mabeijianxi.circle_of_friends.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mabeijianxi on 2015/12/31.
 * 评论列表
 */
public class EaluationListBean implements Serializable {
    //    是否匿名，0不匿名，1匿名
    public int anonymous;
    //    评论图片列表
    public List<EaluationPicBean> attachments;
    public AvatarBean avatar;

    @Override
    public String toString() {
        return "EaluationListBean{" +
                "anonymous=" + anonymous +
                ", attachments=" + attachments +
                ", content='" + content + '\'' +
                ", creatTime='" + creatTime + '\'' +
                ", evaluationId=" + evaluationId +
                ", grade=" + grade +
                ", sid=" + sid +
                ", orderId='" + orderId + '\'' +
                ", userName='" + userName + '\'' +
                ", evaluatereplys=" + evaluatereplys +
                '}';
    }

    /**
     * 评论的图片信息
     */
    public class EaluationPicBean implements Serializable {
        public int height;
        public int width;
        public int x;
        public int y;
        public int attachmentId;
        public String imageId;
        //        原图
        public String imageUrl;
        //        缩略图
        public String smallImageUrl;

        @Override
        public String toString() {
            return "EaluationPicBean{" +
                    "attachmentId=" + attachmentId +
                    ", imageId='" + imageId + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", smallImageUrl='" + smallImageUrl + '\'' +
                    '}';
        }
    }

    /**
     * 用户头像信息
     */
    public class AvatarBean implements Serializable {
        public String mongoId;
        public String picUrl;
        public String smallPicUrl;

        @Override
        public String toString() {
            return "AvatarBean{" +
                    "mongoId='" + mongoId + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    ", smallPicUrl='" + smallPicUrl + '\'' +
                    '}';
        }
    }

    //    内容信息
    public String content;
    //    评论时间
    public String creatTime;
    //    评论id
    public int evaluationId;
    //    评分
    public int grade;
    //    商品id
    public int sid;
    //    小区id
    public String orderId;
    public String userName;
    //回复列表内容
    public List<EvaluatereplysBean> evaluatereplys;

}
