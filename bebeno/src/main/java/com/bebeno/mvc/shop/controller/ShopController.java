package com.bebeno.mvc.shop.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.bebeno.mvc.common.util.FileProcess;
import com.bebeno.mvc.member.model.vo.Member;
import com.bebeno.mvc.shop.model.service.ShopService;
import com.bebeno.mvc.shop.model.vo.ContentFiles;
import com.bebeno.mvc.shop.model.vo.Shop;
import com.bebeno.mvc.shop.model.vo.WineListsOnShop;
import com.bebeno.mvc.wineboard.model.service.WineBoardService;
import com.bebeno.mvc.wineboard.model.vo.WineBoard;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/shop")
public class ShopController {
	@Autowired
	private ShopService service;
	
	@Autowired
	private WineBoardService wineService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@GetMapping("/list")
	public ModelAndView list(ModelAndView model, String shCate, String shRegionD1, String shKeyword) {
		List<Shop> shopList = null;
		
		if(shCate == "") {
			shCate = null;
		} 
		if(shRegionD1 == "") {
			shRegionD1 = null;
		}
		if(shKeyword == "") {
			shKeyword = null;
		} 

		shopList = service.getShopList(shCate, shRegionD1, shKeyword);
		
		model.addObject("shopList", shopList);
		model.setViewName("shop/list");
		
		return model;
	}
	
	@GetMapping("/view")
	@PostMapping("/view")
	public ModelAndView view(ModelAndView model, @RequestParam("no") int no) {	

		Shop shop = service.findShopByNo(no);
		List<ContentFiles> fileList = service.findfilesByNo(no);
		
		shop.setFiles(fileList);
		
		model.addObject("shop", shop);
		model.setViewName("shop/view");
		
		System.out.println(shop);
		System.out.println(shop.getContent());
		
		
		return model;
	}
	
