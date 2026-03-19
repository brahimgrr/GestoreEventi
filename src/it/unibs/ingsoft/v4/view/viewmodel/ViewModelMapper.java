package it.unibs.ingsoft.v4.view.viewmodel;

import it.unibs.ingsoft.v4.model.Campo;
import it.unibs.ingsoft.v4.model.Categoria;
import it.unibs.ingsoft.v4.model.Notifica;
import it.unibs.ingsoft.v4.model.Proposta;
import it.unibs.ingsoft.v4.service.PropostaService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Converts domain objects to view-models.
 * Called by controllers — keeps domain objects out of ConsoleUI.
 */
public final class ViewModelMapper
{
    private ViewModelMapper() {}

    // ---------------------------------------------------------------
    // Proposta → PropostaVM
    // ---------------------------------------------------------------

    public static PropostaVM toPropostaVM(Proposta p, List<Campo> campi)
    {
        List<String> nomi = campi.stream().map(Campo::getNome).collect(Collectors.toList());
        return new PropostaVM(
                p.getCategoria().getNome(),
                p.getStato().toString(),
                p.getDataPubblicazione(),
                p.getTermineIscrizione(),
                p.getNumeroIscritti(),
                nomi,
                Map.copyOf(p.getValoriCampi())
        );
    }

    public static List<PropostaVM> toPropostaVMList(
            List<Proposta> proposte, Function<Proposta, List<Campo>> campiProvider)
    {
        return proposte.stream()
                .map(p -> toPropostaVM(p, campiProvider.apply(p)))
                .collect(Collectors.toList());
    }

    public static Map<String, List<PropostaVM>> toBachecaVM(
            Map<String, List<Proposta>> bacheca, Function<Proposta, List<Campo>> campiProvider)
    {
        Map<String, List<PropostaVM>> result = new LinkedHashMap<>();
        bacheca.forEach((cat, proposte) ->
                result.put(cat, toPropostaVMList(proposte, campiProvider)));
        return result;
    }

    // ---------------------------------------------------------------
    // Proposta → PropostaSelezionabileVM
    // ---------------------------------------------------------------

    public static List<PropostaSelezionabileVM> toSelezionabileVMList(List<Proposta> proposte)
    {
        return IntStream.range(0, proposte.size())
                .mapToObj(i -> {
                    Proposta p = proposte.get(i);
                    Map<String, String> v = p.getValoriCampi();
                    String termine = p.getTermineIscrizione() != null
                            ? p.getTermineIscrizione().toString() : "";
                    return new PropostaSelezionabileVM(
                            i + 1,
                            v.getOrDefault(PropostaService.CAMPO_TITOLO, "senza titolo"),
                            p.getCategoria().getNome(),
                            v.getOrDefault(PropostaService.CAMPO_DATA, "?"),
                            v.getOrDefault(PropostaService.CAMPO_LUOGO, "?"),
                            termine,
                            v.getOrDefault(PropostaService.CAMPO_QUOTA, ""),
                            p.getNumeroIscritti(),
                            parseIntSafe(v.get(PropostaService.CAMPO_NUM_PARTECIPANTI)),
                            p.getStato().toString()
                    );
                })
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Notifica → NotificaVM
    // ---------------------------------------------------------------

    public static List<NotificaVM> toNotificaVMList(List<Notifica> notifiche)
    {
        return IntStream.range(0, notifiche.size())
                .mapToObj(i -> new NotificaVM(
                        i + 1,
                        notifiche.get(i).getMessaggio(),
                        notifiche.get(i).getData()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Categoria → CategoriaVM
    // ---------------------------------------------------------------

    public static List<CategoriaVM> toCategoriaVMList(List<Categoria> categorie)
    {
        return IntStream.range(0, categorie.size())
                .mapToObj(i -> new CategoriaVM(i + 1, categorie.get(i).getNome()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------

    private static int parseIntSafe(String s)
    {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
