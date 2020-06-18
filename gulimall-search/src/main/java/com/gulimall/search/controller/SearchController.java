package com.gulimall.search.controller;

import com.gulimall.search.service.MallSearchService;
import com.gulimall.search.vo.SearchParam;
import com.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-16-20:22
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        param.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);

        return "list";
    }
}
