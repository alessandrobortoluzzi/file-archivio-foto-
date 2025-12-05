// Luogo.java

public class Luogo {
    private String nome;
    private String descrizione;
    
    public Luogo(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }
}