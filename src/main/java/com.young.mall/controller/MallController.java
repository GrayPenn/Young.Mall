package com.young.mall.controller;

import com.young.mall.access.AccessLimit;
import com.young.mall.entity.MallOrder;
import com.young.mall.entity.MallUser;
import com.young.mall.redis.GoodsKey;
import com.young.mall.redis.MallKey;
import com.young.mall.redis.OrderKey;
import com.young.mall.redis.RedisService;
import com.young.mall.result.CodeMsg;
import com.young.mall.result.Result;
import com.young.mall.service.GoodsService;
import com.young.mall.service.MallService;
import com.young.mall.service.MallUserService;
import com.young.mall.service.OrderService;
import com.young.mall.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/mall")
public class MallController implements InitializingBean {

    @Autowired
    MallUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MallService mallService;

    //TODO 暂时注释
//	@Autowired
//	MQSender sender;

    private static volatile boolean isGlobalActivityOver = false;
    private static HashMap<Long, Integer> stockMap = new HashMap<Long, Integer>();
    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 系统初始化
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMallGoodsStock, "" + goods.getId(), goods.getStockCount());
            stockMap.put(goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    /**
     * 获取初始的商品秒杀数量
     */
    public static int getGoodsStockOriginal(long goodsId) {
        Integer stock = stockMap.get(goodsId);
        if (stock == null) {
            return 0;
        }
        return stock;
    }

    /**
     * 通过管理后台设置一个全局秒杀结束的标志，防止数据库、redis、rabbitmq等发生意外，活动无法结束
     **/
    public static void setGlobalActivityOver() {
        isGlobalActivityOver = true;
    }

    public static boolean isGlobalActivityOver() {
        return isGlobalActivityOver;
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMallGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMallOrderByUidGid);
        redisService.delete(MallKey.isGoodsOver);
        mallService.reset(goodsList);
        return Result.success(true);
    }

    /**
     * QPS:1306
     * 5000 * 10
     * QPS: 2114
     */
    @RequestMapping(value = "/{path}/do_mall", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> mall(Model model, MallUser user,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        if (isGlobalActivityOver()) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //验证path
        boolean check = mallService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMallGoodsStock, "" + goodsId);//10
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        MallOrder order = orderService.getMallOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MALL);
        }
        //入队
		//TODO 暂时注释
//    	MallMessage mm = new MallMessage();
//    	mm.setUser(user);
//    	mm.setGoodsId(goodsId);
//    	sender.sendMallMessage(mm);
        return Result.success(0);//排队中
    	/*
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MallOrder order = orderService.getMallOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = mallService.mall(user, goods);
        return Result.success(orderInfo);
        */
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> mallResult(Model model, MallUser user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = mallService.getMallResult(user.getId(), goodsId);
        return Result.success(result);
    }

    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMallPath(HttpServletRequest request, MallUser user,
                                      @RequestParam("goodsId") long goodsId,
                                      @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode
    ) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = mallService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = mallService.createMallPath(user, goodsId);
        return Result.success(path);
    }


    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMallVerifyCod(HttpServletResponse response, MallUser user,
                                           @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image = mallService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MALL_FAIL);
        }
    }
}
