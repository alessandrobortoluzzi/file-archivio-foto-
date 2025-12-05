package archiviofoto;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.io.*;

public class Main {
    private static String passwordAmministratore = "admin123";

    public static void main(String[] argomenti) {
        Scanner scanner = new Scanner(System.in);
        Archivio archivio = new Archivio();
        archivio.caricaDaFile();
        InserimentoDatiAzienda.caricaSedePrincipale(archivio, scanner);

        if (archivio.getSedi().isEmpty()) {
            System.out.println("\nBenvenuto! Configura la sede principale.");
            aggiungiSede(archivio, scanner);
        }

        System.out.println("\n=== ARCHIVIO FOTOGRAFICO ===\n");

        try {
            boolean esci = false;
            while (!esci) {
                stampaMenu();
                int scelta = leggiIntero(scanner, "Scegli: ");
                if (scelta == 0) {
                    esci = true;
                } else if (scelta == 1) {
                    inserisciSoggetto(archivio, scanner);
                } else if (scelta == 2) {
                    inserisciFoto(archivio, scanner);
                } else if (scelta == 3) {
                    cercaFoto(archivio, scanner);
                } else if (scelta == 4) {
                    mostraHelp();
                } else if (scelta == 5) {
                    gestisciAdmin(archivio, scanner);
                } else {
                    System.out.println("Scelta non valida.\n");
                }
            }
        } finally {
            archivio.salvaSuFile();
            salvaJsonArchivio(archivio, "archivio.json");
            System.out.println("Dati salvati (testo e JSON). Arrivederci!");
            scanner.close();
        }
    }

    private static void stampaMenu() {
        System.out.println("MENU PRINCIPALE");
        System.out.println("1 → Nuovo soggetto");
        System.out.println("2 → Nuova foto");
        System.out.println("3 → Cerca foto");
        System.out.println("4 → Help");
        System.out.println("5 → Admin (protetto)");
        System.out.println("0 → Esci");
        System.out.println("─".repeat(30));
    }

    private static void gestisciAdmin(Archivio archivio, Scanner scanner) {
        String password = leggiInput(scanner, "Password admin: ", false, s -> !s.isEmpty());
        if (!password.equals(passwordAmministratore)) {
            System.out.println("Accesso negato.\n");
            return;
        }

        boolean torna = false;
        while (!torna) {
            System.out.println("\nADMIN");
            System.out.println("1 → Modifica sede principale");
            System.out.println("2 → Cambia password admin");
            System.out.println("3 → Aggiungi nuova sede");
            System.out.println("0 → Torna al menu");

            int scelta = leggiIntero(scanner, "Scelta: ");
            if (scelta == 0) {
                torna = true;
            } else if (scelta == 1) {
                InserimentoDatiAzienda.modificaSedePrincipale(archivio, scanner);
            } else if (scelta == 2) {
                cambiaPasswordAdmin(scanner);
            } else if (scelta == 3) {
                aggiungiSede(archivio, scanner);
            } else {
                System.out.println("Opzione non valida.");
            }
        }
    }

    private static void cambiaPasswordAdmin(Scanner scanner) {
        String vecchia = leggiInput(scanner, "Vecchia password: ", false, s -> !s.isEmpty());
        if (!vecchia.equals(passwordAmministratore)) {
            System.out.println("Password errata.\n");
            return;
        }

        String nuova = leggiInput(scanner, "Nuova password: ", false, s -> s.length() >= 4);
        String conferma = leggiInput(scanner, "Conferma: ", false, s -> s.equals(nuova));
        if (conferma.equals(nuova)) {
            passwordAmministratore = nuova;
            System.out.println("Password aggiornata con successo.\n");
        }
    }

    private static void mostraHelp() {
        System.out.println("\nHELP RAPIDO");
        System.out.println("• Prima aggiungi soggetti, poi foto");
        System.out.println("• Le sedi si gestiscono solo da Admin");
        System.out.println("• Ricerca per codice o soggetto");
        System.out.println("• Tutto viene salvato all'uscita\n");
    }

