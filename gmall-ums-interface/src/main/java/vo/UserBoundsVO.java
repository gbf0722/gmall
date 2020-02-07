package vo;

import lombok.Data;

@Data
public class UserBoundsVO {

    private Long memberId;
    //赠送积分
    private Integer growth;
    //成长积分
    private Integer integration;
}

