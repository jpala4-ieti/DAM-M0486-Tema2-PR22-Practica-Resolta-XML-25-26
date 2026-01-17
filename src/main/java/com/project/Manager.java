package com.project;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Classe Manager: Capa d'accés a dades (DAO - Data Access Object)
 * 
 * Aquesta classe encapsula totes les operacions amb la base de dades utilitzant Hibernate.
 * Proporciona mètodes per a les operacions CRUD (Create, Read, Update, Delete) i consultes.
 * 
 * CONCEPTES CLAU DE HIBERNATE:
 * 
 * 1. SessionFactory: Objecte pesat que es crea una sola vegada per aplicació.
 *    Conté la configuració de Hibernate i les metadades dels mapatges.
 * 
 * 2. Session: Representa una connexió amb la base de dades i un context de persistència.
 *    És lleugera i s'ha de crear per a cada operació o grup d'operacions.
 *    Sempre s'ha de tancar després d'usar-la (try-finally o try-with-resources).
 * 
 * 3. Transaction: Representa una transacció de base de dades.
 *    Totes les operacions d'escriptura (INSERT, UPDATE, DELETE) han d'anar dins d'una transacció.
 * 
 * 4. Estats dels objectes en Hibernate:
 *    - Transient: Objecte nou creat amb 'new', no gestionat per Hibernate
 *    - Persistent/Managed: Objecte associat a una Session, Hibernate segueix els seus canvis
 *    - Detached: Objecte que estava Persistent però la seva Session s'ha tancat
 *    - Removed: Objecte marcat per ser esborrat
 */
public class Manager {
    
    // SessionFactory és un atribut estàtic (una sola instància per a tota l'aplicació)
    // És thread-safe i pot ser compartida entre múltiples threads
    private static SessionFactory factory;

