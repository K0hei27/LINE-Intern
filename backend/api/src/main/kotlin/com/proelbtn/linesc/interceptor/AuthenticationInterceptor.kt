package com.proelbtn.linesc.interceptor

import com.proelbtn.linesc.annotation.Authentication
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletResponse
import org.springframework.web.servlet.ModelAndView
import redis.clients.jedis.Jedis
import javax.servlet.http.HttpServletRequest

@Component
class AuthenticationInterceptor: HandlerInterceptor{
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any) : Boolean {
        var flag = true
        if (handler is HandlerMethod) {
            if (handler.getMethodAnnotation(Authentication::class.java) != null) {
                val jedis = Jedis("localhost")
                val auth = request.getHeader("Authorization")
                if (auth.isNullOrEmpty() || !auth.startsWith("Bearer ")) flag = false
                if (flag) {
                    val token = auth.substring("Bearer ".length)
                    val user = jedis.get(token)
                    request.setAttribute("user", user)
                    flag = !user.isNullOrEmpty()
                }
            }
        }

        if (!flag) response.status = 401

        return flag
    }

    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, dataObject: Any, model: ModelAndView?){
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, dataObject: Any, e: Exception?) {
    }
}