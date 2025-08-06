package com.app.manage;


import com.app.controller.dto.responsedto.MemeberInfoStatusVO;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Slf4j
public class LogCostFilter implements Filter {

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        List<MemeberInfoStatusVO> lst = MemeberInfoStatus.getInstance().GetList();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        Object loginkey = request.getSession().getAttribute("memid");
        MemeberInfoStatusVO vo = null;
        if (null != loginkey) {
            long memid = Long.parseLong(loginkey.toString());
            for (MemeberInfoStatusVO temp : lst
            ) {
                if (temp.getMemId() == memid) {
                    vo = temp;
                }
            }
            if (vo == null) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (vo.getMemStatus() == 1) {
                request.setAttribute("memberexception", "1");
                request.getRequestDispatcher("/app/otherapplicationinfo/memberStatusError").forward(request, servletResponse);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
    }

    @Override
    public void destroy() {

    }

}