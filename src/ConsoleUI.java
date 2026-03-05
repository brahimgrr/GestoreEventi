//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package it.unibs.ingsoft.v1.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class ConsoleUI {
    private final Scanner scanner;

    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }

    public void stampa(String msg) {
        System.out.println(msg);
    }

    public String acquisisciStringa(String prompt) {
        System.out.print(prompt);
        return this.scanner.nextLine();
    }

    public int acquisisciIntero(String prompt, int min, int max) {
        while(true) {
            String s = this.acquisisciStringa(prompt);

            try {
                int v = Integer.parseInt(s.trim());
                if (v >= min && v <= max) {
                    return v;
                }

                this.stampa("Valore fuori range [" + min + ", " + max + "].");
            } catch (NumberFormatException var6) {
                this.stampa("Inserisci un intero valido.");
            }
        }
    }

    public boolean acquisisciSiNo(String prompt) {
        while(true) {
            String s = this.acquisisciStringa(prompt + " (s/n): ").trim().toLowerCase();
            if (!s.equals("s") && !s.equals("si") && !s.equals("sì")) {
                if (!s.equals("n") && !s.equals("no")) {
                    this.stampa("Rispondi con s/n.");
                    continue;
                }

                return false;
            }

            return true;
        }
    }

    public List<String> acquisisciListaNomi(String titolo) {
        this.stampa(titolo);
        this.stampa("Inserisci un nome per riga. Riga vuota per terminare.");
        this.newLine();
        ArrayList<String> list = new ArrayList();

        while(true) {
            String s = this.acquisisciStringa("> ");
            if (s == null || s.isBlank()) {
                return list;
            }

            list.add(s.trim());
        }
    }

    public void newLine() {
        System.out.println();
    }

    public void header(String title) {
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }
}
