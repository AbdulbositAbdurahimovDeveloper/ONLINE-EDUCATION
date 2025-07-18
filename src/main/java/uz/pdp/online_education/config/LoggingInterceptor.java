package uz.pdp.online_education.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        System.out.println("---- INCOMING REQUEST ----");
//        System.out.println("METHOD : " + request.getMethod());
//        System.out.println("URI    : " + request.getRequestURI());
//        System.out.println("QUERY  : " + request.getQueryString());
//        System.out.println("AUTH   : " + request.getHeader("Authorization"));
//        System.out.println("--------------------------");
//        return true;
//    }


    /**
     *hozircha bu metod ishlamaydi test paytida loglar kopayib ketmasligi uchun keyinroq ishlatib qoyish kerak
     * @param request
     * @param response
     * @param handler
     * @return true
     */
//    @Override
    public boolean preHandle7(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Muvaffaqiyatli log - Yashil rangda
        System.out.println("\n\033[0;32m🔔  📥  NEW REQUEST RECEIVED\033[0m");
        System.out.println("➡️  Method : " + request.getMethod());
        System.out.println("🌐  URI    : " + request.getRequestURI());
        System.out.println("🔍  Query  : " + (request.getQueryString() != null ? request.getQueryString() : "—"));
        System.out.println("🛡️  Token  : " + (request.getHeader("Authorization") != null ? request.getHeader("Authorization") : "—"));
        System.out.println("⏱️  Time   : " + new java.util.Date());
        System.out.println("✅  Proceeding to handler...\n");
        return true;
    }

    /**
     * Bu metod requestni handlerga o‘tkazgan va handlerning ishini tugatganidan so‘ng ishlaydi.
     * -
     * Agar exception bo‘lsa - Qizil rangda
     * Agar xatolik statusi bo‘lsa (4xx yoki 5xx) - Qizil rangda
     * Agar barcha narsa to‘g‘ri bo‘lsa - Yashil rangda
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Agar exception bo‘lsa - Qizil rangda
        if (ex != null) {
            System.out.println("\n\033[0;31m⚠️  EXCEPTION ENCOUNTERED:\033[0m");
            System.out.println("➡️  Exception: " + ex.getMessage());
            System.out.println("🌐  URI: " + request.getRequestURI());
            System.out.println("⏱️  Time: " + new java.util.Date());
            System.out.println("❗  Status: " + response.getStatus());
            System.out.println("💥  Error occurred during request handling\n");
        }
        // Agar xatolik statusi bo‘lsa (4xx yoki 5xx) - Qizil rangda
        else if (response.getStatus() >= 400) {
            System.out.println("\n\033[0;31m⚠️  ERROR RESPONSE RECEIVED:\033[0m");
            System.out.println("\033[0;31m➡️  Status Code: " + response.getStatus() + "\033[0m");
            System.out.println("\033[0;31m🌐  URI: " + request.getRequestURI() + "\033[0m");
            System.out.println("\033[0;31m⏱️  Time: " + new java.util.Date() + "\033[0m");
            System.out.println("\033[0;31m💥  Error occurred during request handling \033[0m");
        }
        // Agar barcha narsa to‘g‘ri bo‘lsa - Yashil rangda
        else {
            System.out.println("\n\033[0;32m✅  SUCCESSFUL REQUEST:\033[0m");
            System.out.println("\033[0;32m➡️  Status Code: " + response.getStatus() + "\033[0m");
            System.out.println("\033[0;32m🌐  URI: " + request.getRequestURI() + "\033[0m");
            System.out.println("\033[0;32m⏱️  Time: " + new java.util.Date() + "\033[0m");
            System.out.println("\033[0;32m👍  Request successfully processed \033[0m");
        }
    }

}
