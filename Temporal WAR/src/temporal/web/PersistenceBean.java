package temporal.web;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Person;

import org.eclipse.persistence.internal.weaving.PersistenceWeaved;

@ManagedBean
@SessionScoped
public class PersistenceBean {

    @PersistenceContext(unitName = "example")
    private EntityManager entityManager;

    public String getName() {

        List<Person> persons = getEntityManager().createQuery("SELECT p FROM Person p").getResultList();

        if (!persons.isEmpty()) {
            return persons.get(0).getName() + " - weaving: " + (persons.get(0) instanceof PersistenceWeaved);
        }
        return "No Person instances found";
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
