package com.mabeijianxi.circle_of_friends.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mabeijianxi on 2015/12/31.
 * 评价详情
 */
public class EaluationBean implements Serializable{
//    总评论数
    public int totalCount;
//    页号
    public int pageNo;

    public int pageCount;
//    中评
    public int goodCount;
    public int badCount;
    public int middleCount;
//    好评率
    public String goodPD;
    public ArrayList<EaluationListBean> evaluataions;

    @Override
    public String toString() {
        return "EaluationBean{" +
                "totalCount=" + totalCount +
                ", pageNo=" + pageNo +
                ", pageCount=" + pageCount +
                ", goodCount=" + goodCount +
                ", badCount=" + badCount +
                ", middleCount=" + middleCount +
                ", goodPD='" + goodPD + '\'' +
                ", evaluataions=" + evaluataions +
                '}';
    }
}
