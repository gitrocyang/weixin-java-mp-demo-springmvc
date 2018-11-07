package com.github.binarywang.demo.wx.mp.vo;

public class TulingTalk {
  private Perception perception;
  private UserInfo userInfo;

  public Perception getPerception() {
    return perception;
  }

  public void setPerception(Perception perception) {
    this.perception = perception;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(UserInfo userInfo) {
    this.userInfo = userInfo;
  }

  public TulingTalk (String apiKey,String userId,String msg){
    this.perception = new Perception(msg);
    this.userInfo = new UserInfo();
    this.userInfo.setApiKey(apiKey);
    this.userInfo.setUserId(userId);
  }
}