    /**
     * PUNT CLAU D'EXAMEN: Configuració inicial de Hibernate
     * 
     * Aquest mètode inicialitza la SessionFactory llegint el fitxer hibernate.cfg.xml.
     * La SessionFactory:
     * - És un objecte "pesat" (triga a crear-se i consumeix memòria)
     * - Només se'n crea UNA per a tota l'aplicació (Singleton pattern)
     * - Conté tota la configuració: connexió BD, dialecte, mapatges, etc.
     * - És thread-safe i immutable després de ser creada
     * - S'utilitza per crear Sessions (objectes lleugers)
     * 
     * PROCÉS:
     * 1. Configuration llegeix hibernate.cfg.xml
     * 2. Valida els fitxers de mapatge (.hbm.xml)
     * 3. Crea la SessionFactory amb aquesta configuració
     * 
     * Si falla (fitxer no trobat, error de configuració, driver no disponible),
     * llança ExceptionInInitializerError per evitar que l'aplicació continuï.
     */
    public static void createSessionFactory() {
        try {
            // Configuration: Classe que llegeix i processa hibernate.cfg.xml
            Configuration configuration = new Configuration();
            
            // configure(): Busca hibernate.cfg.xml al classpath i el carrega
            // També podríem especificar una ruta diferent: configure("altra-config.xml")
            configuration.configure("hibernate.cfg.xml");
            
            // buildSessionFactory(): Crea la SessionFactory amb la configuració carregada
            // Aquest és el pas més "pesat" - aquí Hibernate processa tots els mapatges
            factory = configuration.buildSessionFactory();
            
        } catch (Throwable ex) {
            // Si quelcom falla, imprimim l'error i llancem ExceptionInInitializerError
            // Això evita que l'aplicació continuï sense una SessionFactory vàlida
            System.err.println("Error creant SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Tanca la SessionFactory alliberant tots els recursos.
     * 
     * S'ha de cridar quan l'aplicació es tanca (shutdown hook).
     * Tanca el pool de connexions i allibera memòria.
     */
    public static void close() {
        if (factory != null) {
            factory.close();
        }
    }

    /**
     * OPERACIÓ CREATE: Afegir una nova Ciutat
     * 
     * PUNT CLAU D'EXAMEN: Operació de persistència amb persist()
     * 
     * CICLE DE VIDA DE L'OBJECTE:
     * 1. ciutat = new Ciutat(...) → Estat TRANSIENT (no gestionat per Hibernate)
     * 2. session.persist(ciutat) → Passa a estat PERSISTENT (gestionat per Hibernate)
     * 3. tx.commit() → S'executa l'INSERT a la base de dades
     * 4. session.close() → L'objecte passa a estat DETACHED
     * 
     * TRANSACCIONS:
     * - beginTransaction(): Inicia una transacció
     * - commit(): Confirma els canvis (aquí és quan s'executa el SQL real)
     * - rollback(): Desfà tots els canvis si hi ha un error
     * 
     * GESTIÓ D'ERRORS:
     * - Try-catch per capturar excepcions
     * - Finally per assegurar que sempre es tanca la sessió
     * 
     * @param nom Nom de la ciutat (obligatori)
     * @param pais País on es troba
     * @param poblacio Nombre d'habitants
     * @return L'objecte Ciutat creat amb l'ID assignat per la base de dades
     */
    public static Ciutat addCiutat(String nom, String pais, Integer poblacio) {
        // Obrim una nova sessió (connexió amb la BD)
        Session session = factory.openSession();
        
        // La transacció començarà com a null i s'inicialitzarà dins del try
        Transaction tx = null;
        
        // Variable per retornar la ciutat creada
        Ciutat ciutat = null;
        
        try {
            // SEMPRE iniciem una transacció per a operacions d'escriptura
            // sense transacció, els canvis no es guarden
            tx = session.beginTransaction();
            
            // Creem l'objecte ciutat (estat TRANSIENT)
            ciutat = new Ciutat(nom, pais, poblacio);
            
            // persist(): Afegeix l'objecte a la sessió (estat PERSISTENT)
            // A partir d'ara, Hibernate segueix aquest objecte
            // Nota: encara no s'ha executat l'INSERT a la BD
            session.persist(ciutat);
            
            // commit(): Confirma la transacció
            // AQUÍ és quan Hibernate executa l'INSERT real a la base de dades
            // També assigna l'ID generat per la BD a l'objecte ciutat
            tx.commit();
            
        } catch (Exception e) {
            // Si quelcom falla durant la transacció
            if (tx != null) {
                // Revertim tots els canvis pendents (rollback)
                // La base de dades queda com estava abans del beginTransaction()
                tx.rollback();
            }
            e.printStackTrace();
            
        } finally {
            // SEMPRE tanquem la sessió per alliberar recursos
            // Les sessions mantenen connexions obertes amb la BD
            // Si no les tanquem, esgotem el pool de connexions
            session.close();
        }
        
        // Retornem la ciutat (ara té l'ID assignat i està en estat DETACHED)
        return ciutat;
    }

    /**
     * OPERACIÓ CREATE: Afegir un nou Ciutadà/Ciutadana
     * 
     * Funciona exactament igual que addCiutat.
     * 
     * DIFERÈNCIA AMB addCiutat:
     * - Ciutada no té relacions One-to-Many (no té col·leccions)
     * - Té una relació Many-to-One amb Ciutat (opcional en aquest mètode)
     * 
     * @param nom Nom del ciutadà (obligatori)
     * @param cognom Cognom del ciutadà
     * @param edat Edat del ciutadà
     * @return L'objecte Ciutada creat amb l'ID assignat
     */
    public static Ciutada addCiutada(String nom, String cognom, Integer edat) {
        Session session = factory.openSession();
        Transaction tx = null;
        Ciutada ciutada = null;
        
        try {
            tx = session.beginTransaction();
            
            // Creem el ciutadà sense ciutat assignada
            ciutada = new Ciutada(nom, cognom, edat);
            
            // persist() el passa a estat PERSISTENT
            session.persist(ciutada);
            
            // commit() executa l'INSERT
            tx.commit();
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            
        } finally {
            session.close();
        }
        
        return ciutada;
    }

    /**
     * OPERACIÓ UPDATE: Actualitzar una Ciutat i les seves relacions
     * 
     * PUNT CLAU D'EXAMEN: Gestió de relacions bidireccionals i objectes DETACHED
     * 
     * PROBLEMA DELS OBJECTES DETACHED:
     * - Els objectes Ciutada del Set poden haver estat creats en una sessió anterior
     * - Quan la sessió es tanca, els objectes passen a estat DETACHED
     * - Si intentem usar-los directament en una nova sessió → ERROR
     * - Hibernate llançarà NonUniqueObjectException (objecte duplicat)
     * 
     * SOLUCIÓ: merge()
     * - merge() agafa un objecte DETACHED
     * - Comprova si existeix a la BD (per l'ID)
     * - El torna a associar a la sessió actual (estat MANAGED)
     * - Retorna la versió MANAGED de l'objecte
     * 
     * BIDIRECCIONALITAT:
     * - Hem de mantenir ambdós costats de la relació sincronitzats:
     *   1. ciutat.getCiutadans().add(ciutada) → Col·lecció del pare
     *   2. ciutada.setCiutat(ciutat) → Referència del fill (FK)
     * 
     * CASCADE:
     * - Com hem definit cascade="all" al fitxer XML, els canvis a Ciutat
     *   es propaguen automàticament als Ciutadans relacionats
     * 
     * @param ciutatId ID de la ciutat a actualitzar
     * @param nom Nou nom
     * @param pais Nou país
     * @param poblacio Nova població
     * @param ciutadans Set de ciutadans a associar amb aquesta ciutat
     */
    public static void updateCiutat(Long ciutatId, String nom, String pais, Integer poblacio, Set<Ciutada> ciutadans) {
        Session session = factory.openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            
            // find(): Cerca l'objecte per ID i el carrega a la sessió
            // L'objecte passa a estat PERSISTENT/MANAGED
            // A partir d'ara, qualsevol canvi es detectarà automàticament (Dirty Checking)
            Ciutat ciutat = session.find(Ciutat.class, ciutatId);
            
            if (ciutat != null) {
                // Actualitzem els camps simples
                // Hibernate detectarà aquests canvis i executarà un UPDATE al fer commit
                ciutat.setNom(nom);
                ciutat.setPais(pais);
                ciutat.setPoblacio(poblacio);
                
                // GESTIÓ DE RELACIONS: Netegem les relacions antigues
                // És important mantenir la consistència bidireccional
                if (ciutat.getCiutadans() != null) {
                    // Per cada ciutadà antic, eliminem la referència a aquesta ciutat
                    for (Ciutada c : ciutat.getCiutadans()) {
                        c.setCiutat(null); // Trenca la relació del costat Many-to-One
                    }
                    // Buidem la col·lecció
                    ciutat.getCiutadans().clear();
                }

                // Afegim les noves relacions
                for (Ciutada ciutada : ciutadans) {
                    /**
                     * PUNT CLAU: merge()
                     * 
                     * Per què necessitem merge()?
                     * - ciutada pot estar en estat DETACHED (d'una sessió anterior)
                     * - Si intentem usar-la directament → ERROR
                     * 
                     * Què fa merge()?
                     * - Busca l'objecte a la BD per l'ID
                     * - Si existeix, actualitza els seus camps amb els valors de ciutada
                     * - Retorna la versió MANAGED (associada a la sessió actual)
                     * - Si no existeix, crea un nou registre
                     * 
                     * IMPORTANT: merge() retorna un NOU objecte (o el mateix si ja estava managed)
                     * Hem de treballar amb l'objecte retornat, no amb l'original
                     */
                    Ciutada ciutadaManaged = (Ciutada) session.merge(ciutada);
                    
                    // Establim la bidireccionalitat:
                    // 1. FK al fill (això s'escriu a la columna ciutat_id de ciutadans)
                    ciutadaManaged.setCiutat(ciutat);
                    
                    // 2. Objecte a la col·lecció del pare (només per Java, no afecta BD)
                    ciutat.getCiutadans().add(ciutadaManaged);
                    
                    // Gràcies a cascade="all", quan es guardi ciutat, 
                    // també es guardaran automàticament els canvis a ciutadaManaged
                }
            }
            
            // commit(): Hibernate detecta tots els canvis (Dirty Checking)
            // i executa els UPDATE necessaris
            tx.commit();
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            
        } finally {
            session.close();
        }
    }

    /**
     * OPERACIÓ UPDATE: Actualitzar un Ciutadà/Ciutadana
     * 
     * PUNT CLAU: Dirty Checking
     * 
     * Hibernate té un mecanisme anomenat "Dirty Checking":
     * - Quan carreguem un objecte amb find(), Hibernate guarda el seu estat inicial
     * - Quan fem commit(), compara l'estat actual amb l'inicial
     * - Si detecta canvis (dirty), executa automàticament un UPDATE
     * - NO cal cridar cap mètode update() o save() explícitament
     * 
     * Aquest mecanisme només funciona amb objectes en estat PERSISTENT/MANAGED.
     * 
     * @param ciutadaId ID del ciutadà a actualitzar
     * @param nom Nou nom
     * @param cognom Nou cognom
     * @param edat Nova edat
     */
    public static void updateCiutada(Long ciutadaId, String nom, String cognom, Integer edat) {
        Session session = factory.openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            
            // find(): Carrega l'entitat i la passa a estat PERSISTENT
            // Hibernate guarda internament una "snapshot" de l'estat actual
            Ciutada ciutada = session.find(Ciutada.class, ciutadaId);
            
            if (ciutada != null) {
                // Modifiquem els camps amb els setters
                // Aquests canvis es registren automàticament per Hibernate
                ciutada.setNom(nom);
                ciutada.setCognom(cognom);
                ciutada.setEdat(edat);
                
                // NO cal fer session.update(ciutada) ni session.save(ciutada)
                // Hibernate ho detectarà automàticament al fer commit()
            }
            
            // commit(): Dirty Checking
            // Hibernate compara l'estat actual amb la snapshot inicial
            // Detecta els canvis i executa: UPDATE ciutadans SET nom=?, cognom=?, edat=? WHERE ciutada_id=?
            tx.commit();
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            
        } finally {
            session.close();
        }
    }

    /**
     * OPERACIÓ DELETE: Esborrar una entitat genèrica
     * 
     * Mètode genèric que funciona per a qualsevol classe d'entitat.
     * Utilitza Generics (<T>) per poder esborrar Ciutat, Ciutada o qualsevol altra entitat.
     * 
     * IMPORTANT:
     * - L'entitat s'ha de carregar abans d'esborrar-la
     * - No podem fer simplement session.remove(new Ciutat(id))
     * - Hibernate necessita l'objecte PERSISTENT per fer el DELETE
     * 
     * CASCADES:
     * - Si hi ha cascade="all" o cascade="delete" definit als mapatges,
     *   esborrar una Ciutat també esborrarà els seus Ciutadans
     * - Cal tenir cuidat amb les cascades per evitar esborrar dades per error
     * 
     * @param <T> Tipus genèric de l'entitat
     * @param clazz Classe de l'entitat (ex: Ciutat.class, Ciutada.class)
     * @param id ID de l'entitat a esborrar
     */
    public static <T> void delete(Class<T> clazz, Long id) {
        Session session = factory.openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            
            // find(): Carreguem l'entitat per ID
            // L'objecte passa a estat PERSISTENT
            T entity = session.find(clazz, id);
            
            if (entity != null) {
                // remove(): Marca l'objecte per ser esborrat (estat REMOVED)
                // També el treu de la sessió
                session.remove(entity);
                
                // Al fer commit(), Hibernate executa el DELETE FROM taula WHERE id=?
            }
            
            // commit(): Executa el DELETE real a la base de dades
            tx.commit();
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            
        } finally {
            session.close();
        }
    }

