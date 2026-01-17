package com.project;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Ciutada implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long ciutadaId;
    private String nom;
    private String cognom;
    private Integer edat;
    private Ciutat ciutat;
    private String uuid = UUID.randomUUID().toString();
    
    // Constructor buit (obligatori per Hibernate)
    public Ciutada() {}
    
    // Constructor amb paràmetres
    public Ciutada(String nom, String cognom, Integer edat) {
        this.nom = nom;
        this.cognom = cognom;
        this.edat = edat;
    }
    
    // Getters i Setters
    public Long getCiutadaId() { return ciutadaId; }
    public void setCiutadaId(Long ciutadaId) { this.ciutadaId = ciutadaId; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getCognom() { return cognom; }
    public void setCognom(String cognom) { this.cognom = cognom; }
    
    public Integer getEdat() { return edat; }
    public void setEdat(Integer edat) { this.edat = edat; }
    
    public Ciutat getCiutat() { return ciutat; }
    public void setCiutat(Ciutat ciutat) { this.ciutat = ciutat; }
    
    // Mètode toString
    @Override
    public String toString() {
        return ciutadaId + ": " + nom + " " + cognom + " (" + edat + " anys)";
    }
    
    // Equals i hashCode basats en UUID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ciutada)) return false;
        Ciutada ciutada = (Ciutada) o;
        return Objects.equals(uuid, ciutada.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}