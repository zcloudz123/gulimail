package com.gulimall.product.web;

import com.gulimall.product.entity.CategoryEntity;
import com.gulimall.product.service.CategoryService;
import com.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-15-15:43
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        //查出一级分类
//        long start = System.currentTimeMillis();
        List<CategoryEntity> category = categoryService.getLevel1Category();
//        long end = System.currentTimeMillis();
//        System.out.println("消耗的时间："+ (end - start));
        model.addAttribute("categories",category);

        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson(){

        return categoryService.getCatalogJson();
    }

    @ResponseBody
    @RequestMapping("hello")
    public String hello(){
        return "hello";
    }
}