	@PostMapping("/registration") 
	public ModelAndView registration(
			ModelAndView model, 
			@SessionAttribute(name = "loginMember") Member loginMember,
			@ModelAttribute Shop shop, @ModelAttribute ContentFiles file,@RequestParam("upfileFront") MultipartFile upfileFront, MultipartHttpServletRequest mtfRequest
			) {
		
		// MultipartHttpServletRequest ???????????????
		// MultipartHttpServletRequest??? getFiles???????????? ?????? ???????????? List????????? ?????? ??? ??????.
		List<MultipartFile> fileList = mtfRequest.getFiles("upfileContent");
		int result = 0;

		// ????????? ??????????????? ????????? "", ????????? ??????????????? "?????????"
		log.info("upfileFront Name : {}", upfileFront.getOriginalFilename());
		// ????????? ??????????????? ????????? true, ????????? ??????????????? false 
		log.info("upfileFront is Empty : {}", upfileFront.isEmpty());
		
//		 1. ????????? ????????? ????????? ?????? ??? ????????? ??????
		if(upfileFront != null && !upfileFront.isEmpty()) {
			// ????????? ???????????? ?????? ??????
			String location = null;
			String renamedFileName = null;
//			String location = request.getSession().getServletContext().getRealPath("resources/upload/shop");

			try {
				location = resourceLoader.getResource("resources/upload/shop").getFile().getPath();
				renamedFileName = FileProcess.save(upfileFront, location);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(renamedFileName != null) {
				shop.setOriginalFileName(upfileFront.getOriginalFilename());
				shop.setRenamedFileName(renamedFileName);
			}
		}
		
		// 2. ????????? ????????? ???????????? ????????? ???????????? ??????
		shop.setWriterNo(loginMember.getNo());
		
		System.out.println(shop);
		
		result = service.save(shop);
		
		
		// ????????? ???????????? ??????????????? ContentFiles vo??? ??????
		for (MultipartFile mf : fileList) {
			 if(mf != null && !mf.isEmpty()) {
					// ????????? ???????????? ?????? ??????
					String location = null;
					String renamedFileName = null;
//					String location = request.getSession().getServletContext().getRealPath("resources/upload/shop");

					try {
						location = resourceLoader.getResource("resources/upload/shop").getFile().getPath();
						renamedFileName = FileProcess.save(mf, location);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if(renamedFileName != null) {
						file.setShopNo(shop.getNo());
						file.setFile_originalFileName(mf.getOriginalFilename());
						file.setFile_renamedFileName(renamedFileName);
					}
					
					service.fileSave(file);
			 }
       }
		
		if(result > 0) {
			model.addObject("msg", "???????????? ??????????????? ?????????????????????.");
			model.addObject("location", "/shop/list");
		} else {
			model.addObject("msg", "????????? ????????? ?????????????????????.");
			model.addObject("location", "/shop/list");
		}
		
		model.setViewName("common/msg");
		
		return model;
	}
	
	@GetMapping("/update")
	public ModelAndView update(
			@SessionAttribute("loginMember") Member loginMember,
			ModelAndView model, @RequestParam("no") int no) {
		
		Shop shop = service.findShopByNo(no);
		
		if(shop.getType() == "?????????") {
			shop.setType("wineshop");
		} else if (shop.getType() == "????????????") {
			shop.setType("rest");
		}
		
		if (loginMember.getNo() == shop.getWriterNo()) {			
			model.addObject("shop", shop);
			model.setViewName("shop/update");
		} else {
			model.addObject("msg", "????????? ???????????????.");
			model.addObject("location", "/shop/list");
			model.setViewName("common/msg");
		}
		
		return model;
	}
	
	@PostMapping("/update")
	public ModelAndView update(ModelAndView model, 
			@SessionAttribute("loginMember") Member loginMember,
			@ModelAttribute Shop shop, @ModelAttribute ContentFiles file,@RequestParam("upfileFront") MultipartFile upfileFront, MultipartHttpServletRequest mtfRequest
			) {
		List<MultipartFile> newfileList = mtfRequest.getFiles("upfileContent");		
		List<ContentFiles> exfileList = service.findfilesByNo(shop.getNo());	
		
		log.info("newfileList : {}", newfileList.toString());
		log.info("newfileList.size : {}", newfileList.size());
		
		shop.setFiles(exfileList);
		
		int result = 0;
		
		// ????????? ???????????? ????????? ???????????? ???????????? ?????? location??? ?????? ????????? ???????????? ????????? ???????????? ??????
		if (loginMember.getNo() == shop.getWriterNo()) {
			if(upfileFront != null && !upfileFront.isEmpty()) {
				String renamedFileName = null;
				String location = null;
				
				try {
					location = resourceLoader.getResource("resources/upload/shop").getFile().getPath();
					
					if(shop.getRenamedFileName() != null) {
						// ????????? ???????????? ???????????? ??????
						FileProcess.delete(location + "/" + shop.getRenamedFileName());
					}
					
					renamedFileName = FileProcess.save(upfileFront, location);
					
					if(renamedFileName != null) {
						shop.setOriginalFileName(upfileFront.getOriginalFilename());
						shop.setRenamedFileName(renamedFileName);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			result = service.save(shop);
			
			// ?????? ????????? ???????????? ??????????????? ???????????? ?????? ????????? ????????????.
			if(newfileList.get(0).getSize() != 0) {
				String location = null;
				String renamedFileName = null;
				
				try {
					location = resourceLoader.getResource("resources/upload/shop").getFile().getPath();
					
					
					// ????????? ContentFiles????????? ???????????? ????????? db???????????? delete?????? ??????
					
					for(int i = 0; i < shop.getFiles().size(); i++) {
						FileProcess.delete(location + "/" + shop.getFiles().get(i).getFile_renamedFileName());
					}
					service.fileDeleteByStoreNo(shop.getNo());
					
					for(MultipartFile mf : newfileList) {
						// ????????? ?????? location??? ??????????????????????????? ???????????? ??????
						renamedFileName = FileProcess.save(mf, location);
						
						// ????????? ??????????????????????????? ?????? ????????? file????????? ????????? save?????? ??????
						if(renamedFileName != null) {
							file.setShopNo(shop.getNo());
							file.setFile_originalFileName(mf.getOriginalFilename());
							file.setFile_renamedFileName(renamedFileName);
						}
						
						service.fileSave(file);
					}
					
					
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
					
			}
			
			
			//////////////////////
			
			if(result > 0) {
				model.addObject("msg", "???????????? ??????????????? ?????????????????????.");
				model.addObject("location", "/shop/view?no=" + shop.getNo());
			} else {
				model.addObject("msg", "????????? ????????? ?????????????????????.");
				model.addObject("location", "/shop/update?no=" + shop.getNo());
			}
		} else {
			model.addObject("msg", "????????? ???????????????.");
			model.addObject("location", "/shop/list");
		}
		
		model.setViewName("common/msg");
		
		return model;
	}
	
	@GetMapping("/delete")
	public ModelAndView delete(ModelAndView model,
			@SessionAttribute("loginMember") Member loginMember,
			@RequestParam("no")int no) {
		
		int result = 0;
		Shop shop = service.findShopByNo(no);
		
		if(shop.getWriterNo() == loginMember.getNo()) {
			result = service.delete(shop.getNo());
			
			if(result > 0) {
				model.addObject("msg", "???????????? ??????????????? ?????????????????????.");
				model.addObject("location", "/shop/list");
			} else {
				model.addObject("msg", "????????? ????????? ?????????????????????.");
				model.addObject("location", "/shop/view?no=" + shop.getNo());
			}
		} else {
			model.addObject("msg", "????????? ???????????????.");
			model.addObject("location", "/shop/list");
		}
		
		model.setViewName("common/msg");
		
		return model;
	}
	
	@GetMapping("/findWine")
	public ModelAndView findWine(ModelAndView model, String wineKind, String nation, String wineKeyword, @RequestParam("shopNo") int shopNo) {
		List<WineBoard> wineList = null;
		
		if(wineKind == "") {
			wineKind = null;
		} 
		if(nation == "") {
			nation = null;
		}
		if(wineKeyword == "") {
			wineKeyword = null;
		} 
		
		wineList = wineService.findWineListOnShop(wineKind, nation, wineKeyword);
		
		log.info("wineKind Name : {}", wineKind);
		log.info("nation Name : {}", nation);
		log.info("wineKeyword Name : {}", wineKeyword);
		
		
		model.addObject("shopNo", shopNo);
		model.addObject("wineList", wineList);
		model.setViewName("shop/wineSearch");
		
		return model;
	}
	
//	@ResponseBody
//	@PostMapping("/saveWine")
//	public Object saveWine(
//            @RequestParam(value="wineNoList[]") List<String> wineNoList, 
//            @RequestParam(value="shopNo") String shopNo
//            ) {
//		int[] result = new int[wineNoList.size()];
//		
//		ArrayList<WineListsOnShop> wineOnShop = new ArrayList<WineListsOnShop>();
////		WineListsOnShop[] wineOnShop = new WineListsOnShop[wineNoList.size()];
//		WineBoard[] wines = new WineBoard[wineNoList.size()];
//		
//        System.out.println("=shopNo=");
//        System.out.println(shopNo);
//        
//        System.out.println("=wine=");
//        for(int i = 0; i < wineNoList.size(); i++) {
//        	wines[i] =  wineService.findBoardByNo(Integer.parseInt(wineNoList.get(i)));
//        	
//        	wineOnShop[i].setShopNo(Integer.parseInt(shopNo));
//        	wineOnShop[i].setKorName(wines[i].getWineName());
//        	wineOnShop[i].setEngName(wines[i].getWineEng());
//        	wineOnShop[i].setFile_originalFileName(wines[i].getOriginalFileName());
//        	wineOnShop[i].setFile_renamedFileName(wines[i].getRenamedFileName());
//        	
//        	service.saveWinesOnShop(wineOnShop[i]);
//        }
//        
////        for(String wineNo : wineNoList) {
////            System.out.println(wineNo);
////            WineBoard wine =  wineService.findBoardByNo(Integer.parseInt(wineNo));
////            
////            wineOnShop[].setShopNo(wine.getWineBno());
////            
////            
////            result = service.saveWinesOnShop(Integer.parseInt(shopNo), Integer.parseInt(wineNo));
////        }
//        //?????????
//        Map<String, Object> retVal = new HashMap<String, Object>();
//        
//        //??????????????? ??????
////        retVal.put("wineLists", wineLists);
//        retVal.put("message", "????????? ?????? ???????????????.");
//        
//        return retVal;
// 
//    }
	
	
	
}
