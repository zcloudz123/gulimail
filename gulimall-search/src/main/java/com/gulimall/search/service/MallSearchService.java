package com.gulimall.search.service;

import com.gulimall.search.vo.SearchParam;
import com.gulimall.search.vo.SearchResult;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-16-21:58
 */
public interface MallSearchService {

    SearchResult search(SearchParam param);
}
