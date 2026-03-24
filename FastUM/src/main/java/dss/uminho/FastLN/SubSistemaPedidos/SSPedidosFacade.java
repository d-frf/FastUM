package dss.uminho.FastLN.SubSistemaPedidos;

import dss.uminho.FastDL.RestauranteDAO;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaGestao.Posto;

import java.util.*;
import java.util.stream.Collectors;

public class SSPedidosFacade implements ISSPedidos {

//    private final Map<String, Restaurante> restaurantes;
    private RestauranteDAO restaurantes;

    public SSPedidosFacade() {
//        this.restaurantes = new HashMap<>();
        this.restaurantes = RestauranteDAO.getInstance();
        if ( !this.restaurantes.containsKey("12345") )
            this.carregarDadosIniciais();
    }


    /**
     * @param res 
     * @return
     * @throws NoSuchElementException
     */
    @Override
    public Map<String, Pedido> getPedidosPorEntregar(String res) throws NoSuchElementException {
        Restaurante r = this.restaurantes.get(res);

        if ( r == null ) throw new NoSuchElementException("Restaurante com id " + res + " nao encontrado!");

        return r.getPedidosPorEntregar();
    }

    public Map<String,Pedido> getPedidosPorFazer(String res) throws NoSuchElementException{
        Restaurante r = this.restaurantes.get(res);

        if ( r == null ) throw new NoSuchElementException("Restaurante com id " + res + " nao encontrado!");

        return r.getPedidosPorFazer();
    }

    public void adicionarPedido(String res,Pedido p){
        Restaurante r = this.restaurantes.get(res);

        if ( r == null ) throw new NoSuchElementException("Restaurante com id " + res + " nao encontrado!");

        r.addPedido(p);

        this.restaurantes.put(r.getId(),r);

    }

    public Map<String,Item> getCatalogo(String res){
        Restaurante r = this.restaurantes.get(res);
        if ( r == null ) throw new NoSuchElementException("Restaurante com id " + res + " nao encontrado!");

        return r.getItensVendidos();
    }

    @Override
    public Map<String, Restaurante> getRestaurantes() {
        return this.restaurantes.values().stream().collect(Collectors.toMap(Restaurante::getId,r->r));
    }


    @Override
    public Item getItem(String res, String idItem) throws NoSuchElementException {
        Restaurante r = this.restaurantes.get(res);

        if ( r == null )
            throw new NoSuchElementException("Restaurante " + res + " nao existe!");

        try {
            return r.getItem(idItem);
        } catch (NoSuchElementException | LNException e){
            throw new NoSuchElementException(e.getMessage());
        }
    }

    @Override
    public void marcarTarefaConcluida(String resId, String pedidoId,
                                      String itemId, int instancia, Tarefa tarefa)
            throws LNException {

        Restaurante r = this.restaurantes.get(resId);
        if (r == null) {
            throw new LNException("Restaurante não encontrado!");
        }

        try{
            r.marcarTarefaPorFazer(pedidoId,itemId,instancia,tarefa);
            this.restaurantes.put(r.getId(),r);
        } catch (LNException e) {
            throw new LNException(e.getMessage());
        }

    }

    public void atualizarEstadoPedido(String resId, String pedidoId,
                                      EstadoPedido novoEstado)
            throws LNException {

        Restaurante r = this.restaurantes.get(resId);
        if (r == null) {
            throw new LNException("Restaurante não encontrado!");
        }

        Pedido pedido = r.getPedido(pedidoId);
        if (pedido == null) {
            throw new LNException("Pedido não encontrado!");
        }

        pedido.setEstado(novoEstado);
        r.updatePedido(pedido);
    }


    @Override
    public void atualizarPedido(String restaurante, Pedido p) throws LNException {
        Restaurante r = this.restaurantes.get(restaurante);

        if ( r == null )
            throw new LNException("Restaurante não encontrado");

        r.updatePedido(p);
        this.restaurantes.put(restaurante,r);
    }

