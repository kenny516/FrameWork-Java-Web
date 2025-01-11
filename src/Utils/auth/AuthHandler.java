package Utils.auth;

import jakarta.servlet.ServletContext;

import javax.security.sasl.AuthenticationException;

public class AuthHandler {
    String roleAttributeName;
    public AuthHandler() {
        this.roleAttributeName = "";
    }
    public AuthHandler(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }

    public String getRoleAttributeName() {
        return roleAttributeName;
    }
    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }

    public void setupAuth(ServletContext servletContext) throws AuthenticationException {
        setRoleAttributeName(servletContext.getInitParameter("role"));
        if (this.roleAttributeName == null || this.roleAttributeName.isEmpty()){
            throw new AuthenticationException("roleAttributeName attribut in web.xml configuration is empty");
        }
    }

    public boolean isAuthorized(String role,String roleRequis){
        if (roleRequis == null || roleRequis.isEmpty()){
            return true;
        }else if (role == null || role.isEmpty()){
            return false;
        }
        return role.equals(roleRequis);
    }




}
