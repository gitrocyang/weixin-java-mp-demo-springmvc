package com.github.binarywang.demo.wx.mp.handler;

import com.github.binarywang.demo.wx.mp.builder.TextBuilder;
import com.github.binarywang.demo.wx.mp.service.TulingService;
import com.github.binarywang.demo.wx.mp.service.WeixinService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Binary Wang
 */
@Component
public class MsgHandler extends AbstractHandler {

  @Autowired
  private TulingService tulingService;

  @Override
  public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                  Map<String, Object> context, WxMpService wxMpService,
                                  WxSessionManager sessionManager) {

    WeixinService weixinService = (WeixinService) wxMpService;

    if (!wxMessage.getMsgType().equals(WxConsts.XmlMsgType.EVENT)) {
      //TODO 可以选择将消息保存到本地
    }

    //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
    if (StringUtils.startsWithAny(wxMessage.getContent(), "你好", "客服")
      && weixinService.hasKefuOnline()) {
      return WxMpXmlOutMessage
        .TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
        .toUser(wxMessage.getFromUser()).build();
    }

/*    //TODO 组装回复消息
    String userId = wxMessage.getFromUser();
    if (userId.length() > 32){
      userId = userId.substring(userId.length()-32);
    }
    String content = tulingService.talk(wxMessage.getContent(),userId);
    //"这是一个有个性的公众号，我不一定会理你哦……";

    return new TextBuilder().build(content, wxMessage, weixinService);*/
    List<WxMpXmlOutMessage> msgs = tulingService.talkToWeChat(wxMessage,weixinService);
    if (!msgs.isEmpty()){
      return  msgs.get(msgs.size()-1);
    } else {
      return new TextBuilder().build("这是一个有个性的公众号，我不一定会理你哦……", wxMessage, weixinService);
    }
  }

}
