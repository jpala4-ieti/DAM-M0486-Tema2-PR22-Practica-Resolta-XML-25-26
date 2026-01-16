package com.project;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Manager {
    private static SessionFactory factory;

    // Crear SessionFactory
    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            factory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error creant SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Tancar SessionFactory
    public static void close() {
        if (factory != null) {
            factory.close();
        }
    }

    // CREATE - Afegir Ciutat
    public static Ciutat addCiutat(String nom, String pais, Integer poblacio) {
        Session session = factory.openSession();
        Transaction tx = null;
        Ciutat ciutat = null;
        try {
            tx = session.beginTransaction();
            ciutat = new Ciutat(nom, pais, poblacio);
            session.persist(ciutat);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return ciutat;
    }

    // CREATE - Afegir Ciutada
    public static Ciutada addCiutada(String nom, String cognom, Integer edat) {
        Session session = factory.openSession();
        Transaction tx = null;
        Ciutada ciutada = null;
        try {
            tx = session.beginTransaction();
            ciutada = new Ciutada(nom, cognom, edat);
            session.persist(ciutada);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return ciutada;
    }

    // UPDATE - Actualitzar Ciutat amb els seus ciutadans
    public static void updateCiutat(Long ciutatId, String nom, String pais, Integer poblacio, Set<Ciutada> ciutadans) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Ciutat ciutat = session.find(Ciutat.class, ciutatId);
            if (ciutat != null) {
                ciutat.setNom(nom);
                ciutat.setPais(pais);
                ciutat.setPoblacio(poblacio);
                
                // Assignar ciutadans a la ciutat
                for (Ciutada ciutada : ciutadans) {
                    Ciutada ciutadaDB = session.find(Ciutada.class, ciutada.getCiutadaId());
                    if (ciutadaDB != null) {
                        ciutadaDB.setCiutat(ciutat);
                    }
                }
                ciutat.setCiutadans(ciutadans);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // UPDATE - Actualitzar Ciutada
    public static void updateCiutada(Long ciutadaId, String nom, String cognom, Integer edat) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Ciutada ciutada = session.find(Ciutada.class, ciutadaId);
            if (ciutada != null) {
                ciutada.setNom(nom);
                ciutada.setCognom(cognom);
                ciutada.setEdat(edat);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // DELETE - Esborrar entitat genèrica
    public static <T> void delete(Class<T> clazz, Long id) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            T entity = session.find(clazz, id);
            if (entity != null) {
                session.remove(entity);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // READ - Llistar colecció genèrica
    public static <T> Collection<T> listCollection(Class<T> clazz, String orderBy) {
        try (Session session = factory.openSession()) {
            String hql = "FROM " + clazz.getSimpleName();
            if (orderBy != null && !orderBy.isEmpty()) {
                hql += " ORDER BY " + orderBy;
            }
            List<T> results = session.createQuery(hql, clazz).list();
            
            // Força la inicialització de col·leccions lazy
            for (T entity : results) {
                org.hibernate.Hibernate.initialize(entity);
            }
            
            return results;
        }
    }

    // READ - Obtenir ciutat amb ciutadans (soluciona LazyInitializationException)
    public static Ciutat getCiutatWithCiutadans(Long ciutatId) {
        try (Session session = factory.openSession()) {
            Ciutat ciutat = session.find(Ciutat.class, ciutatId);
            if (ciutat != null) {
                // Força la càrrega dels ciutadans abans de tancar la sessió
                org.hibernate.Hibernate.initialize(ciutat.getCiutadans());
            }
            return ciutat;
        }
    }

    // Mètode auxiliar per convertir col·lecció a String
    public static String collectionToString(Class<?> clazz, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return "[Cap " + clazz.getSimpleName() + " trobat]";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }
}