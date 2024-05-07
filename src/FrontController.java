import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class FrontController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process_request(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process_request(req, resp);
    }

    public void process_request(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter print = resp.getWriter();
        print.println(req.getRequestURL());
    }
}