    private static String leggiInput(Scanner scanner, String prompt, boolean maiuscolo, Predicate<String> validatore) {
        System.out.print(prompt);
        int tentativi = 0;
        while (tentativi < 3) {
            String input = scanner.nextLine().trim();
            if (maiuscolo) {
                input = input.toUpperCase();
            }
            if (validatore.test(input)) {
                return input;
            }
            tentativi++;
            int rimasti = 3 - tentativi;
            if (rimasti > 0) {
                System.out.println("Valore non valido (" + rimasti + " rimasti)");
                System.out.print(prompt);
            } else {
                System.out.println("Troppi errori → usato valore default");
            }
        }
        return "VUOTO";
    }

    private static int leggiIntero(Scanner scanner, String prompt) {
        System.out.print(prompt);
        int tentativi = 0;
        while (tentativi < 3) {
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                tentativi++;
                int rimasti = 3 - tentativi;
                if (rimasti > 0) {
                    System.out.println("Inserisci un numero (" + rimasti + " rimasti)");
                    System.out.print(prompt);
                } else {
                    System.out.println("Usato 0");
                }
            }
        }
        return 0;
    }

    private static boolean leggiSiNo(Scanner scanner, String prompt) {
        System.out.print(prompt + " (s/n): ");
        int tentativi = 0;
        while (tentativi < 3) {
            String risposta = scanner.nextLine().trim().toLowerCase();
            if (risposta.equals("s") || risposta.equals("y")) {
                return true;
            }
            if (risposta.equals("n")) {
                return false;
            }
            tentativi++;
            if (tentativi < 3) {
                System.out.println("Rispondi s o n");
                System.out.print(prompt + " (s/n): ");
            } else {
                System.out.println("Considerato no");
            }
        }
        return false;
    }

    private static void inserisciSoggetto(Archivio archivio, Scanner scanner) {
        System.out.println("\nNUOVO SOGGETTO");
        System.out.println("1 Personaggio  2 Politico  3 Artista  4 Opera  5 Luogo");

        int tipo = leggiIntero(scanner, "Tipo: ");
        if (tipo < 1 || tipo > 5) {
            System.out.println("Annullato.\n");
            return;
        }

        String chiave = leggiInput(scanner, "Chiave univoca: ", true, s -> !s.isEmpty());

        try {
            if (tipo <= 3) {
                String nome = leggiInput(scanner, "Nome completo: ", false, s -> !s.isEmpty());
                String genere = leggiInput(scanner, "Genere: ", false, s -> !s.isEmpty());
                boolean deceduto = leggiSiNo(scanner, "Deceduto? ");

                if (tipo == 1) {
                    archivio.aggiungiSoggetto(new Personaggio(chiave, nome, genere, deceduto));
                } else if (tipo == 2) {
                    System.out.print("Partito (invio se nessuno): ");
                    String partito = scanner.nextLine().trim();
                    System.out.print("Carica (invio se nessuna): ");
                    String carica = scanner.nextLine().trim();
                    archivio.aggiungiSoggetto(new PersonaggioPolitico(chiave, nome, genere, deceduto, partito, carica));
                } else {
                    System.out.print("Attività principale: ");
                    String attivita = scanner.nextLine().trim();
                    archivio.aggiungiSoggetto(new Artista(chiave, nome, genere, deceduto, attivita));
                }
            } else if (tipo == 4) {
                String opera = leggiInput(scanner, "Nome opera: ", false, s -> !s.isEmpty());
                String artista = leggiInput(scanner, "Artista: ", false, s -> !s.isEmpty());
                String luogo = leggiInput(scanner, "Luogo esposizione: ", false, s -> !s.isEmpty());
                int anno = leggiIntero(scanner, "Anno: ");
                archivio.aggiungiSoggetto(new OperaArtistica(chiave, opera, artista, luogo, anno));
            } else {
                String nome = leggiInput(scanner, "Nome luogo: ", false, s -> !s.isEmpty());
                String descr = leggiInput(scanner, "Descrizione: ", false, s -> !s.isEmpty());
                archivio.aggiungiSoggetto(new Luogo(chiave, nome, descr));
            }

            System.out.println("Soggetto aggiunto.\n");
        } catch (Exception e) {
            System.out.println("Errore: " + e.getMessage() + "\n");
        }
    }

    private static void inserisciFoto(Archivio archivio, Scanner scanner) {
        List<Sede> sedi = archivio.getSedi();
        if (sedi.isEmpty()) {
            System.out.println("Nessuna sede → usa Admin.\n");
            return;
        }

        System.out.println("Sedi disponibili:");
        for (int i = 0; i < sedi.size(); i++) {
            System.out.println(i + " → " + sedi.get(i).getIndirizzo());
        }
        int indiceSede = leggiIntero(scanner, "Numero sede: ");
        if (indiceSede < 0 || indiceSede >= sedi.size()) {
            System.out.println("Indice non valido.\n");
            return;
        }

        String codice = leggiInput(scanner, "Codice foto: ", true, s -> !s.isEmpty());
        String dimensione = leggiInput(scanner, "Dimensione (es. 10x15): ", false, s -> !s.isEmpty());
        String stato = leggiInput(scanner, "Stato conservazione: ", true, s -> s.matches("OTTIMO|BUONO|DISCRETO|SCADENTE"));
        boolean colori = leggiSiNo(scanner, "A colori? ");

        String tipoStampa;
        if (colori) {
            tipoStampa = leggiInput(scanner, "Tipo stampa (CHIARO/OPACO): ", true, s -> s.matches("CHIARO|OPACO"));
        } else {
            tipoStampa = "B/N";
        }

        String chiaveSogg = leggiInput(scanner, "Chiave soggetto: ", true, s -> !s.isEmpty());

        try {
            Foto foto = new Foto(codice, dimensione, stato, colori, tipoStampa, chiaveSogg);
            archivio.aggiungiFoto(indiceSede, foto);
            System.out.println("Foto aggiunta.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("Errore dati: " + e.getMessage() + "\n");
        }
    }

    private static void cercaFoto(Archivio archivio, Scanner scanner) {
        System.out.println("\nRICERCA FOTO");
        System.out.println("1 Per codice  2 Per soggetto  3 Elenco completo");

        int modo = leggiIntero(scanner, "Modalità: ");
        if (modo < 1 || modo > 3) {
            System.out.println("Non valido.\n");
            return;
        }

        if (modo == 1) {
            String codice = leggiInput(scanner, "Codice foto: ", true, s -> !s.isEmpty());
            boolean trovato = false;
            for (Sede sede : archivio.getSedi()) {
                for (Foto foto : sede.getFoto()) {
                    if (foto.getCodice().equalsIgnoreCase(codice)) {
                        System.out.println("→ " + foto + " @ " + sede.getIndirizzo());
                        trovato = true;
                    }
                }
            }
            if (!trovato) {
                System.out.println("Nessun risultato.\n");
            }
        } else if (modo == 2) {
            String chiave = leggiInput(scanner, "Chiave soggetto: ", true, s -> !s.isEmpty());
            boolean trovato = false;
            for (Sede sede : archivio.getSedi()) {
                for (Foto foto : sede.getFoto()) {
                    if (foto.getChiaveSoggetto().equalsIgnoreCase(chiave)) {
                        System.out.println("→ " + foto + " @ " + sede.getIndirizzo());
                        trovato = true;
                    }
                }
            }
            if (!trovato) {
                System.out.println("Nessun risultato.\n");
            }
        } else {
            List<Sede> listaComplete = archivio.getSedi();
            if (listaComplete.isEmpty()) {
                System.out.println("Nessuna sede.\n");
                return;
            }
            for (int i = 0; i < listaComplete.size(); i++) {
                Sede sede = listaComplete.get(i);
                System.out.println("\nSede " + i + ": " + sede.getIndirizzo());
                List<Foto> foto = sede.getFoto();
                if (foto.isEmpty()) {
                    System.out.println("Nessuna foto.");
                } else {
                    for (Foto f : foto) {
                        System.out.println("- " + f.getCodice() + ": " + f.getDimensione() + ", " + f.getStatoConservazione());
                    }
                }
            }
            System.out.println();
        }
    }

    private static void aggiungiSede(Archivio archivio, Scanner scanner) {
        System.out.println("\nNUOVA SEDE");
        String responsabile = leggiInput(scanner, "Responsabile: ", false, s -> !s.isEmpty());
        String indirizzo = leggiInput(scanner, "Indirizzo: ", false, s -> !s.isEmpty());
        String telefono = leggiInput(scanner, "Telefono: ", false, t -> t.length() >= 7 && t.length() <= 20 && t.matches("[0-9+\-() ]+"));
        String orario = leggiInput(scanner, "Orario apertura: ", false, s -> !s.isEmpty());

        Sede nuovaSede = new Sede(responsabile, indirizzo, telefono, orario);
        archivio.aggiungiSede(nuovaSede);
        System.out.println("Sede aggiunta con successo.\n");
    }

    private static void salvaJsonArchivio(Archivio archivio, String nomeFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeFile))) {
            writer.println("{");
            writer.println("  \"sedi\": [");
            List<Sede> sedi = archivio.getSedi();
            for (int i = 0; i < sedi.size(); i++) {
                Sede s = sedi.get(i);
                writer.println("    {");
                writer.println("      \"responsabile\": \"" + s.getResponsabile() + \"\",);
                writer.println("      \"indirizzo\": \"" + s.getIndirizzo() + \"\",);
                writer.println("      \"telefono\": \"" + s.getTelefono() + \"\",);
                writer.println("      \"orario\": \"" + s.getOrario() + \"\",);
                writer.println("      \"foto\": [");
                List<Foto> foto = s.getFoto();
                for (int j = 0; j < foto.size(); j++) {
                    Foto f = foto.get(j);
                    writer.println("        {");
                    writer.println("          \"codice\": \"" + f.getCodice() + \"\",);
                    writer.println("          \"dimensione\": \"" + f.getDimensione() + \"\",);
                    writer.println("          \"stato\": \"" + f.getStatoConservazione() + \"\",);
                    writer.println("          \"aColori\": " + f.isAColori() + ",");
                    writer.println("          \"tipoStampa\": \"" + f.getTipoStampa() + \"\",);
                    writer.println("          \"chiaveSoggetto\": \"" + f.getChiaveSoggetto() + \"\"");
                    writer.print("        }");
                    if (j < foto.size() - 1) writer.println(",");
                    else writer.println();
                }
                writer.println("      ]");
                writer.print("    }");
                if (i < sedi.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("  ],");
            writer.println("  \"soggetti\": [");
            List<Soggetto> soggetti = archivio.getSoggetti();
            for (int i = 0; i < soggetti.size(); i++) {
                Soggetto sogg = soggetti.get(i);
                writer.println("    {");
                writer.println("      \"chiave\": \"" + sogg.getChiave() + \"\",);
                writer.println("      \"tipo\": \"" + sogg.getClass().getSimpleName() + \"\",);
                writer.println("      \"descrizione\": \"" + escapeJson(sogg.descrivi()) + \"\"");
                writer.print("    }");
                if (i < soggetti.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("  ]");
            writer.println("}");
        } catch (IOException e) {
            System.out.println("Errore salvataggio JSON: " + e.getMessage());
        }
    }

    private static String escapeJson(String testo) {
        if (testo == null) return "";
        return testo.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}