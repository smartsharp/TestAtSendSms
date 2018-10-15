package org.example.testsendsms;

import java.io.ByteArrayOutputStream;

public class prepare {
	public static String interChange(String src) {
		String result = null;

		if (src != null) {
		if (src.length() % 2 != 0)
		src += "F";
		src += "0";
		result = "";
		for (int i = 0; i < src.length() - 2; i += 2) {
		result += src.substring(i + 1, i + 2);
		result += src.substring(i, i + 1);
		}
		}

		return result;
	}
	public static String decode7bit(String src) {
		   String result = null;
		         int[] b;
		         String temp=null;
		         byte srcAscii;
		         byte left=0;
		         
		    if (src != null && src.length() %2==0) {
		            result="";
		            b=new int[src.length() /2];
		            temp=src+"0";
		             for(int i=0,j=0,k=0;i<temp.length() -2;i+=2,j++){
		              b[j]=Integer.parseInt(temp.substring(i,i+2),16);		              
		              k=j % 7;
		              srcAscii=(byte)(((b[j]<<k) & 0x7F)|left);
		              result+=(char)srcAscii;
		              left=(byte)(b[j]>>>(7-k));
		               if(k==6){
		               result+=(char)left;
		               left=0;
		              }
		              if(j==src.length() /2)
		               result+=(char)left;
		            }
		            
		   }
		   return result;
	}
	public static String encode7bit(String src) {
		   String result = null;
		   String hex = null;
		   byte value;
		 
		    if (src != null && src.length() == src.getBytes().length) {
		    result = "";
		    byte left=0;
		    byte[] b = src.getBytes();
		     for (int i = 0, j = 0; i < b.length; i++) {
		     j =i & 7;//得到0-7的数相当于i%7	
		     //System.out.print(j);
		     if (j == 0)
		      left = b[i];
		      else {
		      value =(byte)((b[i] << (8 - j))|left);//b[i]右移动8-j位
		      left=(byte)(b[i]>>j);//b[i]左移动生成前几位
		      hex = prepare.byte2hex((byte) value);    
		      result += hex;
		      if(i==b.length -1)
		       result+=prepare.byte2hex(left);	    
		     }
		    }		    
		    result=result.toUpperCase();
		    System.out.println("转化为7进制"+result);
		   }
		   return result;
	}
	public static String byte2hex(byte by){
		 String str=null;
		 int num;
		 Byte by1=(Byte)by;
		 num=by1.intValue();
		 //System.out.println(num);
		 if(num>=0&&num<=255)
		 str=Integer.toHexString(num);
		 else 
			 str=Integer.toHexString(num&0xFF) ;
		 if(str.length()==1){str="0"+str;}
		 
		 return str;
	}
    public static String unicode2gb(String hexString) {
       StringBuffer sb = new StringBuffer();

       if (hexString == null)
        return null;

        for (int i = 0; i + 4 <= hexString.length(); i = i + 4) {
         try {
         int j = Integer.parseInt(hexString.substring(i, i + 4), 16);
         sb.append((char) j);
         } catch (NumberFormatException e) {
         return hexString;

        }
       }

       return sb.toString();
	}
    public static String gb2unicode(String gbString) {
        String result = "";
        char[] c;
        int value;

        if (gbString == null)
            return null;
        // if (gbString.getBytes().length == gbString.length())
        // return gbString;

        String temp = null;
        c = new char[gbString.length()];
        StringBuffer sb = new StringBuffer(gbString);
        sb.getChars(0, sb.length(), c, 0);// 将字符从此序列复制到目标字符数组 c
        for (int i = 0; i < c.length; i++) {
            value = (int) c[i];
            //System.out.println("[" + i + "]:" +value );
            //System.out.println("hex:"+Integer.toHexString(value));
            temp = Integer.toHexString(value);
            result += fill(temp);
        }

        return result.toUpperCase();
    }

    public static String fill(String str){
       if(str.length()!=4&&str.length()==2)
       {str="00"+str;}
       return str;
    }


    public static String utf8ToUCS2(String utf8String) {
        String result = "";
        char[] c;
        int value;

        if (utf8String == null)
            return null;
        try {
            c = Utf8Utils.toUCS2(utf8String.getBytes("utf-8"));
            String temp = null;
            for (int i = 0; i < c.length; i++) {
                value = (int) c[i];
                temp = Integer.toHexString(value);
                result += fill(temp);
            }

            return result.toUpperCase();
        }catch (Exception e){
            return null;
        }
    }

    private final static byte B_10000000 = 128 - 256;
    private final static byte B_11000000 = 192 - 256;
    private final static byte B_11100000 = 224 - 256;
    private final static byte B_11110000 = 240 - 256;
    private final static byte B_00011100 = 28;
    private final static byte B_00000011 = 3;
    private final static byte B_00111111 = 63;
    private final static byte B_00001111 = 15;
    private final static byte B_00111100 = 60;
    public static String UCS2ToUtf8(String hexString) {
        if (hexString == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int i = 0; i + 4 <= hexString.length(); i = i + 4) {
            char ch = '*';
            try {
                ch = (char) Integer.parseInt(hexString.substring(i, i + 4), 16);
            } catch (NumberFormatException e) {
            }
            if (ch <= 0x007F) {
                baos.write(ch);
            } else if (ch <= 0x07FF) {
                int ub1 = ch >> 8;
                int ub2 = ch & 0xFF;
                int b1 = B_11000000 + (ub1 << 2) +  (ub2 >> 6);
                int b2 = B_10000000 + (ub2 & B_00111111);
                baos.write(b1);
                baos.write(b2);
            } else {
                int ub1 = ch >> 8;
                int ub2 = ch & 0xFF;
                int b1 = B_11100000 + (ub1 >> 4);
                int b2 = B_10000000 + ((ub1 & B_00001111) << 2) + (ub2 >> 6);
                int b3 = B_10000000 + (ub2 & B_00111111);
                baos.write(b1);
                baos.write(b2);
                baos.write(b3);
            }
        }

        return new String(baos.toByteArray());
    }

}