    public Pedido getPedido(String resId, String pedidoId)
            throws NoSuchElementException {

        Restaurante r = this.restaurantes.get(resId);
        if (r == null) {
            throw new NoSuchElementException("Restaurante não encontrado!");
        }

        Pedido pedido = r.getPedido(pedidoId);
        if (pedido == null) {
            throw new NoSuchElementException("Pedido não encontrado!");
        }

        return new Pedido(pedido);
    }


    private Map<String, Posto> criarPostosIniciais() {
        Map<String, Posto> postos = new HashMap<>();

        // Postos initialized with no active task (null)
        Posto p1 = new Posto("P_FORNO", "Cozinheiro", Arrays.stream(new Etapa[]{Etapa.COZINHAQUENTE, Etapa.BANCADAFRIA,Etapa.MONTAGEM}).toList(),null);
        Posto p3 = new Posto("P_FOGAO", "Empregado", Arrays.stream(new Etapa[]{Etapa.FINALIZACAO}).toList(),null);
        Posto p4 = new Posto("P_GOD","Chefe cozinha", Arrays.stream(new Etapa[]{Etapa.COZINHAQUENTE,Etapa.BANCADAFRIA,Etapa.MONTAGEM,Etapa.FINALIZACAO}).toList(),null);

        postos.put(p1.getId(), p1);
        postos.put(p3.getId(), p3);
        postos.put(p4.getId(), p4);

        return postos;
    }

    private void carregarDadosIniciais() {
        // 1. Criar Stock com Ingredientes (agora com preços)
        Stock stockInicial = this.criarStockInicial();

        // 2. Criar Itens (Pratos)
        Map<String, Item> itensVendidos = this.criarItensIniciaisMelhorados();

        // 3. Criar Postos
        Map<String,Posto> postos = this.criarPostosIniciais();

        Restaurante rBraga = new Restaurante(
                "12345",
                "Braga",
                stockInicial,
                postos
        );

        this.restaurantes.put(rBraga.getId(),rBraga);

        rBraga.setItensVendidos(itensVendidos);

        this.restaurantes.put(rBraga.getId(),rBraga);

    }

