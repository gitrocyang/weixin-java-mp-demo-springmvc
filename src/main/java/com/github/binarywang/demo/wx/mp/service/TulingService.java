package com.github.binarywang.demo.wx.mp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.demo.wx.mp.builder.TextBuilder;
import com.github.binarywang.demo.wx.mp.config.TulingConfig;
import com.github.binarywang.demo.wx.mp.vo.TulingTalk;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.builder.outxml.NewsBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class TulingService {

  @Autowired
  private TulingConfig tulingConfig;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public List<WxMpXmlOutMessage> talkToWeChat(WxMpXmlMessage wxMessage, WeixinService weixinService) {
    //取出用户和消息
    String userId = wxMessage.getFromUser();
    if (userId.length() > 32) {
      userId = userId.substring(userId.length() - 32);
    }
    TulingTalk tt = new TulingTalk(tulingConfig.getApiKey(), userId, wxMessage.getContent());
    JSONObject talk = (JSONObject) JSON.toJSON(tt);
    //perception.put()
    //申明给服务端传递一个json串
    //创建一个OkHttpClient对象
    OkHttpClient okHttpClient = new OkHttpClient();
    //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), talk.toJSONString());
    //创建一个请求对象
    Request request = new Request.Builder()
      .url(tulingConfig.getApiServer())
      .post(requestBody)
      .build();
    //发送请求获取响应
    try {
      Response response = okHttpClient.newCall(request).execute();
      //判断请求是否成功
      if (response.isSuccessful()) {
        //用来返回的消息
        List<WxMpXmlOutMessage> outMsgs = new ArrayList<WxMpXmlOutMessage>();

        JSONObject obj = JSON.parseObject(response.body().string());
        logger.info("收到机器人回复消息：\r\n" + obj.toJSONString());
        JSONArray objects = obj.getJSONArray("results");
        for (Iterator iterator = objects.iterator(); iterator.hasNext(); ) {
          JSONObject msg = (JSONObject) iterator.next();
          String msgType = msg.getString("resultType").toLowerCase();
          switch (msgType) {
            case "text":
              outMsgs.add(createTextMsg(msg,wxMessage,weixinService));
              break;
            case "url":
              outMsgs.add(createUrlMsg(msg,wxMessage,weixinService));
              break;
            case "news":
              outMsgs.add(createNewsMsg(msg,wxMessage,weixinService));
              break;
            default:
              outMsgs.add(createTextMsg(msg,wxMessage,weixinService));
              break;
          }
        }
        if (outMsgs.size() == 0) outMsgs.add(new TextBuilder().build("客服机器人开小差去喝茶了，你也歇会吧！", wxMessage, weixinService));
        return outMsgs;
        //打印服务端返回结果
      } else {
        return null;
      }
    } catch (IOException e) {
      return null;
    }
  }

  private WxMpXmlOutMessage createTextMsg(JSONObject msg, WxMpXmlMessage wxMessage, WeixinService weixinService) {
    return new TextBuilder().build(msg.getJSONObject("values").getString("text"), wxMessage, weixinService);
  }

  private WxMpXmlOutMessage createUrlMsg(JSONObject msg, WxMpXmlMessage wxMessage, WeixinService weixinService) {
    return new TextBuilder().build("你可以访问 " + msg.getJSONObject("values").getString("url") + " 看看", wxMessage, weixinService);
  }

  private WxMpXmlOutMessage createNewsMsg(JSONObject msg, WxMpXmlMessage wxMessage, WeixinService weixinService) {
    JSONArray news = msg.getJSONObject("values").getJSONArray("news");
    if (!news.isEmpty()) {
      //List<WxMpXmlOutNewsMessage.Item> items = new ArrayList<WxMpXmlOutNewsMessage.Item>();
      NewsBuilder nb = WxMpXmlOutMessage.NEWS().fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser());
      Integer i = 0;
      for (Iterator iterator = news.iterator(); iterator.hasNext(); ) {
        i++;
        JSONObject obj = (JSONObject) iterator.next();
        WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
        item.setDescription(obj.getString("info"));
        item.setPicUrl(obj.getString("icon"));
        item.setTitle(obj.getString("name"));
        item.setUrl(obj.getString("detailurl"));
        //items.add(item);
        nb.addArticle(item);
        if (i>=3) break;
      }

      return nb.build();
    }
    return null;
  }


  public String talk(String message, String userId) {

    TulingTalk tt = new TulingTalk(tulingConfig.getApiKey(), userId, message);
    JSONObject talk = (JSONObject) JSON.toJSON(tt);
    //perception.put()
    //申明给服务端传递一个json串
    //创建一个OkHttpClient对象
    OkHttpClient okHttpClient = new OkHttpClient();
    //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), talk.toJSONString());
    //创建一个请求对象
    Request request = new Request.Builder()
      .url(tulingConfig.getApiServer())
      .post(requestBody)
      .build();
    //发送请求获取响应
    try {
      Response response = okHttpClient.newCall(request).execute();
      //判断请求是否成功
      if (response.isSuccessful()) {
        JSONObject obj = JSON.parseObject(response.body().string());
        logger.info("收到机器人回复消息：\r\n" + obj.toJSONString());
        String msg = "";
        JSONArray objects = obj.getJSONArray("results");
        for (Iterator iterator = objects.iterator(); iterator.hasNext(); ) {
          JSONObject msgs = (JSONObject) iterator.next();
          if (msgs.getString("resultType").toLowerCase().equals("text")) {
            msg += msgs.getJSONObject("values").getString("text");
          }
          if (msgs.getString("resultType").toLowerCase().equals("url")) {
            msg += "你可以访问 " + msgs.getJSONObject("values").getString("url") + " 看看\r\n";
          }
        }
        if (msg.isEmpty()) msg = "客服机器人开小差去喝茶了，你也歇会吧！";
        return msg;
        //打印服务端返回结果
      } else {
        return "机器人开小差去喝茶了，你也歇会吧！";
      }
    } catch (IOException e) {
      return "机器人开小差去喝茶了，你也歇会吧！";
    }
  }

}
