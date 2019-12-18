package org.pff.beans;
import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.primefaces.PrimeFaces;
import org.pff.ParamsManager;
import org.pff.PointState;
import org.pff.Result;

import javax.ejb.ApplicationException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.pff.Result.drawPointJS;
import static org.pff.Result.drawPointJSD;

@ApplicationScoped
@ManagedBean(name="result")
public class ResultBean implements Serializable {
    private final String sessionID;

    private double curX = 0;
    private double curY = 0;
    private double curR = 2;

    public double getCurX() {
        return curX;
    }

    public void setCurX(double curX) {
        this.curX = curX;
    }

    public double getCurY() {
        return curY;
    }

    public void setCurY(double curY) {
        this.curY = curY;
    }

    public double getCurR() {
        return curR;
    }

    public void setCurR(double curZ) {
        this.curR = curZ;
    }

    ArrayList<Result> data;
    private Session ormSession;

    private float number = 2;

    public ResultBean() throws SQLException, ClassNotFoundException {
        data = new ArrayList<>();
        FacesContext fCtx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(true);



        sessionID = session.getId();
        try {
            Configuration configuration = new Configuration();
            configuration.configure("main/resources/hibernate.cfg.xml");
            configuration.addAnnotatedClass(org.pff.Result.class);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                    configuration.getProperties()).build();
            ormSession = configuration.buildSessionFactory(serviceRegistry).openSession();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void checkClick() {
        try {
            Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

            String sx = requestParameterMap.get("pX").replace(',','.');
            String sy = requestParameterMap.get("pY").replace(',','.');
            String sr = requestParameterMap.get("pR").replace(',','.');
            //String sr2 = requestParameterMap.get("myForm:param-r").replace(',','.');

            float x= Float.parseFloat(sx);
            float y= Float.parseFloat(sy);
            float r= Float.parseFloat(sr);
            String col;
            if(ParamsManager.isValidR(r) && ParamsManager.isValidX(x) && ParamsManager.isValidY(y)) {
                if (ParamsManager.isInArea(x, y, r)) {
                    col = "'#00FF00'";
                    addPoint(x,y,r, PointState.IN);

                } else {
                    col = "'#FF0000'";
                    addPoint(x,y,r, PointState.OUT);
                }
            }
            else
            {
                addPoint(x,y,r, PointState.ODZ);
                col = "'#AFAFAF'";
            }
            System.out.println("stfu");
            PrimeFaces.current().executeScript(drawPointJS(x, y, col));
            //return addResult(x,y,r);



        } catch (Exception e) {
            e.printStackTrace();
            //return "check";
        }
        return;
    }
    public void callRedraw(){
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String sr = requestParameterMap.get("pR").replace(',','.');
        double ssr= Double.parseDouble(sr);
        List<Result> res = getAllResults();
        for (int i = 0; i < res.size(); i++) {
            Result r = res.get(i);
            String col;
            if (ParamsManager.isInArea(r.getX(),r.getY(),ssr)){
                col = "'#00FF00'";
            } else {
                col = "'#FF0000'";
            }
            PrimeFaces.current().executeScript(drawPointJSD(r.getX(), r.getY(), col));
        }
    }
    public void removeResult(){
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String sid = requestParameterMap.get("removeForm:param-id");
        int id = Integer.parseInt(sid);
        try{
            ormSession.getTransaction().begin();
            ormSession.createQuery("delete from Result e where e.resultID = :id").setParameter("id", id).executeUpdate();
            ormSession.getTransaction().commit();
        }catch (Exception e){
            ormSession.getTransaction().rollback();
        }
    }

    private void addPoint(double x, double y, double r, PointState match)
    {
        try{

            ormSession.getTransaction().begin();

            ormSession.save(new Result(x,y,r, match,sessionID));

            ormSession.getTransaction().commit();


            data.add(new Result(x,y,r, match,sessionID));

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public List<Result> getAllResults(){
        System.out.println("Triggered");
        ArrayList<Result> temp = new ArrayList<>(data);
        Collections.reverse(temp);
        CriteriaBuilder builder = ormSession.getCriteriaBuilder();

        CriteriaQuery<Result> cquery = builder.createQuery(Result.class);

        Root<Result> root = cquery.from( Result.class );
        cquery.select( root );
        cquery.where(builder.equal( root.get("sessionID"), sessionID ));
        List<Result> persons = ormSession.createQuery( cquery ).getResultList();
        Collections.reverse(persons);
        return persons;
    }
    public void addResult(){
        try{
            Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

            String sx = requestParameterMap.get("myForm:param-x_input").replace(',','.');
            String sy = requestParameterMap.get("myForm:param-y").replace(',','.');
            String sr = requestParameterMap.get("myForm:param-r").replace(',','.');

            float x= Float.parseFloat(sx);
            float y= Float.parseFloat(sy);
            float r= Float.parseFloat(sr);
            addPoint(x,y,r,ParamsManager.getPointState(x, y, r));

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
