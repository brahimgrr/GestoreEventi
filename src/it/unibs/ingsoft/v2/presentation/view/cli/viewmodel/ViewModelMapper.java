package it.unibs.ingsoft.v2.presentation.view.cli.viewmodel;

import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ViewModelMapper
{
    private ViewModelMapper() {}

    public static PropostaVM toPropostaVM(Proposta p, List<Campo> campi)
    {
        List<String> nomi = campi.stream().map(Campo::getNome).collect(Collectors.toList());
        return new PropostaVM(
                p.getCategoria().getNome(),
                p.getStato().toString(),
                p.getDataPubblicazione(),
                p.getTermineIscrizione(),
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

    public static List<CategoriaVM> toCategoriaVMList(List<Categoria> categorie)
    {
        return IntStream.range(0, categorie.size())
                .mapToObj(i -> new CategoriaVM(i + 1, categorie.get(i).getNome()))
                .collect(Collectors.toList());
    }
}
