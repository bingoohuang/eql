package org.n3r.eql.mtcp.spring;

import org.n3r.eql.mtcp.MtcpContext;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MultiTenantIdInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String tid = req.getHeader("tid");
        String tcode = req.getHeader("tcode");
        MtcpContext.setTenantId(tid);
        MtcpContext.setTenantCode(tcode);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MtcpContext.clear();
    }
}
