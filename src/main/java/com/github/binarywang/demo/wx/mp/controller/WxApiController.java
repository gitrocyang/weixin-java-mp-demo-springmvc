package com.github.binarywang.demo.wx.mp.controller;

import com.github.binarywang.demo.wx.mp.service.TulingService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <pre>
 *  注意：此contorller 实现WxMpMenuService接口，仅是为了演示如何调用所有菜单相关操作接口，
 *      实际项目中无需这样，根据自己需要添加对应接口即可
 * </pre>
 *
 * @author Binary Wang(https://github.com/binarywang)
 */
@RestController
@RequestMapping("/wechat/api")
public class WxApiController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private WxMpService wxService;

  @Autowired
  private TulingService tlService;

  /*
  @Autowired
  private HttpServletRequest request;*/


  /**
   * 创建jsSDK签名
   * @param url
   * @return
   * @throws WxErrorException
   */
  @PostMapping("/jssignature")
  public String getJsapiSignature(@RequestBody String url) throws WxErrorException {
    WxJsapiSignature auth = this.wxService.createJsapiSignature(url);
    return auth.toString();
  }

  @PostMapping("/oauth2")
  public String getAuth2Url() throws WxErrorException {
    return wxService.oauth2buildAuthorizationUrl("http://roc.t.maple-soft.com/wechat/api/oauth2callback",
      WxConsts.OAuth2Scope.SNSAPI_USERINFO,
      null);
  }

  @GetMapping("/oauth2callback")
  public String getAuth2Callback(@RequestParam String code,
                                 @RequestParam String state ){
    this.logger.info("code=" + code);
    this.logger.info("state=" + state);
    //获取AccessToken
    WxMpUser wxMpUser = null;
    try {
      WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxService.oauth2getAccessToken(code);
      wxMpUser = wxService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
      WxMpKefuMessage message = WxMpKefuMessage
        .TEXT()
        .toUser(wxMpUser.getOpenId())
        .content("hello," + wxMpUser.getNickname() + ",我找到你了！")
        .build();
      wxService.getKefuService().sendKefuMessage(message);
    } catch (WxErrorException e){
      this.logger.error(e.getMessage());
    }

    return wxMpUser.toString();
  }

  @GetMapping("/talk")
  public String getTalk(){
    String rtn = tlService.talk("你好啊！","333994873");
    return rtn;
  }
}
