package com.app.config;

import com.app.manage.MemeberInfoStatus;
import com.app.controller.dto.responsedto.MemeberInfoStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubMsgListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String[] member = new String(message.getBody()).replace("\"", "").split(",");
        MemeberInfoStatusVO vo = new MemeberInfoStatusVO();
        vo.setMemId(Long.parseLong(member[1]));
        vo.setMemStatus(Integer.parseInt(member[3]));
        MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(vo);
        //System.out.println( "收到消息：" +new String(message.getBody()));

//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request= servletRequestAttributes.getRequest();
//        String loginkey = request.getHeader("userSession");
//        loginkey= ReturnCard.GetSingleton().DecodeSession(loginkey);
//        String[] strarr=loginkey.split("_");
//        if(strarr.length > 1){
//           Integer memid =Integer.parseInt( strarr[0]);
//            if(memid==vo.getMemId()){
//                if(vo.getMemStatus()==1){
//                    request.removeAttribute("userSession");
//                    log.info("==========登录状态拦截 去除session,账户已经被冻结");
//                    throw new BizException(1,"账户已经被冻结");
//                }
//
//            }
//        }
    }
}