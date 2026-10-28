package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.ScenicSpot;
import com.kmbeast.pojo.entity.ScenicSpotBrochure;
import com.kmbeast.pojo.entity.ScenicSpotCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 景点VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScenicSpotVO extends ScenicSpot {

    /**
     * 父级地区ID
     */
    private Integer parentRegionId;

    /**
     * 省份名/直辖市/特殊地区
     */
    private String provinceName;

    /**
     * 市/区/特殊地区
     */
    private String cityName;

    /**
     * 图册
     */
    private List<ScenicSpotBrochure> brochureList;

    /**
     * 景点类别
     */
    private List<ScenicSpotCategory> scenicSpotCategoryList;

    /**
     * 阅读量
     */
    private Integer viewCount;
    /**
     * 点赞量
     */
    private Integer upvoteCount;
    /**
     * 收藏量
     */
    private Integer collectionCount;
}
