package com.yld.hx.newyearparty;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.yld.hx.newyearparty.cache.WechatCache;
import com.yld.hx.newyearparty.utils.SHA1;
@Controller
@RequestMapping(value = "recv")
public class MessageController {
	Log log = LogFactory.getLog(MessageController.class);

	@Autowired
	WechatCache wechatCache;
	
	@RequestMapping(value = "message", method = RequestMethod.GET)
	public void message(HttpServletRequest request, HttpServletResponse response) {
		
		String signature = request.getParameter("signature"); // 随机字符串
		log.debug("signature : " + signature);
		String echostr = request.getParameter("echostr"); // 时间戳
		log.debug("echostr : " + echostr);
		String timestamp = request.getParameter("timestamp"); // 随机数
		log.debug("timestamp : " + timestamp);
		String nonce = request.getParameter("nonce");
		log.debug("nonce : " + nonce);
		String[] str = { "hanxu", timestamp, nonce };
		Arrays.sort(str); // 字典序排序
		String bigStr = str[0] + str[1] + str[2]; // SHA1加密
		String digest = SHA1.TOSHA1(bigStr).toLowerCase();
		log.debug(digest);
		if (digest.equals(signature)) {
			log.debug("验证通过！");
			try {
				response.getWriter().write(echostr);
				response.getWriter().flush();
				response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@RequestMapping(value = "message", method = RequestMethod.POST)
	public void messagerecv(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/html;charset=UTF-8");
        //获取服务器发送过来的信息，因为不是参数，得用输入流读取
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try{
            reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null){
                sb.append(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{
                if (null != reader){ reader.close();}
            } catch (IOException e){
                e.printStackTrace();
            }
        }
		
		log.info("收到消息：" + sb.toString());
	}
	
}