    /**
     * OPERACIÓ READ: Llistar totes les entitats d'un tipus
     * 
     * PUNT CLAU D'EXAMEN: HQL (Hibernate Query Language)
     * 
     * HQL és diferent de SQL:
     * - SQL: SELECT * FROM ciutats
     * - HQL: FROM Ciutat
     * 
     * DIFERÈNCIES CLAU:
     * - HQL treballa amb classes i propietats Java, no amb taules i columnes
     * - "FROM Ciutat" (nom de la classe), no "FROM ciutats" (nom de la taula)
     * - "ORDER BY poblacio" (propietat Java), no "ORDER BY poblacio" (columna BD)
     * - HQL és case-sensitive per als noms de classe
     * 
     * LAZY LOADING:
     * - Si les relacions estan definides amb lazy="true" (o per defecte),
     *   les col·leccions (com ciutadans) NO es carreguen automàticament
     * - Si intentem accedir a ciutadans després de tancar la sessió → ERROR
     * - LazyInitializationException: "failed to lazily initialize a collection"
     * 
     * SOLUCIÓ: initialize()
     * - org.hibernate.Hibernate.initialize() força la càrrega de les relacions
     * - S'ha de cridar ABANS de tancar la sessió
     * 
     * TRY-WITH-RESOURCES:
     * - try (Session session = ...) tanca automàticament la sessió al final
     * - Equivalent a try-finally amb session.close()
     * 
     * @param <T> Tipus genèric de l'entitat
     * @param clazz Classe de l'entitat a llistar
     * @param orderBy Camp pel qual ordenar (opcional, pot ser null)
     * @return Col·lecció amb totes les entitats del tipus especificat
     */
    public static <T> Collection<T> listCollection(Class<T> clazz, String orderBy) {
        // try-with-resources: la sessió es tanca automàticament al final del bloc
        try (Session session = factory.openSession()) {
            
            // Construïm la consulta HQL
            // getSimpleName() retorna només el nom de la classe (ex: "Ciutat", no "com.project.Ciutat")
            String hql = "FROM " + clazz.getSimpleName();
            
            // Si s'especifica un camp d'ordenació, l'afegim
            if (orderBy != null && !orderBy.isEmpty()) {
                hql += " ORDER BY " + orderBy;
                // Exemple: "FROM Ciutat ORDER BY nom"
            }
            
            // createQuery(): Crea una Query HQL
            // - Primer paràmetre: la consulta HQL
            // - Segon paràmetre: la classe del resultat (per type safety)
            // list(): Executa la query i retorna una List amb els resultats
            List<T> results = session.createQuery(hql, clazz).list();
            
            /**
             * PUNT CLAU: initialize() per evitar LazyInitializationException
             * 
             * Problema:
             * - Les relacions (com ciutadans a Ciutat) poden ser lazy
             * - Si no les carreguem abans de tancar la sessió → ERROR
             * - Quan intentem accedir a ciutadans fora d'aquesta funció, la sessió estarà tancada
             * 
             * Solució:
             * - initialize(entity) carrega totes les col·leccions lazy de l'objecte
             * - Ho fem mentre la sessió encara està oberta
             * - Així els objectes retornats poden usar-se sense problemes fora d'aquesta funció
             * 
             * Nota: initialize() NO és recursiu
             * - Només carrega les col·leccions directes de l'entitat
             * - Si hi ha col·leccions dins de col·leccions, cal fer initialize() també per a elles
             */
            for (T entity : results) {
                org.hibernate.Hibernate.initialize(entity);
            }
            
            // Retornem la col·lecció
            // Els objectes estan en estat DETACHED (sessió tancada)
            // però les seves col·leccions ja estan carregades
            return results;
        }
        // La sessió es tanca automàticament aquí (try-with-resources)
    }

