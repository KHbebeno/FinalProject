package com.bebeno.mvc.shop.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.bebeno.mvc.shop.model.vo.Shop;

@Mapper
public interface ShopMapper {

	List<Shop> findAll(@Param("shCate") String shCate, @Param("shRegionD1") String shRegionD1, @Param("shKeyword") String shKeyword);

	int insertShop(Shop shop);

	Shop selectShopByNo(@Param("no") int no);
}
