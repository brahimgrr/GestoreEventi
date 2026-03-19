package it.unibs.ingsoft.v4.view.viewmodel;

import java.time.LocalDate;

/** View-model for a single Notifica entry. */
public record NotificaVM(int index, String messaggio, LocalDate data) {}
