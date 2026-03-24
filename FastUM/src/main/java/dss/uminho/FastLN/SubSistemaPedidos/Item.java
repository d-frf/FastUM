package dss.uminho.FastLN.SubSistemaPedidos;

import java.util.List;
import java.util.Map;

/**
 * Representa um item genérico dentro do subsistema de encomendas.
 * Esta interface segue o padrão Composite, permitindo que tanto
 * produtos individuais como conjuntos complexos de itens sejam
 * tratados através de um único tipo.
 */
public interface Item {

    /** @return Identificador único do item. */
    public String getId();

    /** @return Nome legível para humanos. */
    public String getNome();

    /** @return Preço base sem descontos. */
    public double getPreco();

    /**
     * @return Lista de subitens caso se trate de um item composto;
     * null ou lista vazia caso seja um nó folha.
     */
    public List<Item> getSubItens();

    /**
     * @return Um mapa em que as chaves são IDs de Item e os valores
     * são as tarefas (receita) necessárias para produzir essa parte específica.
     */
    public Map<String, List<Tarefa>> getTarefas();

    /**
     * @return Um mapa que agrupa os nomes dos grupos de produção por ID de Item.
     */
    public Map<String, List<String>> getGrupos();

    /** @return Valor do desconto (0.0 a 1.0). */
    public double getDesconto();

    /** @return Uma cópia profunda do item. */
    public Item clone();
}
