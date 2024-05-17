package Controller;

import Annotation.Controller;
import Utils.AccesXML;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FrontController extends HttpServlet {
    boolean checked = false;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process_request(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            IOException {
        process_request(req, resp);
    }

    public void process_request(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter print = resp.getWriter();
        if (!this.checked) {
            checked = true;
            String directory_controller =  getServletContext().getInitParameter("controller");
            String realPath = getServletContext().getRealPath(directory_controller);
            ArrayList<Class<?>> controllers_list = null;
            try {
                controllers_list = AccesXML.find_classes(realPath);
                for (Class<?> controller_class : controllers_list) {
                    if (controller_class.getAnnotation(Controller.class) != null) {
                        print.println(controller_class.getSimpleName());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void init() throws ServletException {
        super.init();

    }


}