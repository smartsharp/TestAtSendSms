package org.example.testsendsms;

public class PduPack {
    private String smscLen; // 短消息中心号长度
    private String smscFormat;
  //private String smsc= "683108200005F0"; //"683108901305F0";// 短消息中心号 86-13800931500, 683108501505F0  -> 86-1380055150
    private String smsc= "683110304105F0"; // 8613010314500 //"683108200005F0"; //"683108901305F0";// 短消息中心号 86-13800931500, 683108501505F0  -> 86-1380055150
    private int addrLen; // 源地址长度
    private String addrFormat;
    private String addr; // 源地址
    private String msgCoding; // 短消息内容编码方式,tp_dcs
    private int msgLen;
    private String msgContent; // 短消息内容,tp_ud
    int lengh1;
    public PduPack() {
        smscLen = "08";	smscFormat = "91";   addrLen = 13;   addrFormat = "91";
    }
   //设置信息中心号码
   		
   public void setSmsc(String s) {
    	if (s != null) {
    		String centerNo = null;
     		if (s.length() == 11 && s.substring(0, 2).equals("13")) {
     			centerNo = "86" + s;
    		}
 			else if (s.length() == 13  && s.substring(0, 4).equals("8613")) {
     			centerNo = s;
    		} else if (s.length() == 14 && s.substring(0, 5).equals("+8613")) {
     		centerNo = s.substring(1);
    		} else
     		return; 
    		this.smsc= prepare.interChange(centerNo);
   		}
  }
 
  //设置发送原地址
 public void setAddr(String ad) {
    if (ad != null) {
    	String centerNo = null;
     	if (ad.length() == 11 && ad.substring(0, 2).equals("13")) {
     	centerNo = "86" + ad;
     	} else if (ad.length() == 13 && ad.substring(0, 4).equals("8613")) {
     	centerNo = ad;
     	} else if (ad.length() == 14 && ad.substring(0, 5).equals("+8613")) {
     	centerNo = ad.substring(1);
     	} else if (ad.length() > 0) {// 特服号
     	addrFormat = "A1";
     	addrLen = ad.length();
     	centerNo = ad;
   	 } else
     return; 
     addr = prepare.interChange(centerNo);
   	}
  }
 
   /* 设置编码方式 @param encoding   0:表示7-BIT编码 4:表示8-BIT编码 8:表示UCS2编码  */
   public void setMsgCoding(int encoding) {
   if (encoding == 8)
    msgCoding = "18"; // 08 : default, 18: class 0: 19: class 1, 1a: class2, 1b: class3
   else if (encoding == 4)
    msgCoding = "14";  // 04 default, 14: class 0, 15: calss1, 16: class2, 17: class3
   else
    msgCoding = "10"; // 00 default, 10: class0, 11: clas1, 12: calss2, 13: class3
  }
 
   /* 短消息内容  @param content   */
   public void setMsgContent(String content){
    if (content != null) {
     if (content.length() == content.getBytes().length) {    	
         msgCoding = "00"; // 00 default, 10: class0, 11: clas1, 12: calss2, 13: class3
         msgLen = content.getBytes().length;
         msgContent = prepare.encode7bit(content);
         lengh1=msgContent.length()/2;
     } else {
         msgCoding = "08"; // 08 : default, 18: class 0: 19: class 1, 1a: class2, 1b: class3
         //msgContent = prepare.gb2unicode(content);
         msgContent = prepare.utf8ToUCS2(content);
         if(msgContent!=null)
         msgLen=msgContent.length()/2;
         lengh1=msgLen;
     }
    
     if(msgContent!=null){
        msgContent=msgContent.toUpperCase();
    }
   }
  }
 
   /* @return 经过PDU编码的结果,十六进制字符串形式   */
   public String getCodedResult() {
   String result = null;
   final String tp_mti = "31";
   final String tp_mr = "00";
   final String tp_pid = "00";
   final String tp_vp = "00";
  if (smsc!= null && addr != null && msgContent != null) {
    result = smscLen + smscFormat + smsc + tp_mti + tp_mr
      + prepare.byte2hex((byte) addrLen) + addrFormat + addr
      + tp_pid + msgCoding + tp_vp
      + prepare.byte2hex((byte) msgLen) + msgContent;
    result = result.toUpperCase();
   }
 
   return result;
  } 
   public int getmsgLen(){
	   return lengh1;
   }
   
 }
 
