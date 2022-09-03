package org.smssecure.smssecure.protocol;

public class XmppExchangeWirePrefix extends WirePrefix {
  @Override
  public String calculatePrefix(String message) {
    return super.calculateXmppExchangePrefix(message);
  }
}
