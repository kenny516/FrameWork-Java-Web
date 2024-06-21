package Controller;

import Annotation.Get;
import Model.ModelAndView;
import Utils.AccesController;
import Utils.Mapping;
import Utils.Requestparam;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.DefaultParanamer;
import com.thoughtworks.paranamer.Paranamer;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {
    HashMap<String, Mapping> road_controller = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        try {
            process_request(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            process_request(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void process_request(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String url_taped = req.getServletPath();
        PrintWriter print = res.getWriter();
        print.println(url_taped);
        if (this.road_controller.get(url_taped) != null) {
            Mapping mapping = this.road_controller.get(url_taped);
            try {
                // Load the class dynamically

                Object returnValue = handleMethod(req, mapping);
                Method m = mapping.getMethod();
                print.println(m.getParameters()[0]);
                ////////
                if (returnValue instanceof ModelAndView modelView) {
                    handleModelAndView(modelView, req, res);
                } else if (returnValue instanceof String) {
                    // Print the return value
                    print.println("Return value Method=> " + returnValue);

                } else {
                    throw new IOException("Return type is not supported =>" + returnValue.getClass().getSimpleName());
                }
            } catch (Exception e) {
                for (StackTraceElement ste: e.getStackTrace()) {
                    print.println(ste.toString());
                }
                print.println("message = "+e.getMessage());
                if(e.getCause() != null){
                    print.println("cause = "+e.getCause());
                }
            }
        } else {
            throw new IOException("Road not found 404 for this URL =>" + url_taped);
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

    private Object handleMethod(HttpServletRequest request, Mapping mapping) throws Exception {
        // instantiation of class
        Class<?> controllerClass = Class.forName(mapping.getClass_name());
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

        // Get the method to be invoked
        Method method = mapping.getMethod();

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return method.invoke(controllerInstance);
        }
        Object[] paramValues = new Object[parameters.length];

        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] paramNames = paranamer.lookupParameterNames(method);

        Requestparam requestparam = new Requestparam(request);
        for (int i = 0; i < parameters.length; i++) {
            paramValues[i] = requestparam.mappingParam(parameters[i],paramNames[i]);
        }

        // Invoke the method and get the return value
        return method.invoke(controllerInstance, paramValues);
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
                if (method.isAnnotationPresent(Get.class)) {
                    if (road_controller.get(method.getAnnotation(Get.class).road_url()) == null) {
                        road_controller.put(
                                method.getAnnotation(Get.class).road_url(),
                                new Mapping(controller.getName(), method)
                        );
                    } else {
                        throw new ServletException("Url already exist =>" + method.getAnnotation(Get.class).road_url());
                    }
                }
            }
        }
    }


}
