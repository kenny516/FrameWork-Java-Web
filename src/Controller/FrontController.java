package Controller;

import Annotation.Get;
import Model.ModelAndView;
import Utils.AccesController;
import Utils.Mapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {


    HashMap<String, Mapping> road_controller = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process_request(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process_request(req, resp);
    }

    public void process_request(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String url_taped = req.getServletPath();
        PrintWriter print = res.getWriter();
        print.println(url_taped);
        if (this.road_controller.get(url_taped) != null) {
            Mapping mapping = this.road_controller.get(url_taped);
            try {
                // Load the class dynamically
                Class<?> controllerClass = Class.forName(mapping.getClass_name());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                // Get the method to be invoked
                Method method = controllerClass.getMethod(mapping.getMethod_name());
                // Invoke the method and get the return value
                Object returnValue = method.invoke(controllerInstance);

                if (returnValue instanceof ModelAndView modelView) {
                    handleModelAndView(modelView, req, res);
                } else if(returnValue instanceof String) {
                    // Print the return value
                    print.println("Return value Method=> " + returnValue);
                }else{
                    throw new IOException("return type is not supported =>"+returnValue.getClass().getSimpleName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                print.println("Error invoking method: " + e.getMessage());
            }
        } else {
            throw new IOException("road not found 404 for this URL " + url_taped);
        }
    }

    private void handleModelAndView(ModelAndView modelView, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Map<String, Object> modelData = modelView.getData();

        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        RequestDispatcher dispatcher = req.getRequestDispatcher(modelView.getUrl());
        dispatcher.forward(req, res);
    }


    @Override
    public void init() throws ServletException {
        super.init();
        String directory_controller = getServletContext().getInitParameter("controller");
        String realPath = getServletContext().getRealPath(directory_controller);
        String package_class = directory_controller.split("/")[directory_controller.split("/").length - 1];
        ArrayList<Class<?>> controllers_list = AccesController.getControllerList(package_class, realPath);
        for (Class<?> controller : controllers_list) {
            for (Method method : controller.getMethods()) {
                // existe
                if (road_controller.get(method.getAnnotation(Get.class).road_url()) != null) {
                    if (method.isAnnotationPresent(Get.class)) {
                        road_controller.put(
                                method.getAnnotation(Get.class).road_url(),
                                new Mapping(controller.getName(), method.getName())
                        );
                    }
                } else {
                    throw new ServletException("Url already exist =>" + method.getAnnotation(Get.class).road_url());
                }

            }
        }
    }


}
