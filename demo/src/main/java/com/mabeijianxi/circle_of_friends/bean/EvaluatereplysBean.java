package com.mabeijianxi.circle_of_friends.bean;

import java.io.Serializable;

/**
 * Created by mabeijianxi on 2015/12/31.
 * 评价列表的回复详情
 */
public class EvaluatereplysBean implements Serializable{
    /**
     * 回复id
     */
    public int erid;
    /**
     * 回复内容
     */
    public String erContent;
    /**
     *回复人姓名
     */
    public String erReplyuser;
    /**
     *回复时间
     */
    public String erReplytime;

    @Override
    public String toString() {
        return "EvaluatereplysBean{" +
                "erid=" + erid +
                ", erContent='" + erContent + '\'' +
                ", erReplyuser='" + erReplyuser + '\'' +
                ", erReplytime='" + erReplytime + '\'' +
                '}';
    }
}
