package com.miniw.fescommon.interceptor;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.miniw.fescommon.base.dto.UserLoginData;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.base.vo.ResultCode;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.utils.CommonUtil;
import com.miniw.fescommon.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 登录状态 拦截器
 *
 * @author miniw
 * @date 2021/08/03
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private final static String AUTH_SALT_1 = "c8c93222583741bd828579b3d3efd43b_1";
    private final static String AUTH_SALT_2 = "c8c93222583741bd828579b3d3efd43b";

    @Resource
    private final RedisUtil redisUtil;

    public UserInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!verifyCookie(request) && !verifyParams(request)) {
            sendMsg(response);
            return false;
        }
        // 获取登录态中的参数存入redis
        return checkForRequestParam(request, response);
    }

    private boolean checkForRequestParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uinStr = request.getParameter("uin");
        if (ObjectUtil.isEmpty(uinStr)) {
            verifyFailMsg(response);
            return false;
        }

        String timeStr = request.getParameter("time");
        long time = 0;
        if (ObjectUtil.isNotEmpty(timeStr)) {
            time = Long.parseLong(timeStr);
        }

        // 用户登陆数据
        UserLoginData userLoginData = new UserLoginData();
        userLoginData.setUin(uinStr);
        userLoginData.setTs(time);
        // 语言id
        String langStr = request.getParameter("lang");
        if (langStr != null) {
            userLoginData.setLangId(langStr);
        }

        // 服务器版本
        String ver = request.getParameter("ver");
        if (ver != null) {
            userLoginData.setVer(ver);
        }

        // 渠道 id
        String apiId = request.getParameter("apiid");
        if (apiId != null) {
            userLoginData.setApiId(apiId);
        }

        // 国家
        String county = request.getParameter("country");
        if (county != null) {
            userLoginData.setCountry(county);
        }
        // 登录态放入缓存中
        if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, uinStr)) {
            redisUtil.hPut(RedisKeyConstant.USER_LOGIN_DATA, uinStr, userLoginData);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * 国内登录态
     * cookie中获取
     */
    private boolean verifyCookie(HttpServletRequest request) {
        try {

            Cookie[] cookies = request.getCookies();
            if (cookies == null || cookies.length <= 0)
                return false;

            String uinStr = null;
            String timeStr = null;
            String sign = null;
            String s2t = null;
            String auth = null;

            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("uin")) {
                    uinStr = cookie.getValue();
                }
                if (cookie.getName().equals("time")) {
                    timeStr = cookie.getValue();
                }
                if (cookie.getName().equals("sign")) {
                    sign = cookie.getValue();
                }
                if (cookie.getName().equals("s2t")) {
                    s2t = cookie.getValue();
                }
                if (cookie.getName().equals("auth")) {
                    auth = cookie.getValue();
                }
            }

            if (StringUtils.isBlank(uinStr) || StringUtils.isAllBlank(sign, auth, s2t))
                return false;

            return verifyAuth(CommonUtil.initValue(uinStr), sign, auth, s2t, CommonUtil.initValue(timeStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 海外登录态
     * 请求参数中获取
     */
    public boolean verifyParams(HttpServletRequest request) {
        try {
            long uin = CommonUtil.initValue(request.getParameter("uin"));
            if (uin <= 0)
                return false;
            String sign = request.getParameter("sign");
            String s2t = request.getParameter("s2t");
            String auth = request.getParameter("auth");
            long time = CommonUtil.initValue(request.getParameter("time"));

            return verifyAuth(uin, sign, auth, s2t, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 登录态校验
     */
    public boolean verifyAuth(long uin, String sign, String auth, String s2t, long time) {
        if (!CommonUtil.positive(uin))
            return false;

        if (StringUtils.isNotEmpty(sign))
            return verifyAuthBySign(uin, sign, AUTH_SALT_1) || verifyAuthBySign(uin, sign, AUTH_SALT_2);

        return verifyAuthByAuth(uin, auth, s2t, time, AUTH_SALT_1) || verifyAuthByAuth(uin, auth, s2t, time, AUTH_SALT_2);
    }

    /**
     * sign 校验
     *
     * @param uin  迷你号
     * @param sign sign
     * @param salt 签名密钥
     */
    private boolean verifyAuthBySign(Long uin, String sign, String salt) {
        if (sign == null)
            return false;

        String[] strings = sign.split("_");
        if (strings.length != 2)
            return false;

        String newSign = DigestUtils.md5DigestAsHex((uin + salt + strings[1]).getBytes());

        if (!newSign.equals(strings[0]))
            return false;

        return true;
    }

    /**
     * 授权信息校验
     *
     * @param uin  迷你号
     * @param auth 授权字串
     * @param s2t  ？？
     * @param time 时间戳
     * @param salt 签名密钥
     */
    private boolean verifyAuthByAuth(long uin, String auth, String s2t, long time, String salt) {
        if (auth == null || s2t == null || !CommonUtil.positive(time))
            return false;

        String s = DigestUtils.md5DigestAsHex((uin + salt + s2t).getBytes());
        String newAuth = DigestUtils.md5DigestAsHex((time + s + uin).getBytes());
        if (!newAuth.equals(auth))
            return false;

        return true;

    }

    /**
     * 授权失败消息包
     */
    private void sendMsg(HttpServletResponse response) throws IOException {
        Result<Object> resultMsg = new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), ResultCode.SYS_NEED_LOGIN.getMsg());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE); //过时变量，官方解释：浏览器默认为UTF-8编码
        PrintWriter out = response.getWriter();
        out.append(JSON.toJSONString(resultMsg));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }


    /**
     * 参数校验失败消息包
     *
     * @param response 响应
     * @throws IOException ioexception
     */
    void verifyFailMsg(HttpServletResponse response) throws IOException {
        Result<Object> resultMsg = new Result<>(ResultCode.SYS_PARAM_ERROR.getCode(), ResultCode.SYS_PARAM_ERROR.getMsg());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.append(JSON.toJSONString(resultMsg));
        response.setStatus(ResultCode.SYS_PARAM_ERROR.getCode());
    }
}