    /**
     * Creates Items (Products) with properly structured Recipes (Tarefas)
     * Structure rules:
     * - opcional=true: Customer can choose to include this task or not (extras)
     * - opcional=false + alternativas!=null: Customer MUST choose one option from alternativas
     * - opcional=false + alternativas==null: Mandatory task, always executed
     * - grupo: Identifies which customization option this task belongs to
     *          (e.g., "normal" vs "vegetariano" for burger type)
     *          Use "" when task is common to all options or doesn't belong to a customization group
     */
    private Map<String, Item> criarItensIniciaisMelhorados() {
        HashMap<String, Item> itens = new HashMap<>();

        // ==========================================================
        // PRATO 001: Bife Grelhado com Acompanhamento
        // ==========================================================
        List<Tarefa> tarefasBife = new ArrayList<>();

        // MANDATORY: Seasoning (common to all, no grupo)
        Tarefa t1 = new Tarefa("Temperar o bife", Etapa.BANCADAFRIA, "", false,0);
        t1.adicionarIngrediente("flor_sal", 2);
        t1.adicionarIngrediente("pimenta_preta", 1);
        tarefasBife.add(t1);

        // EXCLUSIVE CHOICE: Doneness - each alternative represents a different customization option
        Tarefa grelhaPontoMedio = new Tarefa("Grelhar: Ponto Médio (15 min)", Etapa.COZINHAQUENTE, "medio", false,0);
        grelhaPontoMedio.adicionarIngrediente("bife_vazia", 1);

        Tarefa grelhaMalPassado = new Tarefa("Grelhar: Mal Passado (10 min)", Etapa.COZINHAQUENTE, "mal_passado", false,0);
        grelhaMalPassado.adicionarIngrediente("bife_vazia", 1);

        Tarefa grelhaBemPassado = new Tarefa("Grelhar: Bem Passado (20 min)", Etapa.COZINHAQUENTE, "bem_passado", false,0);
        grelhaBemPassado.adicionarIngrediente("bife_vazia", 1);

        // Customer must pick one: medio, mal_passado, or bem_passado
        grelhaPontoMedio.setAlternativas(List.of(grelhaMalPassado, grelhaBemPassado));
        tarefasBife.add(grelhaPontoMedio);

        // OPTIONAL: Garlic Butter (extra, no grupo needed)
        Tarefa manteigarAlho = new Tarefa("Adicionar Manteiga de Alho", Etapa.BANCADAFRIA, "", true,0.50);
        manteigarAlho.adicionarIngrediente("manteiga_alho", 5);
        tarefasBife.add(manteigarAlho);

        // OPTIONAL: Extra Salt
        Tarefa extraSal = new Tarefa("Flor de Sal Extra", Etapa.MONTAGEM, "", true,0.50);
        extraSal.adicionarIngrediente("flor_sal", 3);
        tarefasBife.add(extraSal);

        itens.put("prato_001", new Produto("prato_001", 18.00, "Bife da Vazia Grelhado", 0.0, tarefasBife));

        // ==========================================================
        // PRATO 002: Lombo de Salmão
        // ==========================================================
        List<Tarefa> tarefasSalmao = new ArrayList<>();

        // MANDATORY: Prepare rice base (common task)
        Tarefa prepararArroz = new Tarefa("Preparar Arroz Basmati", Etapa.COZINHAQUENTE, "", false,0);
        prepararArroz.adicionarIngrediente("arroz_basmati", 100);
        tarefasSalmao.add(prepararArroz);

        // EXCLUSIVE CHOICE: Salmon style - each represents a preparation customization
        Tarefa salmaoSimples = new Tarefa("Selar Salmão Simples", Etapa.COZINHAQUENTE, "simples", false,0);
        salmaoSimples.adicionarIngrediente("lombo_salmao", 1);

        Tarefa salmaoTeriyaki = new Tarefa("Selar Salmão Teriyaki", Etapa.COZINHAQUENTE, "teriyaki", false,0);
        salmaoTeriyaki.adicionarIngrediente("lombo_salmao", 1);
        salmaoTeriyaki.adicionarIngrediente("molho_teriyaki", 10);

        Tarefa salmaoManteiga = new Tarefa("Selar Salmão com Manteiga de Alho", Etapa.COZINHAQUENTE, "manteiga_alho", false,0);
        salmaoManteiga.adicionarIngrediente("lombo_salmao", 1);
        salmaoManteiga.adicionarIngrediente("manteiga_alho", 8);

        // Customer chooses: simples, teriyaki, or manteiga_alho
        salmaoSimples.setAlternativas(List.of(salmaoTeriyaki, salmaoManteiga));
        tarefasSalmao.add(salmaoSimples);

        // OPTIONAL: Extra vegetables
        Tarefa vegetaisExtra = new Tarefa("Adicionar Dose Extra de Vegetais", Etapa.MONTAGEM, "", true, 2.5);
        vegetaisExtra.adicionarIngrediente("mix_vegetais", 50);
        tarefasSalmao.add(vegetaisExtra);

        itens.put("prato_002", new Produto("prato_002", 15.50, "Salmão Selection", 0.0, tarefasSalmao));

        // ==========================================================
        // PRATO 003: Frango Fit Customizável
        // ==========================================================
        List<Tarefa> tarefasFrango = new ArrayList<>();

        // MANDATORY: Grill chicken (common)
        Tarefa grelharFrango = new Tarefa("Grelhar Peito de Frango", Etapa.COZINHAQUENTE, "", false,0);
        grelharFrango.adicionarIngrediente("frango_peito", 1);
        grelharFrango.adicionarIngrediente("flor_sal", 1);
        grelharFrango.adicionarIngrediente("pimenta_preta", 1);
        tarefasFrango.add(grelharFrango);

        // EXCLUSIVE CHOICE: Side dish - each is a different side customization
        Tarefa acompBatatadoce = new Tarefa("Acompanhar com Batata Doce Assada", Etapa.COZINHAQUENTE, "batata_doce", false,0);
        acompBatatadoce.adicionarIngrediente("batata_doce", 100);

        Tarefa acompArroz = new Tarefa("Acompanhar com Arroz Basmati", Etapa.COZINHAQUENTE, "arroz", false,0);
        acompArroz.adicionarIngrediente("arroz_basmati", 80);

        Tarefa acompVegetais = new Tarefa("Acompanhar com Mix de Vegetais", Etapa.BANCADAFRIA, "vegetais", false,0);
        acompVegetais.adicionarIngrediente("mix_vegetais", 100);

        // Customer chooses: batata_doce, arroz, or vegetais
        acompBatatadoce.setAlternativas(List.of(acompArroz, acompVegetais));
        tarefasFrango.add(acompBatatadoce);

        // OPTIONAL: Extra protein
        Tarefa proteinaExtra = new Tarefa("Dose Extra de Frango (+100g)", Etapa.COZINHAQUENTE, "", true,5.00);
        proteinaExtra.adicionarIngrediente("frango_peito", 1);
        tarefasFrango.add(proteinaExtra);

        // OPTIONAL: Extra vegetables
        Tarefa vegetaisExtraFrango = new Tarefa("Adicionar Vegetais Extra", Etapa.BANCADAFRIA, "", true,5.00);
        vegetaisExtraFrango.adicionarIngrediente("mix_vegetais", 50);
        tarefasFrango.add(vegetaisExtraFrango);

        // OPTIONAL: Teriyaki sauce
        Tarefa molhoTeriyaki = new Tarefa("Adicionar Molho Teriyaki", Etapa.MONTAGEM, "", true,0.5);
        molhoTeriyaki.adicionarIngrediente("molho_teriyaki", 15);
        tarefasFrango.add(molhoTeriyaki);

        itens.put("prato_003", new Produto("prato_003", 12.00, "Frango Fit Custom", 0.0, tarefasFrango));

        // ==========================================================
        // PRATO 004: Hambúrguer (Normal vs Vegetariano)
        // This example demonstrates the grupo usage as you described
        // ==========================================================
        List<Tarefa> tarefasBurger = new ArrayList<>();

        // MANDATORY: Toast bun (common to all options)
        Tarefa tostarPao = new Tarefa("Tostar Pão de Hambúrguer", Etapa.COZINHAQUENTE, "", false,0);
        tostarPao.adicionarIngrediente("pao_burger", 1);
        tarefasBurger.add(tostarPao);

        // EXCLUSIVE CHOICE: Burger type (Normal vs Vegetariano)
        // This is THE example you gave: grupo marks the customization choice
        Tarefa grelharCarne = new Tarefa("Grelhar Carne", Etapa.COZINHAQUENTE, "normal", false,0);
        grelharCarne.adicionarIngrediente("carne_picada", 150);

        Tarefa grelharSeitan = new Tarefa("Grelhar Seitan", Etapa.COZINHAQUENTE, "vegetariano", false,0);
        grelharSeitan.adicionarIngrediente("seitan", 150);

        // Customer chooses: "normal" or "vegetariano"
        grelharCarne.setAlternativas(List.of(grelharSeitan));
        tarefasBurger.add(grelharCarne);

        // MANDATORY: Add basic toppings (common to all)
        Tarefa adicionarBase = new Tarefa("Adicionar Alface e Tomate", Etapa.MONTAGEM, "", false,0);
        adicionarBase.adicionarIngrediente("alface", 20);
        adicionarBase.adicionarIngrediente("tomate", 30);
        tarefasBurger.add(adicionarBase);

        // EXCLUSIVE CHOICE: Cheese type
        Tarefa queijoCheddar = new Tarefa("Adicionar Queijo Cheddar", Etapa.MONTAGEM, "cheddar", false,0);
        queijoCheddar.adicionarIngrediente("queijo_cheddar", 30);

        Tarefa queijoMozzarella = new Tarefa("Adicionar Queijo Mozzarella", Etapa.MONTAGEM, "mozzarella", false,0);
        queijoMozzarella.adicionarIngrediente("mozzarella", 30);

        Tarefa semQueijo = new Tarefa("Sem Queijo", Etapa.MONTAGEM, "sem_queijo", false,0);

        // Customer chooses cheese: cheddar, mozzarella, or sem_queijo
        queijoCheddar.setAlternativas(List.of(queijoMozzarella, semQueijo));
        tarefasBurger.add(queijoCheddar);

        // OPTIONAL: Extra bacon
        Tarefa extraBacon = new Tarefa("Adicionar Bacon Extra", Etapa.COZINHAQUENTE, "", true , 1.0);
        extraBacon.adicionarIngrediente("bacon", 50);
        tarefasBurger.add(extraBacon);

        // OPTIONAL: Spicy sauce
        Tarefa molhoPicante = new Tarefa("Adicionar Molho Picante", Etapa.MONTAGEM, "", true,1.0);
        molhoPicante.adicionarIngrediente("molho_picante", 10);
        tarefasBurger.add(molhoPicante);

        itens.put("prato_004", new Produto("prato_004", 10.50, "Hambúrguer Customizável", 0.0, tarefasBurger));

        return itens;
    }

