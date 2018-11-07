package com.github.binarywang.demo.wx.mp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TulingConfig {

  @Value("#{tulingProperties.api_key}")
  private String apiKey;

  @Value("#{tulingProperties.api_secret}")
  private String apiSecret;

  @Value("#{tulingProperties.api_server}")
  private String apiServer;

  public String getApiKey() {
    return this.apiKey;
  }

  public String getApiSecret() {
    return this.apiSecret;
  }

  public String getApiServer() {
    return this.apiServer;
  }
}