    /**
     * OPERACIÓ READ: Obtenir una Ciutat amb els seus Ciutadans
     * 
     * Similar a listCollection, però només retorna una entitat específica.
     * 
     * LAZY vs EAGER:
     * - Si lazy="false" al XML: els ciutadans es carreguen automàticament amb la ciutat
     * - Si lazy="true" (o per defecte): cal fer initialize() manualment
     * 
     * Per assegurar que sempre funcioni (independentment de la configuració),
     * fem initialize() explícitament.
     * 
     * @param ciutatId ID de la ciutat a cercar
     * @return Ciutat amb la col·lecció de ciutadans carregada, o null si no existeix
     */
    public static Ciutat getCiutatWithCiutadans(Long ciutatId) {
        // try-with-resources per tancar automàticament la sessió
        try (Session session = factory.openSession()) {
            
            // find(): Cerca la ciutat per ID
            Ciutat ciutat = session.find(Ciutat.class, ciutatId);
            
            if (ciutat != null) {
                /**
                 * initialize(): Força la càrrega de la col·lecció ciutadans
                 * 
                 * Què fa exactament?
                 * - Comprova si la col·lecció està inicialitzada
                 * - Si no ho està (lazy), executa el SELECT per carregar-la
                 * - Si ja està carregada (eager), no fa res
                 * 
                 * Per què cal?
                 * - Si la relació és lazy i no fem initialize()
                 * - Quan la sessió es tanqui i intentem accedir a getCiutadans() → ERROR
                 * - LazyInitializationException
                 * 
                 * SQL que s'executa:
                 * SELECT * FROM ciutadans WHERE ciutat_id = ?
                 */
                org.hibernate.Hibernate.initialize(ciutat.getCiutadans());
            }
            
            // Retornem la ciutat (estat DETACHED, però amb ciutadans carregats)
            return ciutat;
        }
        // Sessió tancada automàticament
    }

    /**
     * UTILITAT: Convertir una col·lecció a String per mostrar-la
     * 
     * Mètode auxiliar per formatar col·leccions per a la consola.
     * No és específic de Hibernate, però útil per debugging i mostrar resultats.
     * 
     * @param clazz Classe dels elements (per mostrar un missatge si està buida)
     * @param collection Col·lecció a convertir
     * @return String amb tots els elements (un per línia) o missatge si està buida
     */
    public static String collectionToString(Class<?> clazz, Collection<?> collection) {
        // Si la col·lecció és null o buida, retornem un missatge informatiu
        if (collection == null || collection.isEmpty()) {
            return "[Cap " + clazz.getSimpleName() + " trobat]";
        }
        
        // StringBuilder per construir el String de manera eficient
        StringBuilder sb = new StringBuilder();
        
        // Per cada objecte, cridem el seu toString() i afegim un salt de línia
        for (Object obj : collection) {
            sb.append(obj.toString()).append("\n");
        }
        
        return sb.toString();
    }
}