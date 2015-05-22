package com.javahash.hibernate;

import org.hibernate.Session;

import java.util.Date;

public class Run {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Session session = HibernateSessionManager.getSessionFactory().openSession();

        session.beginTransaction();
        User user = new User();

        user.setUserId(1);
        user.setUsername("James");
        user.setCreatedBy("Application");
        user.setCreatedDate(new Date());

        session.save(user);
        session.getTransaction().commit();

    }

}