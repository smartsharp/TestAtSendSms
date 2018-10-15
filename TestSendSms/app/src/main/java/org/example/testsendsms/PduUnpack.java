package org.example.testsendsms;

public class PduUnpack {
    	private String smscLen; // 短消息中心号长度
   		private String smscFormat; 
   		private String smsc; // 短消息中心号
   		private int addrLen; // 源地址长度
  		private String addrFormat; 
   		private String addr; // 源地址 
    	private String msgCoding; // 短消息内容编码方式,tp_dcs
  		private String timestamp; // 时间戳,tp_scts
    	private int msgLen=0; 
  		private String msgContent; // 短消息内容,tp_ud 
   		public PduUnpack(String src){ 
    		if (src != null && src.length() >44) {
    			
    			smscLen=src.substring(0,2);
    			smscFormat=src.substring(2,4);
    			
    			String temp = src.substring(4, 18);
    			smsc = prepare.interChange(temp);
    		 	if (smsc != null && smsc.length() > 1){
     				smsc = smsc.substring(0, smsc.length() - 1);
     				if(smsc.length()==13)	smsc=smsc.substring(2);
    		} 
    		temp = src.substring(20, 22);
   			addrLen = Integer.parseInt(temp, 16);
   			if (addrLen % 2 == 0)
   			{
  				temp = src.substring(24, 24 + addrLen);
  				
   			}
    		else 
                {
    			
    			temp = src.substring(24, 24 + addrLen + 1);
                }
    		addr = prepare.interChange(temp);    		
    		// 去掉为补齐为偶数加上的那一位
    	 	if (addr != null && addr.length() % 2 == 0) {
    	 		if(addr.endsWith("F"))
     		addr = addr.substring(0, addr.length() - 1);     			
    		 	if (addr.length() == 13)// 如果前面有86，去掉它
      			addr = addr.substring(2);
    		}
    		if (addrLen % 2 == 0) {
     			msgCoding = src.substring(24 + addrLen + 2, 24 + addrLen + 4);
     			msgLen=Integer.parseInt(src.substring(24+addrLen+18,24+addrLen+20),16);
     			timestamp=prepare.interChange(src.substring(24 + addrLen + 4,24 + addrLen + 4 + 12));
     			temp = src.substring(24 + addrLen + 4 + 16);
    		} 
        else {
     			msgCoding = src.substring(24 + addrLen + 3, 24 + addrLen + 5);
     			msgLen=Integer.parseInt(src.substring(24+addrLen+19,24+addrLen+21),16);
     			timestamp=prepare.interChange(src.substring(24 + addrLen + 5,24 + addrLen + 5 + 12));
     			temp = src.substring(24 + addrLen + 5 + 16);
    		}
   	 		if (msgCoding.equals("08"))
     			msgContent = prepare.unicode2gb(temp);
    		else if(msgCoding.equals("00"))
     			msgContent = prepare.decode7bit(temp);
 		}
   }
   
 //返回发消息着电话号码
   public String getTel() {
   return addr;
  }
 //返回消息编码格式
   public String getMsgCoding() {
   return msgCoding;
  }
//返回消息内容
   
   public String getMsgContent() {
   return msgContent;
  }
//返回消息长度
   public int getMsgLen() {
   return msgLen;
  }
//返回信息中心号码
   public String getSmsc() {
   return smsc;
  }
   //返回消息发送时间
 	public String getTimeStamp(){
	 return timestamp;
 }
 }