    private Stock criarStockInicial() {
        Map<String, StockIngrediente> mapIngredientes = new HashMap<>();

        // --- PROTEÍNAS ---
        adicionarStock(mapIngredientes, "bife_vazia", "Bife da Vazia", 5.50, 50);
        adicionarStock(mapIngredientes, "lombo_salmao", "Lombo de Salmão", 6.00, 40);
        adicionarStock(mapIngredientes, "frango_peito", "Peito de Frango", 3.00, 60);
        adicionarStock(mapIngredientes, "carne_picada", "Carne Picada Bovino", 2.50, 100); // Para Prato 004
        adicionarStock(mapIngredientes, "seitan", "Seitan Fresco", 2.80, 40);             // Para Prato 004 (Veggie)
        adicionarStock(mapIngredientes, "bacon", "Bacon Fumado", 1.20, 80);               // Extra Prato 004

        // --- BASES E ACOMPANHAMENTOS ---
        adicionarStock(mapIngredientes, "pao_burger", "Pão de Hambúrguer Brioche", 0.80, 100);
        adicionarStock(mapIngredientes, "batata_doce", "Batata Doce", 0.50, 200);
        adicionarStock(mapIngredientes, "arroz_basmati", "Arroz Basmati", 0.40, 300);
        adicionarStock(mapIngredientes, "mix_vegetais", "Mix de Vegetais", 1.20, 150);

        // --- VEGETAIS FRESCOS (Toppings) ---
        adicionarStock(mapIngredientes, "alface", "Alface Iceberg", 0.30, 200);
        adicionarStock(mapIngredientes, "tomate", "Tomate em Rodelas", 0.40, 200);

        // --- LATICÍNIOS / QUEIJOS ---
        adicionarStock(mapIngredientes, "queijo_cheddar", "Queijo Cheddar", 0.90, 150);
        adicionarStock(mapIngredientes, "mozzarella", "Queijo Mozzarella", 0.85, 150);
        adicionarStock(mapIngredientes, "manteiga_alho", "Manteiga de Alho", 0.80, 100);

        // --- TEMPEROS E MOLHOS ---
        adicionarStock(mapIngredientes, "molho_teriyaki", "Molho Teriyaki", 1.50, 100);
        adicionarStock(mapIngredientes, "molho_picante", "Molho Picante Caseiro", 1.20, 50);
        adicionarStock(mapIngredientes, "flor_sal", "Flor de Sal", 0.10, 500);
        adicionarStock(mapIngredientes, "pimenta_preta", "Pimenta Preta", 0.10, 200);

        return new Stock(mapIngredientes);
    }


    /**
     * Método auxiliar para criar Ingrediente e StockIngrediente numa só linha
     */
    private void adicionarStock(Map<String, StockIngrediente> map,
                                String id,
                                String nome,
                                double preco,
                                int quantidade) {

        // Agora usamos o construtor correto da sua classe Ingrediente
        Ingrediente ing = new Ingrediente(id, nome, preco);

        StockIngrediente si = new StockIngrediente(quantidade, ing);
        map.put(id, si);
    }

}