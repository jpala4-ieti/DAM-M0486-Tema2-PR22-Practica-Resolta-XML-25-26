package com.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Ciutat implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long ciutatId;
    private String nom;
    private String pais;
    private Integer poblacio;
    private Set<Ciutada> ciutadans = new HashSet<>();
    private String uuid = UUID.randomUUID().toString();
    
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
    
    public void setCiutadans(Set<Ciutada> ciutadans) {
        this.ciutadans.clear();
        if (ciutadans != null) {
            ciutadans.forEach(this::addCiutada);
        }
    }
    
    // Mètodes helper per mantenir coherència bidireccional
    public void addCiutada(Ciutada ciutada) {
        ciutadans.add(ciutada);
        ciutada.setCiutat(this);
    }
    
    public void removeCiutada(Ciutada ciutada) {
        ciutadans.remove(ciutada);
        ciutada.setCiutat(null);
    }
    
    // Mètode toString (igual que la versió JPA)
    @Override
    public String toString() {
        String llistaCiutadans = "Buit";
        if (ciutadans != null && !ciutadans.isEmpty()) {
            llistaCiutadans = ciutadans.stream()
                .map(c -> c.getNom() + " " + c.getCognom())
                .collect(Collectors.joining(" | "));
        }
        
        return String.format("Ciutat [ID=%d, Nom=%s, País=%s, Població=%d, Ciutadans: [%s]]",
            ciutatId, nom, pais, poblacio, llistaCiutadans);
    }
    
    // Equals i hashCode basats en UUID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ciutat)) return false;
        Ciutat ciutat = (Ciutat) o;
        return Objects.equals(uuid, ciutat.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}