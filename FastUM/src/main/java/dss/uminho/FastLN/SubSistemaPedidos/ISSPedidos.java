package dss.uminho.FastLN.SubSistemaPedidos;

import dss.uminho.FastLN.LNException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Interface do Subsistema de Pedidos.
 * Define as operações de gestão de pedidos, consulta de catálogos e
 * monitorização do estado de preparação nos restaurantes.
 */
public interface ISSPedidos {

    /**
     * Obtém todos os pedidos de um restaurante que ainda estão em fase de processamento.
     * Realiza uma cópia defensiva de cada pedido para garantir a integridade dos dados.
     * @param res ID do restaurante.
     * @return Map que associa o ID do pedido ao objeto Pedido correspondente.
     * @throws NoSuchElementException Caso o ID do restaurante não exista.
     */
    public Map<String, Pedido> getPedidosPorFazer(String res) throws NoSuchElementException;

    public Map<String,Pedido> getPedidosPorEntregar(String res) throws NoSuchElementException;
    /**
     * Regista um novo pedido no sistema de um restaurante específico.
     * @param res ID do restaurante onde o pedido será realizado.
     * @param p   Objeto Pedido contendo os itens e tarefas selecionadas pelo cliente.
     * @throws NoSuchElementException Caso o ID do restaurante não exista.
     */
    public void adicionarPedido(String res, Pedido p) throws NoSuchElementException;

    /**
     * Consulta a lista de itens (produtos) disponíveis para venda num determinado restaurante.
     * @param res ID do restaurante.
     * @return Map de IDs de itens para os respetivos objetos Item.
     * @throws NoSuchElementException Caso o ID do restaurante não exista.
     */
    public Map<String, Item> getCatalogo(String res) throws NoSuchElementException;

    /**
     * Obtém a listagem global de todos os restaurantes registados no sistema.
     * * @return Map de IDs para objetos Restaurante.
     */
    public Map<String, Restaurante> getRestaurantes();

    /**
     * Recupera um item específico do catálogo de um restaurante.
     * @param res    ID do restaurante.
     * @param idItem ID do item/produto pretendido.
     * @return O objeto Item correspondente.
     * @throws NoSuchElementException Caso o restaurante ou o item não existam.
     */
    public Item getItem(String res, String idItem) throws NoSuchElementException;

    /**
     * Atualiza o progresso de um pedido, marcando uma tarefa específica de um item como concluída.
     * Este método percorre as tarefas selecionadas do item para encontrar a correspondência.
     * @param restaurante ID do restaurante.
     * @param pedidoId    ID do pedido.
     * @param itemId      ID do item dentro do pedido.
     * @param tarefa      Objeto tarefa a ser marcado como feito (identificado pela descrição).
     * @throws LNException Caso o restaurante, pedido ou item não sejam encontrados.
     */
    public void marcarTarefaConcluida(String restaurante, String pedidoId,
                               String itemId, int instancia, Tarefa tarefa)
            throws LNException;

    /**
     * Altera o estado global de um pedido (ex: de EMPREPARACAO para PRONTO).
     * @param restaurante ID do restaurante.
     * @param pedidoId    ID do pedido.
     * @param novoEstado  O novo {@link EstadoPedido} a aplicar.
     * @throws LNException Caso o restaurante ou o pedido não existam.
     */
    public void atualizarEstadoPedido(String restaurante, String pedidoId,
                               EstadoPedido novoEstado) throws LNException;

    public void atualizarPedido(String restaurante, Pedido p) throws LNException;

    /**
     * Recupera a informação detalhada de um pedido específico.
     * Retorna uma cópia (clone) do pedido para evitar modificações externas diretas.
     * @param restaurante ID do restaurante.
     * @param pedidoId    ID do pedido.
     * @return Cópia do Pedido solicitado.
     * @throws NoSuchElementException Caso o restaurante ou o pedido não existam.
     */
    public Pedido getPedido(String restaurante, String pedidoId) throws NoSuchElementException;


}