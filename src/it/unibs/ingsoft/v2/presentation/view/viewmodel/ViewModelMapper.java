package it.unibs.ingsoft.v2.presentation.view.viewmodel;

import it.unibs.ingsoft.v2.domain.AppConstants;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ViewModelMapper
{
    private ViewModelMapper() {}

    public static PropostaVM toPropostaVM(Proposta p)
    {
        return new PropostaVM(
                p.getCategoria().getNome(),
                p.getStato().toString(),
                p.getValoriCampi()
        );
    }

    private static String formatData(LocalDate d)
    {
        return d != null ? d.format(AppConstants.DATE_FMT) : null;
    }

    public static List<PropostaVM> toPropostaVMList(List<Proposta> proposte)
    {
        return proposte.stream()
                .map(ViewModelMapper::toPropostaVM)
                .collect(Collectors.toList());
    }

    public static Map<String, List<PropostaVM>> toBachecaVM(
            Map<String, List<Proposta>> bacheca)
    {
        Map<String, List<PropostaVM>> result = new LinkedHashMap<>();
        bacheca.forEach((cat, proposte) ->
                result.put(cat, toPropostaVMList(proposte)));
        return result;
    }

    public static List<CategoriaVM> toCategoriaVMList(List<Categoria> categorie)
    {
        return IntStream.range(0, categorie.size())
                .mapToObj(i -> new CategoriaVM(i + 1, categorie.get(i).getNome()))
                .collect(Collectors.toList());
    }
}
