package com.fupto.back.admin.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class MemberSearchDto {
    private Integer page;
    private Integer size;
    private String role;
    private String memberStatus;
    private String gender;
    private String provider;
    private String searchType;
    private String searchKeyword;
    private String dateType;
    private String startDate;
    private String endDate;

    public MemberSearchDto() {
        this.page = 1;
        this.size = 10;
        this.searchType = "userId";
        this.searchKeyword = "";
        this.memberStatus = "all";
        this.dateType = "all";
        this.provider = "";
    }
}
