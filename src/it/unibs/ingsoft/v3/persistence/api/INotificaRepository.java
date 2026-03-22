package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.domain.ArchivioNotifiche;

public interface INotificaRepository
{
    ArchivioNotifiche get();
    void save();
}
