package com.github.binarywang.demo.wx.mp.vo;

public class Perception {

  private InputText inputText;

  public InputText getInputText() {
    return inputText;
  }

  public void setInputText(InputText inputText) {
    this.inputText = inputText;
  }

  public Perception(String msg){
    this.inputText = new InputText();
    this.inputText.setText(msg);
  }
}
