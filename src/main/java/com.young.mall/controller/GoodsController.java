package com.young.mall.controller;

import com.young.mall.entity.MallUser;
import com.young.mall.redis.GoodsKey;
import com.young.mall.redis.RedisService;
import com.young.mall.result.Result;
import com.young.mall.service.GoodsService;
import com.young.mall.service.MallUserService;
import com.young.mall.vo.GoodsDetailVo;
import com.young.mall.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController extends BaseController {

	@Autowired
	MallUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
    ThymeleafViewResolver thymeleafViewResolver;
	
	@Autowired
    ApplicationContext applicationContext;
	
	/**
	 * QPS:1267 load:15 mysql
	 * 5000 * 10
	 * QPS:2884, load:5 
	 * */
    @RequestMapping(value="/to_list")
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, MallUser user) {
    	model.addAttribute("user", user);
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
    	return render(request, response, model, "goods_list", GoodsKey.getGoodsList, "");
    }
    
    @RequestMapping(value="/to_detail2/{goodsId}",produces="text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, MallUser user,
                          @PathVariable("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	
    	//取缓存
    	String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
    	if(!StringUtils.isEmpty(html)) {
    		return html;
    	}
    	//手动渲染
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	model.addAttribute("goods", goods);
    	
    	long startAt = goods.getStartDate().getTime();
    	long endAt = goods.getEndDate().getTime();
    	long now = System.currentTimeMillis();
    	
    	int mallStatus = 0;
    	int remainSeconds = 0;
    	if(now < startAt ) {//秒杀还没开始，倒计时
    		mallStatus = 0;
    		remainSeconds = (int)((startAt - now )/1000);
    	}else  if(now > endAt){//秒杀已经结束
    		mallStatus = 2;
    		remainSeconds = -1;
    	}else {//秒杀进行中
    		mallStatus = 1;
    		remainSeconds = 0;
    	}
    	model.addAttribute("mallStatus", mallStatus);
    	model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";
    	WebContext ctx = new WebContext(request,response,
    			request.getServletContext(),request.getLocale(), model.asMap());
    	html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
    	if(!StringUtils.isEmpty(html)) {
    		redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
    	}
    	return html;
    }
    
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MallUser user,
										@PathVariable("goodsId")long goodsId) {
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	long startAt = goods.getStartDate().getTime();
    	long endAt = goods.getEndDate().getTime();
    	long now = System.currentTimeMillis();
    	int mallStatus = 0;
    	int remainSeconds = 0;
    	if(now < startAt ) {//秒杀还没开始，倒计时
    		mallStatus = 0;
    		remainSeconds = (int)((startAt - now )/1000);
    	}else  if(now > endAt){//秒杀已经结束
    		mallStatus = 2;
    		remainSeconds = -1;
    	}else {//秒杀进行中
    		mallStatus = 1;
    		remainSeconds = 0;
    	}
    	GoodsDetailVo vo = new GoodsDetailVo();
    	vo.setGoods(goods);
    	vo.setUser(user);
    	vo.setRemainSeconds(remainSeconds);
    	vo.setMallStatus(mallStatus);
    	return Result.success(vo);
    }
    
    
}
