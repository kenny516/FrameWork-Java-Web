package Controller;

import Annotation.*;
import Annotation.validation.Validator;
import Model.CustomSession;
import Model.ModelAndView;
import Utils.*;
import com.google.gson.Gson;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import error.ErrorHandler;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.util.*;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class FrontController extends HttpServlet {
    Gson json = new Gson();
    HashMap<String, Mapping> road_controller = new HashMap<>();
    CustomSession customSession;
    ServletException servletException;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, "POST");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, "GET");
    }


    private void handleException(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException {
        ErrorHandler.handleException(req, res, e);
    }

    private void handleRestApiException(HttpServletResponse res, Exception e) throws IOException {
        ErrorHandler.handleRestApiException(res, e);
    }


    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String requestMethod) throws IOException {
        try {
            String urlTaped = req.getServletPath();
            customSession.setHttpSession(req.getSession(true));
            Mapping mapping = road_controller.get(urlTaped);

            if (mapping == null) {
                handleException(req, resp, new ServletException("Url not found => " + urlTaped));
                return;
            }

            HashSet<VerbAction> verbActions = mapping.getVerbActions();
            Method method = null;
            for (VerbAction verbAction : verbActions) {
                if (requestMethod.equals(verbAction.getVerb())) {
                    method = verbAction.getMethod();
                    break;
                }
            }
            if (method == null) {
                handleException(req, resp, new ServletException("Access denied for  method doesn't exist" + requestMethod + " not verb found for URL :"+urlTaped));
                return;
            }


            process_request(req, resp, method);
        } catch (Exception e) {
            handleException(req, resp, e);
        }
    }


    public void process_request(HttpServletRequest req, HttpServletResponse res, Method method) throws Exception {
        if (res.isCommitted()) {
            return; // Prevent further response processing if already committed
        }
        if (this.servletException != null) {
            handleException(req, res, this.servletException);
            return;
        }

        PrintWriter print = res.getWriter();
        try {
            Class<?> controllerClass = method.getDeclaringClass();
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            this.handleFields(req, controllerClass, controllerInstance);
            Object returnValue = handleMethod(req, method, controllerInstance);
            if (returnValue == void.class) {
                return;
            }
            if (method.isAnnotationPresent(RestApi.class)) {
                res.setContentType("application/json");
                if (!res.isCommitted()) {  // Ensure the response isn't committed
                    if (returnValue instanceof ModelAndView modelView) {
                        print.write(json.toJson(modelView.getData()));
                    } else {
                        print.write(json.toJson(returnValue));
                    }
                }
            } else {
                if (!res.isCommitted()) {  // Ensure response isn't committed for JSP// Ensure response isn't committed for JSP
                    if (Validator.verifyErrorRequest(req)) {
                        ModelAndView modelAndView = (ModelAndView) returnValue;
                        String formOrigin = (String) modelAndView.getData().get("error");
                        Mapping methodFormOrigin = road_controller.get(formOrigin);

                        HttpServletRequest getRequest = new HttpServletRequestWrapper(req) {
                            @Override
                            public String getMethod() {
                                return "GET";
                            }
                        };
                        HashSet<VerbAction> verbActions = methodFormOrigin.getVerbActions();
                        for (VerbAction verbAction : verbActions) {
                            if (verbAction.getVerb().equals("GET")) {
                                Method methodForm = verbAction.getMethod();
                                Object returnValueForm = handleMethod(getRequest, methodForm, controllerInstance);
                                if (returnValueForm instanceof ModelAndView modelView) {
                                    handleModelAndView(modelView, getRequest, res);
                                }
                                else {
                                    throw new ServletException("retour de l'url form error n'est pas de type modelAndView");
                                }
                                break;
                            }
                        }

                    } else {
                        if (returnValue instanceof ModelAndView modelView) {
                            handleModelAndView(modelView, req, res);
                        } else if (returnValue instanceof String) {
                            print.println(returnValue);
                        } else {
                            throw new IOException("Return type is not supported =>" + returnValue.getClass().getSimpleName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (method.isAnnotationPresent(RestApi.class)) {
                handleRestApiException(res, e);
            } else {
                handleException(req, res, e);
            }
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

    private void handleFields(HttpServletRequest request, Class controllerClass, Object controllerInstance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field[] listFields = controllerClass.getDeclaredFields();
        for (Field field : listFields) {
            if (field.getType() == CustomSession.class) {
                Method setCustomSession = controllerClass.getDeclaredMethod("set" + Tools.capitalize(field.getName()), CustomSession.class);
                setCustomSession.invoke(controllerInstance, this.customSession);
            }
        }
    }

    private Object handleMethod(HttpServletRequest request, Method method, Object controllerInstance) throws Exception {

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return method.invoke(controllerInstance);
        }
        Object[] paramValues = new Object[parameters.length];

        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] paramNames = paranamer.lookupParameterNames(method);

        Requestparam requestparam = new Requestparam(request);
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == CustomSession.class) {
                // add custom session to paramValues
                paramValues[i] = this.customSession;
            } else if (!parameters[i].isAnnotationPresent(Param.class)) {
                throw new ServletException("ETU 2409 : Annotation Param not found for this method =>" + method.getName());
            } else {
                paramValues[i] = requestparam.mappingParam(parameters[i], paramNames[i]);
            }
        }
        // Invoke the method and get the return value
        return method.invoke(controllerInstance, paramValues);
    }

    public String handleRequestMethod(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(Post.class)) {
            return "POST";
        } else {
            return "GET";
        }
    }


    @Override
    public void init() throws ServletException {
        super.init();
        this.customSession = new CustomSession();
        String directory_controller = getServletContext().getInitParameter("controller");
        String realPath = getServletContext().getRealPath(directory_controller);
        String package_class = directory_controller.split("/")[directory_controller.split("/").length - 1];
        ArrayList<Class<?>> controllers_list = AccesController.getControllerList(package_class, realPath);
        for (Class<?> controller : controllers_list) {
            for (Method method : controller.getMethods()) {
                // existe
                if (method.isAnnotationPresent(Url.class)) {
                    String url = method.getAnnotation(Url.class).road_url();
                    Mapping mappingCurrent = road_controller.get(url);
                    String verb = handleRequestMethod(method);
                    if (mappingCurrent == null) {
                        VerbAction verbAction = new VerbAction(verb, method);
                        Mapping mp = new Mapping();
                        mp.setClass_name(controller.getName());
                        mp.getVerbActions().add(verbAction);

                        road_controller.put(url, mp);
                    } else {
                        if (!controller.getName().equals(mappingCurrent.getClass_name())) {
                            this.servletException = new ServletException("ETU 2409 :a controller " + mappingCurrent.getClass_name() + " already use this url =>" + url);
                        } else if (!mappingCurrent.getVerbActions().add(new VerbAction(verb, method))) {
                            this.servletException = new ServletException("ETU 2409 : Method " + verb + " already exist for this url =>" + url);
                        }
                    }
                }
            }
        }
    }
}
