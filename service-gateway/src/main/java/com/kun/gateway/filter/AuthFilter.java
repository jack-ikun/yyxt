package com.kun.gateway.filter;


import com.alibaba.fastjson.JSONObject;
import com.kun.common.result.Result;
import com.kun.common.result.ResultCodeEnum;
import com.kun.common.utils.JwtHelper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author jiakun
 * @create 2023-03-07-17:08
 */
public class AuthFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, GatewayFilterChain chain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String path = request.getURI().getPath();

        if(antPathMatcher.match("/**/inner/**",path)){
            ServerHttpResponse response = serverWebExchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        Long userId = this.getUserId(request);
        if(antPathMatcher.match("/api/**/auth/**",path)){
            if(StringUtils.isEmpty(userId)){
                ServerHttpResponse response = serverWebExchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AURH);
            }
        }
        return chain.filter(serverWebExchange);
    }
    /**
     * api接口鉴权失败返回数据
     * @param response
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));

    }

    private Long getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(null  != tokenList) {
            token = tokenList.get(0);
        }
        if(!StringUtils.isEmpty(token)) {
            return JwtHelper.getUserId(token);
        }
        return null;

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
