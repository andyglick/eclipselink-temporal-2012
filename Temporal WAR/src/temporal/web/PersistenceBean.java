package temporal.web;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class PersistenceBean {

    private String name = "Test@" + System.currentTimeMillis();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
