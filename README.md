
# Java Web Framework

## Project Overview
This project involves building a lightweight Java-based web framework that handles controllers, routing, and form management through a series of sprints. Each sprint progressively adds functionality to the framework, from simple URL mapping to form handling and exception management.

## Sprints Breakdown

### **SPRINT 0: FrontController Creation**
- **Goal:** Create a `FrontController` to capture and display the URL entered by the user.
- **Description:**
    - Developed the `FrontController` to handle HTTP requests and print the URL for basic validation.

---

### **SPRINT 1: Controllers Listing**
- **Goal:** Modify the `FrontController` to list all controllers in the project.
- **Description:**
    - Created a `Controller` annotation for marking classes as controllers.
    - Introduced an XML configuration file to specify the directory containing the controllers.
    - Extended the `FrontController` to dynamically detect and list all annotated controllers.

---

### **SPRINT 2: Route Mapping for Controllers**
- **Goal:** Implement routing for controllers using annotations.
- **Description:**
    - Added a `Controller` annotation to denote classes and a `Get` annotation to map routes to methods.
    - Modified the `FrontController` to associate URLs with controller classes and methods.
    - Mapped specific URLs to corresponding methods, displaying which class and method the route points to.

---

### **SPRINT 3: Displaying Function Return Values**
- **Goal:** Display the return values of functions corresponding to URLs.
- **Description:**
    - Updated the framework to invoke the method mapped to the URL and display its return value in the response.

---

### **SPRINT 4: JSP Page Redirection**
- **Goal:** Handle requests and redirect to JSP pages.
- **Description:**
    - Introduced `ModelAndView` class for managing model data and views.
    - Enabled the `FrontController` to redirect to JSP pages, providing a seamless view rendering process.

---

### **SPRINT 5: Exception Handling**
- **Goal:** Integrate exception handling into the framework.
- **Description:**
    - Added exception handling to `FrontController` and `AccessController`.
    - Handled both system and user-defined exceptions, improving error management and debugging.

---

### **SPRINT 6: Form Handling**
- **Goal:** Enable form data submission and processing.
- **Description:**
    - Built functionality to capture form data and call the corresponding function based on the URL.
    - Displayed the function’s response after form submission.
    - Implemented a `Param` annotation for mapping form fields to method parameters.
    - Note: To ensure proper mapping, compile the project using the `-parameters` flag.

---

### **SPRINT 7: Form Handling with Object Mapping**
- **Goal:** Map form fields directly to an object’s properties.
- **Description:**
    - Collected form arguments and set them into a Java object, then returned the object for further processing.

---

### **SPRINT 8: Handling Session**

---

### **SPRINT 9: REST API Implementation**
- **Goal:** Implement a REST API layer.
- **Description:**
    - Created a new `RestApi` annotation for REST-enabled controllers.
    - Modified the `FrontController` to detect and verify the presence of the `RestApi` annotation.
    - Retrieved the return value of methods annotated with `RestApi`, converted the data to JSON using the Gson library, and returned it to the client via `getWriter` for JSON responses.

---
