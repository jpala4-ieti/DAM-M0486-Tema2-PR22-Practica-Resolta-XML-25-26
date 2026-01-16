package com.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Ciutat implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long ciutatId;
    private String nom;
    private String pais;
    private Integer poblacio;
    private Set<Ciutada> ciutadans = new HashSet<>();
    
    // Constructor buit (obligatori per Hibernate)
    public Ciutat() {}
    
    // Constructor amb paràmetres
    public Ciutat(String nom, String pais, Integer poblacio) {
        this.nom = nom;
        this.pais = pais;
        this.poblacio = poblacio;
    }
    
    // Getters i Setters
    public Long getCiutatId() { return ciutatId; }
    public void setCiutatId(Long ciutatId) { this.ciutatId = ciutatId; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    
    public Integer getPoblacio() { return poblacio; }
    public void setPoblacio(Integer poblacio) { this.poblacio = poblacio; }
    
    public Set<Ciutada> getCiutadans() { return ciutadans; }
    public void setCiutadans(Set<Ciutada> ciutadans) { this.ciutadans = ciutadans; }
    
    // Mètode toString
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ciutatId).append(": ").append(nom).append(" (").append(pais).append("), ");
        sb.append("Població: ").append(poblacio).append(", Ciutadans: [");
        
        if (ciutadans != null && !ciutadans.isEmpty()) {
            boolean primer = true;
            for (Ciutada c : ciutadans) {
                if (!primer) sb.append(" | ");
                sb.append(c.getNom()).append(" ").append(c.getCognom());
                primer = false;
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
