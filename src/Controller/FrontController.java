package Controller;

import Annotation.Controller;
import Utils.AccesController;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FrontController extends HttpServlet {

    ArrayList<Class<?>> controllers_list;

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
        for (Class<?> controller_class : this.controllers_list) {
            if (controller_class.getAnnotation(Controller.class) != null) {
                print.println(controller_class.getSimpleName());
            }
        }
    }


    @Override
    public void init() throws ServletException {
        super.init();
        String directory_controller = getServletContext().getInitParameter("controller");
        String realPath = getServletContext().getRealPath(directory_controller);
        String package_class = directory_controller.split("/")[directory_controller.split("/").length - 1];
        this.controllers_list = AccesController.getControllerList(package_class, realPath);


    }


}
