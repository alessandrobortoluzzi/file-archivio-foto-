package archiviofoto;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Archivio {
    private final List<Soggetto> soggetti = new ArrayList<>();
    private final List<Sede> sedi = new ArrayList<>();
    private static final String SEDI_FILE = "sedi.txt";
    private static final String SOGGETTI_FILE = "soggetti.txt";
    private static final String FOTO_FILE = "foto.txt";

    public List<Soggetto> getSoggetti() {
        return soggetti;
    }

    public List<Sede> getSedi() {
        return sedi;
    }

    public void aggiungiSede(Sede s) {
        if (s != null) {
            sedi.add(s);
        }
    }

    public void aggiungiSoggetto(Soggetto s) {
        if (s == null) return;
        for (Soggetto esistente : soggetti) {
            if (esistente.getChiave().equalsIgnoreCase(s.getChiave())) {
                System.out.println("ERRORE: chiave già usata -> " + s.getChiave());
                return;
            }
        }
        soggetti.add(s);
        System.out.println("Soggetto aggiunto");
    }

    public void aggiungiFoto(int indiceSede, Foto f) {
        if (f == null) return;
        if (indiceSede < 0 || indiceSede >= sedi.size()) {
            System.out.println("ERRORE: sede non esiste");
            return;
        }
        for (Sede s : sedi) {
            for (Foto foto : s.getFoto()) {
                if (foto.getCodice().equalsIgnoreCase(f.getCodice())) {
                    System.out.println("ERRORE: codice foto già usato");
                    return;
                }
            }
        }
        boolean trovato = false;
        for (Soggetto so : soggetti) {
            if (so.getChiave().equalsIgnoreCase(f.getChiaveSoggetto())) {
                trovato = true;
                break;
            }
        }
        if (!trovato) {
            System.out.println("ERRORE: soggetto non esiste");
            return;
        }
        sedi.get(indiceSede).aggiungiFoto(f);
        System.out.println("Foto aggiunta");
    }

    public void salvaSuFile() {
        salvaSedi();
        salvaSoggetti();
        salvaFoto();
    }

    public void caricaDaFile() {
        caricaSedi();
        caricaSoggetti();
        caricaFoto();
    }

    private void salvaSedi() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SEDI_FILE))) {
            for (Sede s : sedi) {
                pw.println(s.getResponsabile() + "|" + s.getIndirizzo() + "|" + s.getTelefono() + "|" + s.getOrario());
            }
        } catch (IOException e) {
            System.out.println("Errore salvataggio sedi");
        }
    }

    private void caricaSedi() {
        File f = new File(SEDI_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 4) {
                    aggiungiSede(new Sede(p[0], p[1], p[2], p[3]));
                }
            }
        } catch (IOException e) {
            System.out.println("Errore caricamento sedi");
        }
    }

    private void salvaSoggetti() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SOGGETTI_FILE))) {
            for (Soggetto s : soggetti) {
                if (s instanceof PersonaggioPolitico) {
                    PersonaggioPolitico p = (PersonaggioPolitico) s;
                    pw.println("POLITICO|" + p.getChiave() + "|" + p.getNome() + "|" + p.getGenere() + "|" + (p.isDeceduto() ? "1" : "0") + "|" + p.getPartito() + "|" + p.getCarica());
                } else if (s instanceof Artista) {
                    Artista a = (Artista) s;
                    pw.println("ARTISTA|" + a.getChiave() + "|" + a.getNome() + "|" + a.getGenere() + "|" + (a.isDeceduto() ? "1" : "0") + "|" + a.getAttivita());
                } else if (s instanceof Personaggio) {
                    Personaggio p = (Personaggio) s;
                    pw.println("PERSONAGGIO|" + p.getChiave() + "|" + p.getNome() + "|" + p.getGenere() + "|" + (p.isDeceduto() ? "1" : "0"));
                } else if (s instanceof OperaArtistica) {
                    OperaArtistica o = (OperaArtistica) s;
                    pw.println("OPERA|" + o.getChiave() + "|" + o.getNomeOpera() + "|" + o.getArtista() + "|" + o.getLuogo() + "|" + o.getAnno());
                } else if (s instanceof Luogo) {
                    Luogo l = (Luogo) s;
                    pw.println("LUOGO|" + l.getChiave() + "|" + l.getNome() + "|" + l.getDescrizione());
                }
            }
        } catch (IOException e) {
            System.out.println("Errore salvataggio soggetti");
        }
    }

    private void caricaSoggetti() {
        File f = new File(SOGGETTI_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                String tipo = p[0];
                try {
                    if ("PERSONAGGIO".equals(tipo) && p.length >= 5) {
                        aggiungiSoggetto(new Personaggio(p[1], p[2], p[3], "1".equals(p[4])));
                    } else if ("POLITICO".equals(tipo) && p.length >= 7) {
                        aggiungiSoggetto(new PersonaggioPolitico(p[1], p[2], p[3], "1".equals(p[4]), p[5], p[6]));
                    } else if ("ARTISTA".equals(tipo) && p.length >= 6) {
                        aggiungiSoggetto(new Artista(p[1], p[2], p[3], "1".equals(p[4]), p[5]));
                    } else if ("OPERA".equals(tipo) && p.length >= 6) {
                        int anno = 0;
                        try {
                            anno = Integer.parseInt(p[5]);
                        } catch (NumberFormatException ignored) {}
                        aggiungiSoggetto(new OperaArtistica(p[1], p[2], p[3], p[4], anno));
                    } else if ("LUOGO".equals(tipo) && p.length >= 4) {
                        aggiungiSoggetto(new Luogo(p[1], p[2], p[3]));
                    }
                } catch (Exception ex) { }
            }
        } catch (IOException e) {
            System.out.println("Errore caricamento soggetti");
        }
    }

    private void salvaFoto() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FOTO_FILE))) {
            for (int i = 0; i < sedi.size(); i++) {
                Sede s = sedi.get(i);
                for (Foto f : s.getFoto()) {
                    pw.println(f.getCodice() + "|" + f.getDimensione() + "|" + f.getStatoConservazione() + "|" + (f.isAColori() ? "1" : "0") + "|" + f.getTipoStampa() + "|" + f.getChiaveSoggetto() + "|" + i);
                }
            }
        } catch (IOException e) {
            System.out.println("Errore salvataggio foto");
        }
    }

    private void caricaFoto() {
        File f = new File(FOTO_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 7) continue;
                try {
                    String codice = p[0];
                    String dimensione = p[1];
                    String stato = p[2];
                    boolean aColori = "1".equals(p[3]);
                    String tipoStampa = p[4];
                    String chiave = p[5];
                    int indice = Integer.parseInt(p[6]);
                    if (indice < 0 || indice >= sedi.size()) continue;
                    Foto foto = new Foto(codice, dimensione, stato, aColori, tipoStampa, chiave);
                    aggiungiFoto(indice, foto);
                } catch (Exception ex) { }
            }
        } catch (IOException e) {
            System.out.println("Errore caricamento foto");
        }
    }
}