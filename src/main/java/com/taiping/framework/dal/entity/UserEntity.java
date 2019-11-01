package com.taiping.framework.dal.entity;

import java.io.Serializable;

import com.taiping.framework.dal.entity.anno.Column;
import com.taiping.framework.dal.entity.anno.ID;
import com.taiping.framework.dal.entity.anno.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table("TMS_USER")
public class UserEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ID(value = "USER_ID", sequence = "TESTUSER")
	private Integer userId;

	@Column("USER_NAME")
	private String userName;
	
	@Column("PASSWORD")
	private String password;
	
	private String email;
	
	@Column("EMP_CODE")
	private String empCode;
	
	@Column("DEPT_ID")
	private String deptId;
	
	@Column("ORG_ROLE")
	private String orgRole;
	
}
