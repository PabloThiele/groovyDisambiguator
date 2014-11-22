package utils.model;

import utils.enums.TagList;

import java.math.BigDecimal;

/**
 * Created by Pablo_Thiele on 11/18/2014.
 */
public class TagStatistic {

    private TagList tagName;
    private Long hitNumber;
    private Integer tagId;
    private BigDecimal percentageUsed;

    public TagStatistic(TagList tagName, Long hitNumber, Integer tagId) {
        this.tagName = tagName;
        this.hitNumber = hitNumber;
        this.tagId = tagId;

    }

    public TagStatistic(TagList tagName, Long hitNumber, Integer tagId, BigDecimal percentageUsed) {
        this.tagName = tagName;
        this.hitNumber = hitNumber;
        this.tagId = tagId;
        this.percentageUsed = percentageUsed;
    }

    public TagList getTagName() {
        return tagName;
    }

    public void setTagName(TagList tagName) {
        this.tagName = tagName;
    }

    public Long getHitNumber() {
        return hitNumber;
    }

    public void setHitNumber(Long hitNumber) {
        this.hitNumber = hitNumber;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }
}
