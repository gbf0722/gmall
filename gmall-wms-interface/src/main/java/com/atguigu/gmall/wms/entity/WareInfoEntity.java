package com.atguigu.gmall.wms.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓库信息
 * 
 * @author gao
 * @email 1052651354@qq.com
 * @date 2019-12-03 15:00:47
 */
@ApiModel
@Data
@TableName("wms_ware_info")
public class WareInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	@ApiModelProperty(name = "id",value = "id")
	private Long id;
	/**
	 * 仓库名
	 */
	@ApiModelProperty(name = "name",value = "仓库名")
	private String name;
	/**
	 * 仓库地址
	 */
	@ApiModelProperty(name = "address",value = "仓库地址")
	private String address;
	/**
	 * 区域编码
	 */
	@ApiModelProperty(name = "areacode",value = "区域编码")
	private String areacode;

}
