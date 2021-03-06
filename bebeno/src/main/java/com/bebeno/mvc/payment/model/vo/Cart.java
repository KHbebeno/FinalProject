package com.bebeno.mvc.payment.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

	private int cart_id;
	
	private int cart_qty;
	
	private int wine_bno;
	
	private String id;
	
	private String amount;
	
	private Date addDate;
	
	private String renamedFileName;
}
