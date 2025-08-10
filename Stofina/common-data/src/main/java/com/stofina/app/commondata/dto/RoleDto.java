package com.stofina.app.commondata.dto;

import com.stofina.app.commondata.model.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {
    private Long id;

    private RoleType roleType;

}
