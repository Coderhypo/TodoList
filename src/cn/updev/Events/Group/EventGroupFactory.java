package cn.updev.Events.Group;

import cn.updev.Database.HibernateSessionFactory;
import cn.updev.Events.Event.EventFactory;
import cn.updev.Events.Static.EventGroupWeight;
import cn.updev.Events.Static.IEvent;
import cn.updev.Events.Static.ITeamEvents;
import cn.updev.Events.Static.IUserEvents;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.List;

/**
 * Created by hypo on 15-11-17.
 */
public class EventGroupFactory {

    private Date groupExpect;
    private String groupTitle;
    private List<IEvent> list;
    private Integer ownerId;
    private EventGroupWeight weight;

    public EventGroupFactory(Date groupExpect, String groupTitle, List<IEvent> list, Integer ownerId, EventGroupWeight weight) {
        this.groupExpect = groupExpect;
        this.groupTitle = groupTitle;
        this.list = list;
        this.ownerId = ownerId;
        this.weight = weight;
    }

    private Date getGroupExpect() {
        return groupExpect;
    }

    private String getGroupTitle() {
        if(groupTitle.length() > 20){
            this.groupTitle = groupTitle.substring(0,20);
        }
        return groupTitle;
    }

    private List<IEvent> getList() {
        return list;
    }

    private Integer getOwnerId() {
        return ownerId;
    }

    private EventGroupWeight getWeight() {
        return weight;
    }

    private List<IEvent> saveList(List<IEvent> list){

        Session session = HibernateSessionFactory.currentSession();
        Transaction transaction = session.beginTransaction();
        EventFactory factory = new EventFactory();
        for(IEvent event : list){
            event = factory.update(event);
        }

        transaction.commit();
        HibernateSessionFactory.closeSession();

        return list;
    }

    private EventGroupInfo save(){

        Session session = HibernateSessionFactory.currentSession();
        Transaction transaction = session.beginTransaction();

        EventGroupInfo groupInfo = new EventGroupInfo(getGroupExpect(), getGroupTitle(), getOwnerId(), getWeight());
        session.save(groupInfo);
        transaction.commit();
        HibernateSessionFactory.closeSession();

        saveList(getList());

        return groupInfo;
    }

    public IUserEvents getUserEvents(){

        EventGroupInfo groupInfo = save();
        IUserEvents events = new UserEventGroup(groupInfo, getList());

        return  events;
    }

    public ITeamEvents getTeamEvents(){

        EventGroupInfo groupInfo = save();
        ITeamEvents events = new TeamEventGroup(groupInfo, getList());

        return  events;
    }

    public AbstractEventGroup update(AbstractEventGroup eventGroup){

        EventGroupInfo eventGroupInfo = eventGroup.getGroupInfo();

        this.groupExpect = eventGroupInfo.getGroupExpect();
        this.groupTitle = eventGroupInfo.getGroupTitle();
        this.ownerId = eventGroupInfo.getOwnerId();
        this.weight = eventGroupInfo.getWeight();
        this.list = eventGroup.getList();

        eventGroupInfo.setGroupExpect(getGroupExpect());
        eventGroupInfo.setGroupTitle(getGroupTitle());
        eventGroupInfo.setOwnerId(getOwnerId());
        eventGroupInfo.setWeight(getWeight());

        Session session = HibernateSessionFactory.currentSession();
        Transaction transaction = session.beginTransaction();

        session.update(eventGroupInfo);
        transaction.commit();
        HibernateSessionFactory.closeSession();

        saveList(getList());

        return eventGroup;
    }

    public Boolean delete(AbstractEventGroup eventGroup){

        EventGroupInfo eventGroupInfo = eventGroup.getGroupInfo();
        this.list = eventGroup.getList();

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateSessionFactory.currentSession();
            transaction = session.beginTransaction();

            for(IEvent event : list){

                session.delete(event);
            }

            session.delete(eventGroupInfo);
            transaction.commit();
        }catch (Exception e){

            System.err.println(e);
            transaction.rollback();
            return false;
        }finally {

            HibernateSessionFactory.closeSession();
        }

        return true;
    }
}